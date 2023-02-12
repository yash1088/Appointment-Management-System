package RM2;


import RM1.config.Configuration;
import RM1.damsInterface.DamsInterface;
import RM2.DataBase.Message;
import RM2.ImplementRemoteInterface.MainInterface;
import RM2.Server.Montreal;
import RM2.Server.Quebec;
import RM2.Server.Sherbrooke;
import model.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author Sumit Monapara
 * @created 31/03/2022
 */
public class RM2 {
    public static String Bug_ID = "RM2_BUG";
    public static int lastSequenceID = 0;
    public static ConcurrentHashMap<Integer, Message> message_list = new ConcurrentHashMap<>();
    public static Queue<Message> message_q = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;
    private static boolean BugFlag = true;
    static String host_ip = "";
    static Registry regmtl;
    static MainInterface mainInterface_mtl;
    static Registry regque;
    static MainInterface mainInterface_que;
    static Registry regshe;
    static MainInterface mainInterface_she;
    static int cnt_fault = 0;

    static {
        try {
            regmtl = LocateRegistry.getRegistry(9992);
            mainInterface_mtl = (MainInterface) regmtl.lookup("MTL");

            regque = LocateRegistry.getRegistry(9991);
            mainInterface_que = (MainInterface) regque.lookup("QUE");

            regshe = LocateRegistry.getRegistry(9993);
            mainInterface_she = (MainInterface) regshe.lookup("SHE");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        new Thread(() -> {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

//        new Thread(() -> MTLServer.main(args)).start();
//        Thread.sleep(500);
//        new Thread(() -> QUEServer.main(args)).start();
//        Thread.sleep(500);
//        new Thread(() -> SHEServer.main(args)).start();
//        Thread.sleep(500);
    }

    private static void receive() throws Exception{
        Runnable helloRunnable = new Runnable() {
            public void run() {
                if(!host_ip.isEmpty()){
                    messsageToFront("Heartbeat from RM2",host_ip);
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, Utils.HEARTBEAT_TIME, TimeUnit.SECONDS);

        MulticastSocket socket = null;
        InetAddress mcIPAddress = null;
        try {
            socket = new MulticastSocket(Utils.SEQUENCER_MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP));

            byte[] buffer = new byte[1000];
            System.out.println("RM2 UDP Server Listening for Multicast from Sequencer (port=1234)............");

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
                    02- Sending its list to osther RM for sync
                    03-RM2 had bug and updating its list
                    12-RM2 has bug
                    22-RM2 is down
                */
                if(msg_type.equalsIgnoreCase("00")){
                    Message message = create_message_obj(data);
                    if (message.sequenceId != lastSequenceID + 1 && message.sequenceId > lastSequenceID) {
                        Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "RM2", "Null", "Null", "Null", 0);
                        System.out.println("RM2 send request to update its message list. from:" + lastSequenceID + "To:" + message.sequenceId);

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
                else if (parts[2].equalsIgnoreCase("03") && parts[5].equalsIgnoreCase("RM2")) {
                    update_message_list(parts[1]);
                }
                else if (parts[2].equalsIgnoreCase("12")) {
                    Message message = create_message_obj(data);
                    BugFlag = false;
                    System.out.println("RM2 has bug:" + message.toString());
                }

                else if (parts[2].equalsIgnoreCase("22")) {
                    Runnable crash_task = () -> {
                        try {


                            serversFlag = false;
                            MainInterface mainInterface = (MainInterface) regmtl.lookup("MTL");
                            mainInterface.shutDown();

                            System.out.println("RM2 shutdown Montreal Server");

                            mainInterface = (MainInterface) regque.lookup("QUE");
                            mainInterface.shutDown();
                            System.out.println("RM2 shutdown Quebec Server");

                            mainInterface = (MainInterface) regshe.lookup("SHE");
                            mainInterface.shutDown();
                            System.out.println("RM2 shutdown Sherbrooke Server");

                            Montreal.main(new String[0]);
                            Thread.sleep(500);
                            Quebec.main(new String[0]);
                            Thread.sleep(500);
                            Sherbrooke.main(new String[0]);

                            Thread.sleep(5000);

                            System.out.println("RM2 is reloading servers hashmap");
                            reloadServers();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread handleThread = new Thread(crash_task);
                    handleThread.start();
                    handleThread.join();
                    System.out.println("RM2 handled the crash!");
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
                System.out.println("RM2 update its message list" + message);
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
        System.out.println("RM2 sending its list of messages for initialization. list of messages:" + list);
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
        Registry reg;
        MainInterface mainInterface = null;

        String URL = "";
        System.out.println("Request to server : "+input);
        if (input.patientID.startsWith("MTL")){
            reg = LocateRegistry.getRegistry(9992);
            mainInterface = (MainInterface) regmtl.lookup("MTL");
        }

        else if (input.patientID.startsWith("QUE")){
            reg = LocateRegistry.getRegistry(9991);
            mainInterface = (MainInterface) regque.lookup("QUE");
        }

        else if (input.patientID.startsWith("SHE")){
            reg = LocateRegistry.getRegistry(9993);
            mainInterface = (MainInterface) regshe.lookup("SHE");
        }

        if (input.patientID.charAt(3) == 'P') {
            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {

                String response = mainInterface.bookappointment(input.patientID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = mainInterface.patientappointmentschedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = mainInterface.cancelPatientAppointment(input.patientID, input.newAppointmentID,input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = mainInterface.swapAppointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            }

        }
        else if (input.patientID.charAt(3) == 'A') {

            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {

                String response = mainInterface.bookappointment(input.patientID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = mainInterface.patientappointmentschedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = mainInterface.cancelPatientAppointment(input.patientID, input.newAppointmentID,input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = mainInterface.swapAppointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("ADD_APPOINTMENT")) {
                System.out.println("Add");
                System.out.println(input.bookingCapacity);
                String response = mainInterface.addappointment(String.valueOf(input.newAppointmentType.charAt(0)),input.newAppointmentID,  String.valueOf(input.bookingCapacity));
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("REMOVE_APPOINTMENT")) {
                System.out.println("Remove");
                String response = mainInterface.removeAppointment(input.newAppointmentID, input.newAppointmentType);

                System.out.println(response);
                return response;
            }
            else if (input.Function.equalsIgnoreCase("LIST_AVAILABLE_APPOINTMENT")) {
                if(input.Function.equals("Physician")){
                    input.newAppointmentType = "P";
                }
                String response = mainInterface.listAppointment(input.newAppointmentType);
                System.out.println(response);
                return response;
            }
            else if (input.Function.equalsIgnoreCase("FAULT")) {
                String response = mainInterface.fault(input.patientID);
                if(response.contains("Fault")){
                    System.out.println("if");
                    System.out.println(response.substring(5));
                    if(response.substring(5).equals("3")){
                        response = response+"There is fault in Replica...System is restarting...";

                    }
                    else {
                        response = response.substring(0,5);
                    }

                }
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.patientID.substring(0, 3);
    }

    private static void processRequests() throws Exception {
        System.out.println("before while true");
        while (true) {
            synchronized (RM2.class) {
                Iterator<Message> itr = message_q.iterator();
                while (itr.hasNext()) {
                    Message data = itr.next();
                    if (data.sequenceId == lastSequenceID+1 && serversFlag) {
                        if (data.patientID.equalsIgnoreCase(Bug_ID) && BugFlag == true) {

                            System.out.println("RM2 is executing message request. Detail:" + data);
                            requestToServers(data);
                            Message bug_message = new Message(data.sequenceId, "Null", "RM2",
                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);

                            lastSequenceID += 1;
                            messsageToFront(bug_message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        } else {
                            System.out.println("RM2 is executing message request. Detail:" + data);
                            String response = requestToServers(data);
                            Message message = new Message(data.sequenceId, response, "RM2",
                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);
                            lastSequenceID += 1;
                            messsageToFront(message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        }
                    message_q.remove(data);
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
            System.out.println("Message multicasted from RM2 to other RMs. Detail:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reloadServers() throws Exception {
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            System.out.println("Recovery Mood-RM2 is executing message request. Detail:" + entry.getValue().toString());
            requestToServers(entry.getValue());
            if (entry.getValue().sequenceId >= lastSequenceID)
                lastSequenceID = entry.getValue().sequenceId + 1;
        }
        message_q.clear();
    }
}
