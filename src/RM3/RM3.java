package RM3;


import RM3.Interface.Main_interface;
import RM3.Server.Montreal;
import RM3.Server.Quebec;
import RM3.Server.Sherbrooke;

import RM3.model.Message;
import RM3.model.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

public class RM3 {
    static String host_ip = "";
    public static String Bug_ID = "RM3_BUG";
    public static int lastSequenceID = 0;
    public static ConcurrentHashMap<Integer, Message> message_list = new ConcurrentHashMap<>();
    public static Queue<Message> message_q = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;
    private static boolean BugFlag = true;
    public static Registry registry_she_patient_admin;
    public static Registry registry_mtl_patient_admin;
    public static Registry registry_que_patient_admin;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> Montreal.main(args)).start();
        new Thread(() -> Quebec.main(args)).start();
        new Thread(() -> Sherbrooke.main(args)).start();
    }

    private static void receive() throws Exception{
        Runnable helloRunnable = new Runnable() {
            public void run() {
                if(!host_ip.isEmpty()){
                    messsageToFront("Heartbeat from RM3",host_ip);
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, model.Utils.HEARTBEAT_TIME, TimeUnit.SECONDS);
        MulticastSocket socket = null;

        try {

            socket = new MulticastSocket(Utils.SEQUENCER_MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP));
            byte[] buffer = new byte[1000];
            System.out.println("RM3 UDP Server Listening for Multicast from Sequencer (port=1234)............");

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
                    03-RM3 had bug and updating its list
                    11-RM3 has bug
                    21-RM3 is down
                */
                if(msg_type.equalsIgnoreCase("00")){
                    Message message = create_message_obj(data);
                    if (message.sequenceId != lastSequenceID + 1 && message.sequenceId > lastSequenceID) {
                        Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "RM3", "Null", "Null", "Null", 0);
                        System.out.println("RM3 send request to update its message list. from:" + lastSequenceID + "To:" + message.sequenceId);
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
                else if (parts[2].equalsIgnoreCase("03") && parts[5].equalsIgnoreCase("RM3")) {
                    update_message_list(parts[1]);
                }
                else if (parts[2].equalsIgnoreCase("11")) {
                    Message message = create_message_obj(data);
                    BugFlag = false;
                    System.out.println("RM3 has bug:" + message.toString());
                }
                else if (parts[2].equalsIgnoreCase("21")) {
                    Runnable crash_task = () -> {
                        try {
                            serversFlag = false;

                            registry_mtl_patient_admin = LocateRegistry.getRegistry(1010);
                            Main_interface mtlImpl = (Main_interface) registry_mtl_patient_admin.lookup("MTL");
                            mtlImpl.shutDown();
                            System.out.println("RM3 shutdown Montreal Server");

                            registry_que_patient_admin = LocateRegistry.getRegistry(1000);
                            Main_interface queImpl = (Main_interface) registry_que_patient_admin.lookup("QUE");
                            queImpl.shutDown();
                            System.out.println("RM3 shutdown Quebec Server");

                            registry_she_patient_admin = LocateRegistry.getRegistry(1020);
                            Main_interface sheImpl = (Main_interface) registry_she_patient_admin.lookup("SHE");
                            sheImpl.shutDown();
                            System.out.println("RM3 shutdown Quebec Server");

                            Montreal.main(new String[0]);
                            Thread.sleep(500);
                            Quebec.main(new String[0]);
                            Thread.sleep(500);
                            Sherbrooke.main(new String[0]);

                            Thread.sleep(5000);

                            System.out.println("RM3 is reloading servers hashmap");
                            reloadServers();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread handleThread = new Thread(crash_task);
                    handleThread.start();
                    handleThread.join();
                    System.out.println("RM3 handled the crash!");
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
                System.out.println("RM3 update its message list" + message);
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
        System.out.println("RM3 sending its list of messages for initialization. list of messages:" + list);
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
        Main_interface main_interface = null;
        if (input.patientID.startsWith("MTL")) {
            registry_mtl_patient_admin = LocateRegistry.getRegistry(1010);
            main_interface = (Main_interface) registry_mtl_patient_admin.lookup("MTL");
        } else if (input.Function.startsWith("QUE")) {
            registry_que_patient_admin = LocateRegistry.getRegistry(1000);
            main_interface = (Main_interface) registry_que_patient_admin.lookup("QUE");
        } else if (input.Function.startsWith("SHE")) {
            registry_she_patient_admin = LocateRegistry.getRegistry(1020);
            main_interface = (Main_interface) registry_she_patient_admin.lookup("SHE");
        }

        if (input.patientID.charAt(3) == 'P') {
            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {
                String response = main_interface.book_appointment(input.patientID, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = main_interface.get_appointment_schedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = main_interface.cancel_appointment(input.patientID, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = main_interface.swap_appointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            }

        }

        else if (input.patientID.charAt(3) == 'A') {
            if (input.Function.equalsIgnoreCase("BOOK_APPOINTMENT")) {
                String response = main_interface.book_appointment(input.patientID, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("GET_APPOINTMENT_SCHEDULE")) {
                String response = main_interface.get_appointment_schedule(input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("CANCEL_APPOINTMENT")) {
                String response = main_interface.cancel_appointment(input.patientID, input.newAppointmentID, input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("SWAP_APPOINTMENT")) {
                String response = main_interface.swap_appointment(input.patientID, input.oldAppointmentID, input.oldAppointmentType, input.newAppointmentID, input.newAppointmentType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("ADD_APPOINTMENT")) {
                System.out.println("Add Appointment");
                String response = main_interface.add_appointment(input.newAppointmentID, input.newAppointmentType, input.bookingCapacity);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("REMOVE_APPOINTMENT")) {
                String response = main_interface.remove_appointment(input.newAppointmentID, input.newAppointmentType, input.patientID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("LIST_AVAILABLE_APPOINTMENT")) {
                String response = main_interface.list_appointment_availability(input.newAppointmentType);
                System.out.println("response from gps RM3"+response);
                return response;
            }
        }
        return "Null response from server" + input.patientID.substring(0, 3);
    }

    private static void processRequests() throws Exception {
        System.out.println("before while true");
        while (true) {
            synchronized (RM3.class) {
                Iterator<Message> itr = message_q.iterator();
                while (itr.hasNext()) {
                    Message data = itr.next();
                    if (data.sequenceId == lastSequenceID + 1 && serversFlag) {
                        if (data.patientID.equalsIgnoreCase(Bug_ID) && BugFlag == true) {
                            System.out.println("RM3 is executing message request. Detail:" + data);
                            requestToServers(data);
                            Message bug_message = new Message(data.sequenceId, "Null", "RM3",

                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);
                            lastSequenceID += 1;
                            messsageToFront(bug_message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        } else {
                            System.out.println("RM3 is executing message request. Detail:" + data);
                            String response = requestToServers(data);
                            Message message = new Message(data.sequenceId, response, "RM3",
                                    data.Function, data.patientID, data.newAppointmentID,
                                    data.newAppointmentType, data.oldAppointmentID,
                                    data.oldAppointmentType, data.bookingCapacity);
                            lastSequenceID += 1;
                            messsageToFront(message.toString(), data.FrontIpAddress);
                            message_q.poll();
                        }
                    }
                }
            }
        }
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
        System.out.println("Message to front:" + message);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(4321);
            byte[] bytes = message.getBytes();
            InetAddress aHost = InetAddress.getByName(FrontIpAddress);

            System.out.println(aHost);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, 1999);
            socket.send(request);
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
            InetAddress aHost = InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP);

            DatagramPacket request = new DatagramPacket(data, data.length, aHost, port);
            socket.send(request);
            System.out.println("Message multicasted from RM3 to other RMs. Detail:" + message);
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
            System.out.println("Recovery Mood-RM3 is executing message request. Detail:" + entry.getValue().toString());
            requestToServers(entry.getValue());
            if (entry.getValue().sequenceId >= lastSequenceID)
                lastSequenceID = entry.getValue().sequenceId + 1;
        }
        message_q.clear();
    }
}
