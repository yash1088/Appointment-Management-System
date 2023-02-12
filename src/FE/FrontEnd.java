package FE;


import FE.SendData.SendData_interface;
import FE.SendData.SendData_interfaceHelper;
import model.Utils;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import FE. *;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Pratik Gondaliya
 * @created 06/04/2022
 */

public class FrontEnd {
    private static final int sequencerPort = 1333;
    private static final String sequencerIP = "192.168.2.17";
    private static final String RM_Multicast_group_address = "230.1.1.10";
    private static final int FE_SQ_PORT = 1414;
    private static final int FE_PORT = 1999;
    private static final int RM_Multicast_Port = 1234;
    public static String FE_IP_Address = "192.168.2.11";

    public static void main(String[] args) {

        new Thread(() -> new Sequencer()).start();

        try {

            FrontEnd_interface inter = new FrontEnd_interface() {
                @Override
                public void informRmHasBug(int RmNumber) {
                    Request errorMessage = new Request(RmNumber, "1");
                    System.out.println("Replica Manager:" + RmNumber + "has bug");
                    System.out.println("Bug has been found on Replica Manager:" + RmNumber);
                    sendDataToSequencer(errorMessage);
                }

                @Override
                public void informRmIsDown(int RmNumber) {
                    Request errorMessage = new Request(RmNumber, "2");
                    System.out.println("Raplica Manager:" + RmNumber + "is down");
                    sendDataToSequencer(errorMessage);
                }

                @Override
                public int sendRequestToSequencer(Request Request) {
                    return sendDataToSequencer(Request);
                }

                @Override
                public void retryRequest(Request Request) {
                    System.out.println("No response from all Rms, Retrying request..");
                    sendDataToSequencer(Request);
                }
            };

            ORB orb = ORB.init(args, null);
            POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
            rootpoa.the_POAManager().activate();

            FrontEnd_Implementation servant = new FrontEnd_Implementation(inter);
            Runnable task = () -> {
                listenForUDPResponses(servant);
            };
            Thread thread = new Thread(task);
            thread.start();

            servant.setORB(orb);

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            SendData_interface href = SendData_interfaceHelper.narrow(ref);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            NameComponent[] path = ncRef.to_name("FrontEnd");
            ncRef.rebind(path, href);

            System.out.println("FrontEnd Server is started and running\n");
            while (true) {
                orb.run();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static int sendDataToSequencer(Request requestFromClient) {
        DatagramSocket aSocket = null;
        String dataFromClient = requestFromClient.toString();
        System.out.println("FrontEnd server has send data to Sequencer:- " + dataFromClient);
        int sequenceID = 0;
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getLocalHost();
            DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost,
                    sequencerPort);
            aSocket.send(requestToSequencer);

            byte[] buffer = new byte[1000];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(response);

            String sentence = new String(response.getData(), 0,
                    response.getLength());
            sequenceID = Integer.parseInt(sentence.trim());
            System.out.println("Data Received from Sequencer to FrontEnd (SequenceId):- " + sequenceID + "\n");
        } catch (SocketException e) {
            System.out.println("Error: " + requestFromClient.noRequestSendError());
            System.out.println("Socket Error in FrontEnd: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + requestFromClient.noRequestSendError());
            e.printStackTrace();
            System.out.println("IOException in FrontEnd : " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        return sequenceID;
    }


    private static void listenForUDPResponses(FrontEnd_Implementation servant) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(FE_PORT);
            byte[] buffer = new byte[1000];
            System.out.println("FrontEnd Server Started on the port:- " + FE_PORT);
            while (true) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(response);
                String sentence = new String(response.getData(), 0,
                        response.getLength()).trim();
                if (sentence.startsWith("Heartbeat")) {
                    System.out.println(sentence);
                } else {
                    System.out.println("FrontEnd has received response from Replica Manager:- " + sentence);
                    Response rmResponse = new Response(sentence);
                    System.out.println("Response has been added to List");
                    servant.addReceivedResponse(rmResponse);
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        } finally {
        }
    }


}

/**
 * @author Yash Radadiya
 * @created 02/04/2022
 */
class Sequencer {
        private int sequencerId = 0;
        private final String sequencerIP = "192.168.2.17";
        int temp = 0;
        public String last_message = "";


        public Sequencer() {
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket(1333);
                byte[] buffer = new byte[5000];
                System.out.println("Sequencer UDP Server Started");
                while (true) {

                    DatagramPacket request = new DatagramPacket(buffer,
                            buffer.length);

                    aSocket.receive(request);

                    String object = new String(request.getData(), 0,
                            request.getLength());

                    System.out.println("Object received from FrontEnd:- "+object);

                    String[] parts = object.split(";");
                    int sequencerId1 = Integer.parseInt(parts[0]);
                    temp = sequencerId1;
                    String ip = request.getAddress().getHostAddress();

                    String temp_object = ip + ";" + parts[2] + ";" + parts[3] + ";" + parts[4] + ";" + parts[5] + ";" + parts[6] + ";" + parts[7] + ";" + parts[8] + ";" + parts[9] + ";";

                    sendMessage(temp_object, sequencerId1, parts[2].equalsIgnoreCase("00"));

                    byte[] SeqId = (Integer.toString(sequencerId)).getBytes();
                    InetAddress aHost1 = request.getAddress();
                    int port1 = request.getPort();

                    DatagramPacket request_to_front = new DatagramPacket(SeqId,
                            SeqId.length, aHost1, port1);
                    aSocket.send(request_to_front);
                }

            } catch (SocketException e) {
                System.out.println("Socket Exception in Sequencer: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO Exception in Sequencer: " + e.getMessage());
            } finally {
                if (aSocket != null)
                    aSocket.close();
            }
        }

        public  void sendMessage(String message, int sequencerId1, boolean isRequest) {
            int port = Utils.SEQUENCER_MULTICAST_PORT;
            String finalMessage = "";
            if(!last_message.equals(message)) {
                if (sequencerId1 == 0 && isRequest) {
                    sequencerId1 = ++sequencerId;
                }
                finalMessage = sequencerId1 + ";" + message;
                last_message = message;
            }else{
                finalMessage = sequencerId + ";" + message;
            }

            DatagramSocket aSocket = null;

            try {
                aSocket = new DatagramSocket();
                byte[] messages_RMS = finalMessage.getBytes();
                InetAddress aHost = InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP);

                DatagramPacket request_to_RMS = new DatagramPacket(messages_RMS,
                        messages_RMS.length, aHost, port);
                aSocket.send(request_to_RMS);
                System.out.println("Packet send to RMS");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }