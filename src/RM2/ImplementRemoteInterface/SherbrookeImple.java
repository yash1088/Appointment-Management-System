package RM2.ImplementRemoteInterface;

import org.omg.CORBA.ORB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SherbrookeImple extends UnicastRemoteObject implements MainInterface {
    HashMap<String, String> inner;
    HashMap<String, HashMap<String, String>> outer;
    HashMap<String, String> msg = null;
    HashMap<String, String> tmp = null;
    HashMap<String, HashMap<String, String>> outer_patient = null;
    HashMap<String,String> hp;
    HashMap<String, String> tmp_patient = new HashMap<>();
    DatagramPacket reply = null;
    DatagramSocket ds_swap,ds ;
    DatagramSocket ds_mtl ;
    DatagramPacket reply_mtl = null;
    DatagramSocket ds_que = null;
    DatagramPacket reply_que = null;
    String rep_que = "";
    String rep_mtl = null;
    DatagramPacket requestMTL=null;
    FileWriter writer;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    String send = "";
    String require_data="";
    String msg_patient="";
    String pre_pid ="";
    String msg_udp="";
    int msgfromshe = 0;
    int cntshe = 0;
    String log = "";

    public SherbrookeImple() throws RemoteException, IOException {
        msg = new HashMap<String, String>();
        inner = new HashMap<String, String>();
        outer = new HashMap<String, HashMap<String, String>>();
        tmp = new HashMap<String, String>();
        outer_patient = new HashMap<String, HashMap<String, String>>();

        inner.put("SHEA101010", "10");
        outer.put("P", inner);
        inner = new HashMap<String, String>();

        inner.put("SHEM211022", "15");
        outer.put("S",inner);
        inner = new HashMap<String, String>();

        inner.put("SHEE300100", "20");
        outer.put("D",inner);

        File folder = new File("ServerList");
        folder.mkdir();

        File f = new File(folder, "Sherbrook" + ".txt");

        writer = new FileWriter(f, true);
        writer.write("You are connected to Distributed Appointment Management System");
    }

    private ORB orb;
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public synchronized String bookappointment(String pid, String str) {
        str = str.trim();
        String tempp  = pid.substring(8);//MTLP12342
        pid = pid.substring(0,8);

        if(tempp.equals("1")){
            require_data = "book".concat(pid).concat(str);
            try {
                ds_mtl = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                requestMTL = new DatagramPacket(arr,arr.length,add,9597);

                ds_mtl.send(requestMTL);

                byte[] fetchdataque = new byte[1000];
                reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_mtl.receive(reply_mtl);

                rep_mtl = new String(reply_mtl.getData());
                msg_patient = rep_mtl.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else if(tempp.equals("2")){
            //System.out.println("Hello bookshe");
            require_data = "book".concat(pid).concat(str);
            try {
                ds_que = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

                ds_que.send(requestQUE);

                byte[] fetchdataque = new byte[1000];
                reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_que.receive(reply_que);

                rep_que = new String(reply_que.getData());
                msg_patient = rep_que.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else {
            log = "\n\nbookappointment called by "+ pid +"on "+dtf.format(now);
            printLog(log);

            pre_pid = pid;

            tmp_patient = outer.get(str);

            if(outer.size()==0){
                msg_patient = "there is no appointment";
            }
            else{

                Set<String> str1 = tmp_patient.keySet();
                String str2 = str1.toString();

                HashMap<String, String> mm = outer_patient.get(pid);
                if(mm==null){
                    cntshe = 0;
                }
                else {
                    cntshe = mm.size();
                }
                //start udp for she

                DatagramPacket requestSHE = null;

                try {
                    require_data = "send".concat(pid);
                    ds_que = new DatagramSocket();
                    //System.out.println(apptype);
                    byte[] arr = require_data.getBytes();
                    InetAddress add = InetAddress.getLocalHost();
                    if(pid.contains("QUE")){
                        requestSHE = new DatagramPacket(arr,arr.length,add,9597);
                    }
                    else if(pid.contains("MTL")) {
                        requestSHE = new DatagramPacket(arr,arr.length,add,9595);
                    }

                    ds_que.send(requestSHE);

                    byte[] fetchdataque = new byte[1000];
                    reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                    ds_que.receive(reply_que);

                    rep_que = new String(reply_que.getData());
                    msgfromshe = Integer.parseInt(rep_que.trim());
                    System.out.println("Cnt she"+cntshe);
                    System.out.println("Msgfromque"+msgfromshe);
                }
                catch(Exception e){
                    System.out.println("Catch");
                    //System.out.println(e.getMessage());
                }

                if(outer_patient.containsKey(pid)){
                    if(cntshe+msgfromshe==3){
                        System.out.println("Ypu cant book more than 3 appointments");
                        msg_patient = "You can`t book more than 3 appointments except your city!!!";
                        printLog("\nYou can`t book more than 3");
                    }
                    else{

                        HashMap<String,String> testdata = outer_patient.get(pid);

                        if(testdata.containsKey(str)){
                            msg_patient = "You can`t book appointment with same type";
                            printLog("\nYou can`t book appointment with same type");
                        }
                        else{
                            hp = outer_patient.get(pid);
                            hp.put(str, str2.substring(1,11));

                            HashMap<String,String> hh = outer.get(str);    //p={MTLM100200=[10]}

                            Set<String> set_str = hh.keySet();
                            String str11 = set_str.toString();
                            //System.out.println(str11);

                            //System.out.println(hh.values());
                            Collection<String> hhvalue = hh.values();
                            String ff = String.valueOf(hhvalue);
                            String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))-1);

                            HashMap<String,String> to_replce = new HashMap<>();
                            to_replce.put(str11.substring(1,11), temp);
                            outer.replace(str,to_replce);
                            System.out.println("After adding"+outer);

                            printLog("\nYour slot has been booked on"+ str2);

                            msg_patient = "Your slot has been booked on"+ str2;

                        }
                    }
                }
                else{

                    HashMap<String,String> tmpp = new HashMap<>();
                    tmpp.put(str, str2.substring(1,11));
                    outer_patient.put(pid, tmpp);

                    HashMap<String,String> hh = outer.get(str);    //p={MTLM100200=[10]}
                    System.out.println(hh);

                    Set<String> set_str = hh.keySet();
                    String str11 = set_str.toString();
                    System.out.println(str11);

                    //System.out.println(hh.values());
                    Collection<String> hhvalue = hh.values();
                    String ff = String.valueOf(hhvalue);
                    String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))-1);

                    HashMap<String,String> to_replce = new HashMap<>();
                    to_replce.put(str11.substring(1,11), temp);
                    outer.replace(str,to_replce);
                    System.out.println("After adding"+outer);
                    printLog("\nYour slot has been booked on"+ str2);

                    msg_patient = "Your slot has been booked on"+ str2;

                }
            }
        }

        System.out.println(outer_patient);

        return msg_patient;
    }

    public  String sendData() {
        return outer.toString();
    }

    @Override
    public synchronized String addappointment(String apptype, String appid, String capicity) {
        String msg = "";
        if(appid.contains("SHE")){
            if(outer.containsKey(apptype)){
                HashMap<String, String> inner = outer.get(apptype);
                inner.put(appid, capicity);

            }
            else{
                HashMap<String, String> tmp = new HashMap<>();
                tmp.put(appid, capicity);
                outer.put(apptype, tmp);

            }

        }
        else if(appid.contains("MTL")){
            require_data = "add".concat(apptype).concat(appid).concat(capicity);//addPQUEE30010050

            try {
                ds_mtl = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                requestMTL = new DatagramPacket(arr,arr.length,add,9597);

                ds_mtl.send(requestMTL);

                byte[] fetchdataque = new byte[1000];
                reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_mtl.receive(reply_mtl);

                rep_mtl = new String(reply_mtl.getData());
                msg = rep_mtl.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else if(appid.contains("QUE")){
            require_data = "add".concat(apptype).concat(appid).concat(capicity);//addPQUEE30010050
            try {
                ds_que = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

                ds_que.send(requestQUE);

                byte[] fetchdataque = new byte[1000];
                reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_que.receive(reply_que);

                rep_que = new String(reply_que.getData());
                msg = rep_que.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        System.out.println(outer);
        log = "\n\naddappointment called by admin on"+dtf.format(now).concat("\nParameter for this method is  "+ apptype+"(Appointment type)"+appid+"(Appointment id)"+capicity+"(Capicity)").concat("\nappointment is added");
        printLog(log);

        if(outer==null){
            System.out.println("Empty");
            msg = "null";
        }
        else {
            msg = outer.toString();
        }

        return msg;
    }

    public  String listAppointment(String apptype) {
        printLog("\n\nList appointment has been called by SHE admin for the type "+apptype);
        String temp = "";
        String shedata = "";
        if(apptype.contains("Phy")){
            temp = "P";
        }
        else if(apptype.contains("Sur")){
            temp = "S";
        }
        else if(apptype.contains("Den")){
            temp = "D";
        }

        System.out.println("HelloQUE"+outer.get(temp));
        if(outer.get(temp)==null){
            shedata = "No Appointment Available";
        }
        else {
            shedata = outer.get(temp).toString();
        }

        DatagramSocket ds_mtl = null;
        DatagramSocket ds_que = null;
        DatagramPacket reply_mtl = null;
        DatagramPacket reply_que = null;
        String rep_mtl = null;
        String rep_que = null;

        try {
            ds_mtl = new DatagramSocket();


            //System.out.println(apptype);
            String tempp = "list".concat(apptype);
            byte[] arr = tempp.getBytes();
            InetAddress add = InetAddress.getLocalHost();

            DatagramPacket requestMTL = new DatagramPacket(arr,arr.length,add,9597);
            ds_mtl.send(requestMTL);

            byte[] fetchdatamtl = new byte[1000];
            reply_mtl = new DatagramPacket(fetchdatamtl, fetchdatamtl.length);
            ds_mtl.receive(reply_mtl);

            ds_que = new DatagramSocket();
            DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9596);
            ds_que.send(requestQUE);

            byte[] fetchdatashe = new byte[1000];
            reply_que = new DatagramPacket(fetchdatashe, fetchdatashe.length);
            ds_que.receive(reply_que);

            rep_mtl = new String(reply_mtl.getData());
            rep_que = new String(reply_que.getData());
            System.out.println("Hello rep mtl"+rep_mtl.trim());
            System.out.println("rep que"+rep_que.trim());
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

//        requestSHE(apptype);
//        System.out.println("Hello"+outer.get(temp)+rep_que+var);
        send = "Sherbrook Result: "+shedata+"\n"+"Montreal Result: ".concat(rep_mtl.trim())+"\n"+"Quebec Result: ".concat(rep_que.trim());
        return send;

    }

    @Override
    public  String patientappointmentschedule(String pid) {
        String msg = "";
        String tmp_pid = pid.substring(0,8);

        log = "\n\n patientappointmentschedule called "+dtf.format(now).concat("\n Parameter for this method is  "+ pid+"(Patient id)");
        printLog(log);

        HashMap<String,String> tmpp = new HashMap<>();
        tmpp = outer_patient.get(tmp_pid);

        if(pid.length()>9) {
            if(tmpp==null){
                System.out.println("Empty");
                msg = "You don`t have an appointment here";
            }
            else {
                msg = tmpp.toString();
            }
        }
        else {
            if(tmpp==null||tmpp.isEmpty()){
                System.out.println("Empty");
                msg = "You don`t have an appointment here";
            }
            else {
                msg = tmpp.toString();
            }
            try{
                //msg = tmpp.toString();
                ds_mtl = new DatagramSocket();

                //System.out.println(apptype);
                String tempp = "get".concat(pid);
                byte[] arr = tempp.getBytes();
                InetAddress add = InetAddress.getLocalHost();

                DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9597);
                ds_mtl.send(requestQUE);

                byte[] fetchdataque = new byte[1000];
                reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_mtl.receive(reply_mtl);

                ds_que = new DatagramSocket();
                DatagramPacket requestSHE = new DatagramPacket(arr,arr.length,add,9595);
                ds_que.send(requestSHE);

                byte[] fetchdatashe = new byte[1000];
                reply_que = new DatagramPacket(fetchdatashe, fetchdatashe.length);
                ds_que.receive(reply_que);

                rep_mtl = new String(reply_mtl.getData());
                rep_que = new String(reply_que.getData());
                //            System.out.println("Hello rep que"+rep_que.trim());
                //            System.out.println("rep she"+rep_she.trim());
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }

//        requestSHE(apptype);
//        System.out.println("Hello"+outer.get(temp)+rep_que+var);
            msg = "\nMontreal Result: ".concat(rep_mtl.trim())+"\n"+"Quebec Result: ".concat(rep_que.trim())+"\n"+"Sherbrook Result: ".concat(msg);
        }

        return msg;

    }

    @Override
    public synchronized String cancelPatientAppointment(String p_id, String cancel_id, String cancel_type)  {
        log = "\n\n CancelAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ p_id+"(Patient id)"+ cancel_id+"(cancel id)"+ cancel_type+"(App Type)");
        printLog(log);

        String cancel_msg = "";
        HashMap<String, String> old = null;
        require_data = "cancel".concat(p_id).concat(cancel_id).concat(cancel_type);
        if(cancel_id.contains("SHE")){
            if(outer_patient.containsKey(p_id)){
                old = outer_patient.get(p_id);// {MTLP123={P=[MTLM300222]}}
                if(old.containsKey(cancel_type)){  //{P={MTLM1111=[10]}}
                    old.remove(cancel_type);
                    outer_patient.get(p_id).remove(cancel_type);
                    HashMap<String,String> hh = outer.get(cancel_type);    //p={MTLM100200=[10]}
                    Set<String> set_str = hh.keySet();
                    String str11 = set_str.toString();
                    Collection<String> hhvalue = hh.values();
                    String ff = String.valueOf(hhvalue);

                    String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))+1);

                    HashMap<String,String> to_replce = new HashMap<>();
                    to_replce.put(str11.substring(1,11), temp);
                    outer.replace(cancel_type,to_replce);
                    System.out.println("After adding"+outer);
                    printLog("\nRequest succesfully compiled");
                    cancel_msg = "Your appointment successfully cancelled!!!!!!";
                }

            }
            else
            {
                cancel_msg = "You can`t cancel appointment because there is no appointmnet found for this type";
                printLog("\nYou can`t cancel appointment because there is no appointmnet found for this type");
            }
        }
        else if(cancel_id.contains("MTL")){
            try {
                ds_mtl = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                requestMTL = new DatagramPacket(arr,arr.length,add,9597);

                ds_mtl.send(requestMTL);

                byte[] fetchdataque = new byte[1000];
                reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_mtl.receive(reply_mtl);

                rep_mtl = new String(reply_mtl.getData());
                cancel_msg = rep_mtl.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else if(cancel_id.contains("QUE")){
            try {
                ds_que = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

                ds_que.send(requestQUE);

                byte[] fetchdataque = new byte[1000];
                reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_que.receive(reply_que);

                rep_que = new String(reply_que.getData());
                cancel_msg = rep_que.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else {

        }
        System.out.println("Helllo cancel");
        System.out.println(outer_patient);

        return cancel_msg;
    }

    public synchronized  String removeAppointment(String remove_id, String remove_type)  {
        log = "\n\n removeAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ remove_id+"(App id)"+ remove_type+"(App type)");
        printLog(log);

        String remove_msg = "";
        if(remove_id.contains("SHE")){
            if (outer.isEmpty()) {
                remove_msg = "You can not delete appoitment since there is no appointment found";
                printLog("\nYou can not delete appoitment since there is no appointment found");

            }
            else {
                outer.remove(remove_type);
                remove_msg = "Appointment for given type has been removed";
                System.out.println(outer);
                printLog("\nRequest done");
            }
        }
        else if(remove_id.contains("MTL")){
            require_data = "remove".concat(remove_id).concat(remove_type);//removeQUEE300100P

            try {
                ds_mtl = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                requestMTL = new DatagramPacket(arr,arr.length,add,9597);

                ds_mtl.send(requestMTL);

                byte[] fetchdataque = new byte[1000];
                reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_mtl.receive(reply_mtl);

                rep_mtl = new String(reply_mtl.getData());
                remove_msg = rep_mtl.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }
        else if(remove_id.contains("QUE")){
            require_data = "remove".concat(remove_id).concat(remove_type);//removeQUEE300100P
            try {
                ds_que = new DatagramSocket();
                //System.out.println(apptype);
                byte[] arr = require_data.getBytes();
                InetAddress add = InetAddress.getLocalHost();
                DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

                ds_que.send(requestQUE);

                byte[] fetchdataque = new byte[1000];
                reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                ds_que.receive(reply_que);

                rep_que = new String(reply_que.getData());
                remove_msg = rep_que.trim();
            }
            catch(Exception e){
                System.out.println("Catch");
                System.out.println(e.getMessage());
            }
        }


        return remove_msg;
    }

    @Override
    public  void invoke() {
        try {
            writer.write("\n");
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(SherbrookeImple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public synchronized String swapAppointment(String p_id, String oldAppID, String oldAppType, String newAppID, String newAppType) throws RemoteException {
        return null;
    }


    public  String listAppointment(String p_id, String oldAppID, String oldAppType, String newAppID, String newAppType) {
        log = "\n\n SwapAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ p_id+"(P_id)"+ oldAppID+"(Old App id)"+oldAppType+" "+newAppID+" "+newAppType);
        printLog(log);

        String msg = "";
        if(outer_patient.containsKey(p_id)){ //MTLP1234= P = [MTLM101010]
            HashMap<String, String> inner_patient = outer_patient.get(p_id);
            if(newAppType.equals(oldAppType)&&newAppID.contains("SHE")){
                msg = "You can`t swap appointment with same type in your city";
            }
            else{
                if(inner_patient.containsKey(oldAppType)){
                    String require_data = p_id.concat(newAppID).concat(newAppType).concat("swap");
                    if(newAppID.contains("MTL")){
                        DatagramSocket ds_mtl = null;
                        DatagramPacket reply_mtl = null;
                        String rep_mtl = null;

                        System.out.println(require_data);

                        try {
                            ds_mtl = new DatagramSocket();

                            //System.out.println(apptype);
                            byte[] arr = require_data.getBytes();
                            InetAddress add = InetAddress.getLocalHost();
                            DatagramPacket requestMTL = new DatagramPacket(arr,arr.length,add,9597);

                            ds_mtl.send(requestMTL);

                            byte[] fetchdataque = new byte[1000];
                            reply_mtl = new DatagramPacket(fetchdataque, fetchdataque.length);
                            ds_mtl.receive(reply_mtl);//

                            rep_mtl = new String(reply_mtl.getData());
                            cancelPatientAppointment(p_id,oldAppID,oldAppType);
                            msg = "Old Appointment has been replaced by New Appointment!!!";

                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }

                    else if(newAppID.contains("QUE")){

                        DatagramSocket ds_que = null;

                        DatagramPacket reply_que = null;
                        String rep_que = null;
                        System.out.println(require_data);

                        try {
                            ds_que = new DatagramSocket();

                            //System.out.println(apptype);
                            byte[] arr = require_data.getBytes();
                            InetAddress add = InetAddress.getLocalHost();
                            DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

                            ds_que.send(requestQUE);

                            byte[] fetchdataque = new byte[1000];
                            reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
                            ds_que.receive(reply_que);//

                            rep_que = new String(reply_que.getData());
                            cancelPatientAppointment(p_id,oldAppID,oldAppType);
                            msg = "Old Appointment has been replaced by New Appointment!!!";

                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    else
                    {
                        cancelPatientAppointment(p_id,oldAppID,oldAppType);
                        bookappointment(p_id,newAppType);
                        msg = "Old Appointment has been replaced by New Appointment!!!";
                    }
                }
                else{
                    msg = "Something went wrong";
                }
            }

        }
        return msg;
    }

    public  void acceptUDP() {
        try{
            DatagramSocket ds_swap = new DatagramSocket(9596);
            byte [] arr = new byte[1000];
            while(true){
                System.out.println("Sendswap");
                DatagramPacket req = new DatagramPacket(arr,arr.length);
                ds_swap.receive(req);

                String msg1 = new String(req.getData());
                System.out.println(msg1.trim());
                if(msg1.contains("book")){
                    System.out.println("book");//bookMTLP1234P
                    msg_udp = bookappointment(msg1.substring(4,12), msg1.substring(12).trim());//bookMTLP1234
                    //msg_udp = "Appointment Booked";
                    byte [] data = msg_udp.getBytes();
                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }

                else if(msg1.contains("cancel")){
                    msg_udp = cancelPatientAppointment(msg1.substring(6,14),msg1.substring(14,24),msg1.substring(24).trim()); ////cancelMTLP1234MTLE300100P
                    byte [] data = msg_udp.getBytes();
                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }
                else if(msg1.contains("get")){
                    msg_udp = patientappointmentschedule(msg1.substring(3).trim().concat("udp")); //getMTLP1234
                    byte [] data = msg_udp.getBytes();
                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }

                else if(msg1.contains("swap")){
                    System.out.println("crgvhbjn");
                    String p_id = msg1.substring(0,8);  //MTLP1234QUEE300100P
                    String apptype = msg1.substring(18,19);
                    //String p_id = msg1.substring(0,7);

                    bookappointment(p_id,apptype);
                    msg_udp = "done";

                    byte [] data = msg_udp.getBytes();

                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }

                else if(msg1.contains("add")){//addPQUEE30010050
                    msg_udp = addappointment(msg1.substring(3,4),msg1.substring(4,14),msg1.substring(14).trim()); ////cancelMTLP1234MTLE300100P
                    byte [] data = msg_udp.getBytes();
                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }

                else if(msg1.contains("remove")){//removeQUEE300100P
                    msg_udp = removeAppointment(msg1.substring(6,16),msg1.substring(16).trim());
                    byte [] data = msg_udp.getBytes();
                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }

                else if(msg1.contains("list")) {
                    System.out.println("inside if");
                    System.out.println(msg1.substring(4, 5));
                    System.out.println(outer);
                    if (outer.get(msg1.substring(4, 5)) == null) {
                        String msg = "No Appointments Available";
                        byte[] data = msg.getBytes();
                        DatagramPacket reply = new DatagramPacket(data, data.length, req.getAddress(), req.getPort());
                        ds_swap.send(reply);
                    } else {
                        String msg = outer.get(msg1.substring(4, 5)).toString();

                        byte[] data = msg.getBytes();

                        DatagramPacket reply = new DatagramPacket(data, data.length, req.getAddress(), req.getPort());
                        ds_swap.send(reply);
                    }
                }
                else if(msg1.contains("send")){//sendMTLP1234
                    byte [] data;
                    HashMap<String,String> hashMap = outer_patient.get(msg1.substring(4,12).trim());

                    if(hashMap==null){
                        data = "0".getBytes();
                    }
                    else{

                        msg_udp = String.valueOf(hashMap.keySet().stream().count());
                        System.out.println(msg_udp);
                        data = msg_udp.getBytes();
                    }

                    DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
                    ds_swap.send(reply);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printLog(String log){
        try {
            writer.write(log);
            Logger.getLogger(SherbrookeImple.class.getName()).log(Level.SEVERE, log);

        } catch (IOException ex) {
            Logger.getLogger(SherbrookeImple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String shutDown() throws RemoteException {
        outer = new HashMap<>();
        outer_patient = new HashMap<>();
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

    @Override
    public String fault(String adminid) throws RemoteException {
        String msg = "";
        try{
            int x = 5/0;
        }
        catch (Exception e){
            msg = "Exception";
        }
        return msg;
    }

}