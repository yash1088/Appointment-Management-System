package RM1;

import RM1.config.Configuration;
import RM1.damsInterface.DamsInterface;
import RM1.model.Message;
import RM1.server.MTLServer;
import RM1.server.QUEServer;
import RM1.server.SHEServer;
import model.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author Sumit Monapara
 * @created 31/03/2022
 */

public class RM1 {
    public static String Bug_ID = "RM1_BUG";
    public static int lastSequenceID = 0;
    static String host_ip = "";
    public static ConcurrentHashMap<Integer, Message> message_list = new ConcurrentHashMap<>();
    public static Queue<Message> message_q = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;
    private static boolean BugFlag = true;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> MTLServer.main(args)).start();
        Thread.sleep(500);
        new Thread(() -> QUEServer.main(args)).start();
        Thread.sleep(500);
        new Thread(() -> SHEServer.main(args)).start();
        Thread.sleep(500);
    }

    private static void receive() throws Exception{
        Runnable helloRunnable = new Runnable() {
            public void run() {
                if(!host_ip.isEmpty()){
                    messsageToFront("Heartbeat from RM1",host_ip);
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, Utils.HEARTBEAT_TIME, TimeUnit.SECONDS);
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(Utils.SEQUENCER_MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP));
            byte[] buffer = new byte[1000];
            System.out.println("RM1 UDP Server Listening for Multicast from Sequencer (port=1234)............");

            new Thread(() -> {
                try {
                    processRequests();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String data = new String(request.getData(), 0, request.getLength());
                String[] parts = data.split(";");
                System.out.println("Received Data from Sequencer : "+data);

                String msg_type = parts[2];
                /*
                Message Types:
                    00- Simple message
                    01- Sync request between the RMs
                    02- Sending its list to other RM for sync
                    03-RM1 had bug and updating its list
                    11-Rm1 has bug
                    21-Rm1 is down
                */
                if(msg_type.equalsIgnoreCase("00")){
                    Message message = create_message_obj(data);
                    if (message.sequenceId != lastSequenceID + 1) {
                        Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "RM1", "Null", "Null", "Null", 0);
                        System.out.println("RM1 send request to update its message list. from:" + lastSequenceID + "To:" + message.sequenceId);
                        
                        send_multicast_toRM(initial_message);
                    }
                    host_ip = message.getFrontIpAddress();
                    System.out.println("Message is added to queue:" + message + " || lastSequence>>>" + lastSequenceID);
                    message_q.add(message);
                    message_list.put(message.sequenceId, message);
                }
                else if (parts[2].equalsIgnoreCase("01")) {
                    Message message = create_message_obj(data);
                    if (!message_list.contains(message.sequenceId))
                        message_list.put(message.sequenceId, message);
                }
                else if (parts[2].equalsIgnoreCase("02")) {
                    initial_send_list_for_RM(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5]);
                }
                else if (parts[2].equalsIgnoreCase("03") && parts[5].equalsIgnoreCase("RM1")) {
                    update_message_list(parts[1]);
                }
                else if (parts[2].equalsIgnoreCase("11")) {
                    Message message = create_message_obj(data);
                    BugFlag = false;
                    System.out.println("Rm1 has bug:" + message.toString());
                }
                else if (parts[2].equalsIgnoreCase("21")) {
                    Runnable crash_task = () -> {
                        try {
                            
                            serversFlag = false;

                            startRegistry(7777);
                            String URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.MTLSERVER;
                            DamsInterface damsInterface = (DamsInterface) Naming.lookup(URL);
                            damsInterface.shutDown();
                            System.out.println("RM1 shutdown Montreal Server");

                            startRegistry(7777);
                            URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.QUESERVER;
                            damsInterface = (DamsInterface) Naming.lookup(URL);
                            damsInterface.shutDown();
                            System.out.println("RM1 shutdown Quebec Server");

                            startRegistry(7777);
                            URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.SHESERVER;
                            damsInterface = (DamsInterface) Naming.lookup(URL);
                            damsInterface.shutDown();
                            System.out.println("RM1 shutdown Sherbrooke Server");

                            MTLServer.main(new String[0]);
                            Thread.sleep(500);
                            QUEServer.main(new String[0]);
                            Thread.sleep(500);
                            SHEServer.main(new String[0]);

                            Thread.sleep(5000);

                            System.out.println("RM1 is reloading servers hashmap");
                            reloadServers();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread handleThread = new Thread(crash_task);
                    handleThread.start();
                    handleThread.join();
                    System.out.println("RM1 handled the crash!");
                    serversFlag = true;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void update_message_list(String data) {
        String[] parts = data.split("@");
        for (int i = 0; i < parts.length; ++i) {
            Message message = create_message_obj(parts[i]);
            if (!message_list.containsKey(message.sequenceId)) {
                System.out.println("RM1 update its message list" + message);
                message_q.add(message);
                message_list.put(message.sequenceId, message);
            }
        }
    }

    private static void initial_send_list_for_RM(Integer start, Integer end, String RM_name) {
        String list = "";
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            if (entry.getValue().sequenceId > start && entry.getValue().sequenceId < end) {
                list += entry.getValue().toString() + "@";
            }
        }
        if (list.endsWith("@"))
            list.substring(list.length() - 1);
        Message message = new Message(0, list, "03", start.toString(), end.toString(), RM_name, "Null", "Null", "Null", 0);
        System.out.println("RM1 sending its list of messages for initialization. list of messages:" + list);
        send_multicast_toRM(message);
    }

    private static Message create_message_obj(String data) {
        String[] parts = data.split(";");
        int sequenceId = Integer.parseInt(parts[0]);
        String FrontIpAddress = parts[1];
        String MessageType = parts[2];
        String Function = parts[3];
        String patientID = parts[4];
        String newAppointmentID = parts[5];
        String newAppointmentType = parts[6];
        String oldAppointmentID = parts[7];
        String oldAppointmentType = parts[8];
        int bookingCapacity = Integer.parseInt(parts[9]);
        Message message = new Message(sequenceId, FrontIpAddress, MessageType, Function, patientID, newAppointmentID, newAppointmentType, oldAppointmentID, oldAppointmentType, bookingCapacity);
        return message;
    }

    private static String requestToServers(Message input) throws Exception {
        startRegistry(7777);
        String URL = "";
        System.out.println("Request to server : "+input);
        if (input.patientID.startsWith("MTL"))
            URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.MTLSERVER;
        else if (input.patientID.startsWith("QUE"))
            URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.QUESERVER;
        else if (input.patientID.startsWith("SHE"))
            URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.SHESERVER;
        System.out.println("URL : "+URL);
        DamsInterface damsInterface = (DamsInterface) Naming.lookup(URL);

        if (input.patientID.charAt(3) == 'P') {
            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {
               
                String response = damsInterface.bookAppointment(input.patientID, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = damsInterface.getAppointmentSchedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = damsInterface.cancelAppointment(input.patientID, input.newAppointmentID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = damsInterface.swapAppointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            }

        } else if (input.patientID.charAt(3) == 'A') {

            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {

                String response = damsInterface.bookAppointment(input.patientID, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = damsInterface.getAppointmentSchedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = damsInterface.cancelAppointment(input.patientID, input.newAppointmentID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = damsInterface.swapAppointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("ADD_APPOINTMENT")) {
                System.out.println("Add");
                String response = damsInterface.addAppointment(input.newAppointmentID, input.newAppointmentType, input.bookingCapacity);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("REMOVE_APPOINTMENT")) {
                String response = damsInterface.removeAppointment(input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("LIST_AVAILABLE_APPOINTMENT")) {
                String response = damsInterface.listAppointmentAvailability(input.newAppointmentType);
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.patientID.substring(0, 3);
    }

    private static void processRequests() throws Exception {
        System.out.println("before while true");
        while (true) {
            synchronized (RM1.class) {
                Iterator<Message> itr = message_q.iterator();
                while (itr.hasNext()) {
                    Message data = itr.next();
                    if (data.sequenceId == lastSequenceID+1 && serversFlag) {
                        if (data.patientID.equalsIgnoreCase(Bug_ID) && BugFlag == true) {

                            System.out.println("RM1 is executing message request. Detail:" + data);
                            requestToServers(data);
                            Message bug_message = new Message(data.sequenceId, "Null", "RM1",
                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);

                            lastSequenceID += 1;
                            messsageToFront(bug_message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        } else {
                            System.out.println("RM1 is executing message request. Detail:" + data);
                            String response = requestToServers(data);
                            Message message = new Message(data.sequenceId, response, "RM1",
                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);
                            lastSequenceID += 1;
                            messsageToFront(message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        }
                    itr.remove();
                    }
                }
                message_q.clear();
            }
        }
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
        System.out.println("Message to front:" + message+" : FrontIP:"+FrontIpAddress);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] bytes = message.getBytes();
            InetAddress aHost = InetAddress.getByName(FrontIpAddress);

            System.out.println(aHost);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, 1999);
            socket.send(request);
            System.out.println("Message sent to FE");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void send_multicast_toRM(Message message) {
        int port = Utils.SEQUENCER_MULTICAST_PORT;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] data = message.toString().getBytes();
            InetAddress aHost = InetAddress.getLocalHost();

            DatagramPacket request = new DatagramPacket(data, data.length, aHost, port);
            socket.send(request);
            System.out.println("Message multicasted from RM1 to other RMs. Detail:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();

        } catch (RemoteException e) {
            System.out.println("RMI registry cannot be located at port "/**/ + RMIPortNum);
            LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }

    public static void reloadServers() throws Exception {
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            System.out.println("Recovery... RM1 is executing message request. Detail:" + entry.getValue().toString());
            requestToServers(entry.getValue());
            if (entry.getValue().sequenceId >= lastSequenceID)
                lastSequenceID = entry.getValue().sequenceId + 1;
        }
        message_q.clear();
    }
}
