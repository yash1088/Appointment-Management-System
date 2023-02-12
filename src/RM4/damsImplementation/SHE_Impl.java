package RM4.damsImplementation;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import RM4.damsInterface.DamsInterface;
import RM4.model.Mydetails;

/**
 * @author Krishna
 * @created 2022-04-07/04/2022
 */

    public class SHE_Impl extends UnicastRemoteObject implements DamsInterface {
        public HashMap<String, HashMap<String, Mydetails>> database = null;
        DatagramSocket asocket = null;
        String resultString_she_mtl = "";
        String resultString_she_que = "";

        public SHE_Impl() throws RemoteException {
            super();
            database = new HashMap<>();
            // TODO Auto-generated constructor stub
        }

        @Override
        public String bookAppointment(String Uid, String apid, String Apt) throws RemoteException {

            String result="";

            if(database.containsKey(Apt) && database.get(Apt).containsKey(apid)){
                HashMap<String, Mydetails> tmp = database.get(Apt);
                if(tmp.get(apid).getSlots() - tmp.get(apid).getUid().size() > 0 && !(tmp.get(apid).getUid().contains(Uid))){
                    tmp.get(apid).getUid().add(Uid);
                    result = "Appointment booked!!!";
                }else{
                    result = "No more slots available or you have already booked this appointment!!!";
                }
            } else {
                result = "No appointment availbale with this type and ID!";
            }
            //montreal
            new Thread(() -> {
                try {
                    DatagramSocket asocket = new DatagramSocket();
                    byte[] data = ("book"+":"+Uid+"-"+apid+"-"+Apt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6792;

                    DatagramPacket request = new DatagramPacket(data, Apt.length(), aHost, serverPort);
                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_mtl = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();
//quebec
            new Thread(() -> {
                try {
                    asocket = new DatagramSocket();
                    byte[] data = ("book"+":"+Uid+"-"+apid+"-"+Apt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6790;

                    DatagramPacket request = new DatagramPacket(data, Apt.length(), aHost, serverPort);

                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_que = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();


            return result;
        }

        @Override
        public String GetAppointment(String Uid) throws RemoteException{
            System.out.println("Get called");
            String result = "";
            for(Map.Entry<String, HashMap<String, Mydetails>> data : database.entrySet()){
                HashMap<String, Mydetails> tmp = data.getValue();
                for(Map.Entry<String, Mydetails> details : tmp.entrySet()){
                    if(details.getValue().getUid().contains(Uid)){
                        result = data.getKey()+" : "+details.getKey() +",";
                    }
                }
            }
            return result;
        }

        @Override
        public String CancleAppointment(String Uid, String apid) throws RemoteException{

            String result="appointment not found";
            for(Map.Entry<String, HashMap<String, Mydetails>> entry : database.entrySet()){
                HashMap<String, Mydetails> tmpdata = entry.getValue();
                for(Map.Entry<String, Mydetails> data : tmpdata.entrySet()){
                    String key = data.getKey();
                    Mydetails mydetails = data.getValue();
                    if(key.equals(apid) && mydetails.getUid().contains(Uid)){
                        mydetails.getUid().remove(Uid);
                        result = "appointment cancelled!";
                    }
                }
            }
            return result;
        }

        @Override
        public String AddAppointment(String apid, String Apt, int slots) throws RemoteException{
            // TODO Auto-generated method stub
            HashMap<String, RM4.model.Mydetails> tmplistHashMap = new HashMap<>();


            String result = "";
            if (database.containsKey(Apt)) {
                tmplistHashMap = database.get(Apt);
                if (tmplistHashMap.containsKey(apid)) {
                    result = "Appointment id is already exist";
                } else {
                    tmplistHashMap.put(apid, new Mydetails(new ArrayList<>(), slots));
                    database.put(Apt, tmplistHashMap);
                    result = "appoinmetn added";
                }
            } else {
                tmplistHashMap.put(apid, new RM4.model.Mydetails(new ArrayList<>(), slots));
                database.put(Apt, tmplistHashMap);
                result = "appointment added";
            }
            return result;
        }

        @Override
        public String RemoveAppointment(String apid, String Apt)throws RemoteException {
            String result="";
            if(database.containsKey(Apt)){
                HashMap<String, Mydetails> tmp = database.get(Apt);
                if(tmp.containsKey(apid)){
                    Mydetails mydetails = tmp.get(apid);
                    if(mydetails.getUid().size() > 0){
                        result = "patient has booked the appointment.";
                        tmp.remove(apid);
                    }else {
                        result = "no booking found, canceling it!";
                        tmp.remove(apid);
                    }
                }else {
                    result = "No AppointmentID available";
                }

            }else{
                result = "No Appointment Available";
            }
            return result;
        }

        @Override
        public String ListAppointment(String Apt) throws RemoteException{

            String resultString = "";
//montreal
            new Thread(() -> {
                try {
                    DatagramSocket asocket = new DatagramSocket();
                    byte[] data = ("list:"+Apt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6792;

                    DatagramPacket request = new DatagramPacket(data, data.length, aHost, serverPort);
                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_mtl = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();
//quebec
            new Thread(() -> {
                try {
                    asocket = new DatagramSocket();
                    byte[] data = ("list:"+Apt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6790;

                    DatagramPacket request = new DatagramPacket(data, data.length, aHost, serverPort);

                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_que = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();


            try {
                TimeUnit.SECONDS.sleep(2L);
            }catch (Exception e){
                e.printStackTrace();
            }

            if (database.containsKey(Apt)) {
                for (Map.Entry<String, Mydetails> data : database.get(Apt).entrySet()) {
                    data.getKey();

                    if ((data.getValue().getSlots() - data.getValue().getUid().size()) > 0) {
                        resultString += data.getKey() + ":" + (data.getValue().getSlots() - data.getValue().getUid().size())
                                + "\n";
                    }

                }
            }
            String finalString = resultString+resultString_she_mtl+resultString_she_que;
            return (finalString.equals("") ? "No data Found!!" : finalString.trim());

        }

        @Override
        public String SwapAppointment(String Uid, String oApid, String oapt, String nApid, String napt) throws RemoteException {
            String result="";
            String cancel_result="", book_result="";
            cancel_result = CancleAppointment(Uid, oApid);
            if(cancel_result.startsWith("Appointment Canceled")){
                book_result = bookAppointment(Uid, nApid, napt);
                if(book_result.equals("successfull")){
                    result = "Appointment swapped";
                }else{
                    book_result = bookAppointment(Uid, oApid,oapt);
                    result = "Cant swap the appointment!";
                }
            }else{
                result = "Appointment not found";
            }
            String resultString = "";
//montreal
            new Thread(() -> {
                try {
                    DatagramSocket asocket = new DatagramSocket();
                    byte[] data = ("Swap:"+Uid+"-"+oApid+"-"+oapt+"-"+nApid+"-"+napt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6792;

                    DatagramPacket request = new DatagramPacket(data, data.length, aHost, serverPort);
                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_mtl = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();
//quebec
            new Thread(() -> {
                try {
                    asocket = new DatagramSocket();
                    byte[] data = ("Swap:"+Uid+"-"+oApid+"-"+oapt+"-"+nApid+"-"+napt).getBytes();
                    InetAddress aHost = InetAddress.getByName("localhost");
                    int serverPort = 6790;

                    DatagramPacket request = new DatagramPacket(data, data.length, aHost, serverPort);

                    asocket.send(request);
                    byte[] buffer = new byte[10000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    asocket.receive(reply);
                    resultString_she_que = new String(reply.getData()).trim();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();

            return result;

        }

        @Override
        public String shutDown() throws RemoteException {
        HashMap<Object, Object> outer = new HashMap<>();
        ArrayList aList = new ArrayList();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignored
                }
                System.exit(1);
            }
        });
        return "Shutting down";
    }

    public String get_swapappointmentdata(String Uid,String oApid, String oapt,String nApid,String napt) throws RemoteException {
            String result="";
            String cancel_result="", book_result="";
            cancel_result = CancleAppointment(Uid, oApid);
            if(cancel_result.startsWith("Appointment Canceled")){
                book_result = bookAppointment(Uid, nApid, napt);
                if(book_result.equals("successfull")){
                    result = "Appointment swapped";
                }else{
                    book_result = bookAppointment(Uid, oApid,oapt);
                    result = "Cant swap the appointment!";
                }
            }else{
                result = "Appointment not found";
            }

            return result;
        }
        public String get_bookappointmentdata(String Uid,String apid,String Apt){
            String result="";

            if(database.containsKey(Apt) && database.get(Apt).containsKey(apid)){
                HashMap<String, Mydetails> tmp = database.get(Apt);
                if(tmp.get(apid).getSlots() - tmp.get(apid).getUid().size() > 0 && !(tmp.get(apid).getUid().contains(Uid))){
                    tmp.get(apid).getUid().add(Uid);
                    result = "Appointment booked!!!";
                }else{
                    result = "No more slots available or you have already booked this appointment!!!";
                }
            } else {
                result = "No appointment availbale with this type and ID!";
            }
            return result;
        }
        public String get_listappointmentdata(String Apt) {
            String resultString = "";

            if (database.containsKey(Apt)) {
                System.out.println("In function : "+Apt);

                for (Map.Entry<String, Mydetails> data : database.get(Apt.trim()).entrySet()) {
                    data.getKey();

                    if ((data.getValue().getSlots() - data.getValue().getUid().size()) > 0) {
                        resultString += data.getKey() + ":" + (data.getValue().getSlots() - data.getValue().getUid().size())
                                + ",";
                    }

                }
            }
            System.out.println("got : "+resultString);
            return resultString;

        }

    }


