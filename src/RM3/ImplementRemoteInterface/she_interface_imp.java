package RM3.ImplementRemoteInterface;

import RM3.Interface.Main_interface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
// Implementing the remote interface

public class she_interface_imp extends UnicastRemoteObject implements Main_interface {
    static HashMap<String, HashMap<String, Integer>> outer = null;
    static HashMap<String, Integer> inner = null;
    static HashMap<String, HashMap<String, Integer>> list_availability_outer = null;
    static List aList = null;
    static int year = 0;
    static int week = 0;
    static int targetWeek = 0;
    String str = "Hellooooooooooooooooooo";
    static int targetYear = 0;
    static List aList_add = null;
    //For interserver Communication using UDP
    class UDPAddress {
        InetAddress clientAddress;
        int port;
        public UDPAddress(InetAddress clientAddress, int port) {
            this.clientAddress = clientAddress;
            this.port = port;
        }
    }
    public she_interface_imp() throws RemoteException, IOException {
        outer = new HashMap<>();
        inner = new HashMap<>();
        list_availability_outer = new HashMap<>();
//        inner.put("SHEA250222", 5);
//        inner.put("SHEA260222", 5);
//        outer.put("Surgeon", inner);
//        inner.clear();
//        inner.put("SHEM090222", 6);
//        outer.put("Physician", inner);
        aList = new ArrayList();
//        aList.add("SHEA050422,Physician,QUEP1234");
//        aList.add("SHEE060422,Dental,QUEP1234");
        aList_add = new ArrayList();
    }
    // Implementing the interface methods
    @Override
    public boolean authenticate(String client_userName, String client_password) {
        if ((client_userName != null && !client_userName.isEmpty()) && (client_password != null && !client_password.isEmpty())) {
            if ((client_userName.equalsIgnoreCase("userid")) && (client_password.equalsIgnoreCase("password"))) {
                return true;
            }
        }
        return false;
    }
    @Override
    public synchronized String printMsg(String msg) {
        return null;
    }
    //SHE Admin
    // Implementing the interface method for she_admin_client
    //Appointment Creation
    public synchronized String add_appointment(String options_appointment_type_admin, String final_appointmentID_admin, int slot_numbers) {
        if (outer.containsKey(options_appointment_type_admin)) {
//            System.err.println("You can not add because details is already there");
            HashMap<String, Integer> inner = outer.get(options_appointment_type_admin);
            inner.put(final_appointmentID_admin, slot_numbers);
        } else {
            HashMap<String, Integer> tmp = new HashMap<>();
            tmp.put(final_appointmentID_admin, slot_numbers);
            outer.put(options_appointment_type_admin, tmp);
        }
        String result = outer.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        //or something similar
        return result.trim();
    }
    public synchronized String list_appointment_availability(String appointment_type) {
        //For interserver Communication using UDP Client Side code checking in Montreal Server
        String result = "";
        String map_final = "";
        String result_que_mtl = "";
        HashMap<String, Integer> store = new HashMap<>();
        if (outer.containsKey(appointment_type)) {
            store = outer.get(appointment_type);
        }
        //UDP Client side code for Quebec
        HashMap<String, String> map = new HashMap<>();
        try (DatagramSocket aSocket = new DatagramSocket()) {
            String message = appointment_type;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 110;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            if (!(new String(reply.getData()).trim().isEmpty()) || store.size() > 0) {
                result = store.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));
                result_que_mtl = new String(reply.getData()).trim() + "\n" + result.trim();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //UDP Client side code for Montreal
        try (DatagramSocket aSocket = new DatagramSocket()) {
            String message = appointment_type;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 111;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            if (!(new String(reply.getData()).trim().isEmpty()) || result_que_mtl != null) {
                String value = new String(reply.getData()).trim() + "\n" + result_que_mtl.trim();
                map_final = value;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map_final.trim();
    }
    public synchronized HashMap<String, Integer> get_she_server_data(String appointment_type) {
        HashMap<String, Integer> she_response = new HashMap<String, Integer>();
        if (outer.containsKey(appointment_type)) {
            she_response = outer.get(appointment_type);
        }
        return she_response;
    }
    int slots = 0;
    String msg = "";
    String msg1 = "";
    public synchronized String get_data_book_appointment(String patient_Id, String appointmentID, String appointmentType) {
        HashMap<Integer, Integer> week_no_and_number_of_appointment = new HashMap<>();
        boolean can = true;
        for (int i = 0; i < aList.size(); i++) {
            //str = MTLM110222,Physician,QUEA1234
            String str = String.valueOf(aList.get(i));
            String date_raw = str.split(",")[0].trim().substring(4, 10);
            String type = str.split(",")[1].trim();
            String user_id = str.split(",")[2].trim();
            System.out.println(type);
            System.out.println(user_id);
            System.out.println(date_raw);
            String date_raw_patient_entered = appointmentID.split(",")[0].trim().substring(4, 10);
            if (patient_Id.equals(user_id) && appointmentType.equals(type) && date_raw_patient_entered.equals(date_raw)) {
                can = false;
            }
        }
        String raw = get_appointment_schedule(patient_Id);
        String tmp_str = raw.replaceAll("[\\n ]", "");
        System.out.println(tmp_str + "tmp_str");
        String response = tmp_str.replaceAll("\\]\\[", ",").replace(",,", ",").replaceAll("\\[", "").replaceAll("\\]", "");
        System.out.println(response + "result");
        String[] parts = response.split(",");
        List<String> strList = new ArrayList<String>();
        for (int x = 0; x < parts.length - 2; x = x + 3) {
            String tmpStr = parts[x] + "," + parts[x + 1] + "," + parts[x + 2];
            if (parts[x + 2].substring(0, 3).contains("SHE")) {
                System.out.println("Local data will no go inside list...");
            } else {
                strList.add(tmpStr);
            }
        }
        System.out.println(strList + "strList");
        for (int i = 0; i < strList.size(); i++) {
            //str = MTLM110222,Physician,QUEA1234
            str = strList.get(i);
            System.out.println(str);
            //Date = 110222
            String date_raw = str.split(",")[0].trim().substring(4, 10);
            String date_final = date_raw.substring(0, 2) + "/" + date_raw.substring(2, 4) + "/20" + date_raw.substring(4, 6);
            System.out.println(date_final);
            String booked_date = date_final;
            Date date = null;
            try {
                date = new SimpleDateFormat("dd/MM/yyyy").parse(booked_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            boolean result = isDateInCurrentWeek(date);
            System.out.println(targetWeek);
            if (result == true || result == false) {
                if (week_no_and_number_of_appointment.containsKey(targetWeek)) {
                    week_no_and_number_of_appointment.put(targetWeek, week_no_and_number_of_appointment.get(targetWeek) + 1);
                    System.out.println(week_no_and_number_of_appointment);
                } else {
                    System.out.println("else");
                    week_no_and_number_of_appointment.put(targetWeek, 1);
                    System.out.println(week_no_and_number_of_appointment);
                }
            }
        }
        if (can) {
            if (outer.containsKey(appointmentType)) {
                String date_raw = appointmentID.split(",")[0].trim().substring(4, 10);
                String date_final = date_raw.substring(0, 2) + "/" + date_raw.substring(2, 4) + "/20" + date_raw.substring(4, 6);
                Date date = null;
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy").parse(date_final);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                isDateInCurrentWeek(date);
                System.out.println("new target week" + targetWeek);
                slots = outer.get(appointmentType).get(appointmentID);
                if (slots >= 1) {
                    int tmp = slots - 1;
                    System.out.println(aList + "Inner");
                    if (aList.contains(appointmentID + "," + appointmentType + "," + patient_Id)) {
                        msg = "You have already booked appointment with this appointment type,appointment id and patient id...";
                    } else if (week_no_and_number_of_appointment.containsKey(targetWeek)) {
                        if (week_no_and_number_of_appointment.get(targetWeek) >= 3) {
                            msg = "You can not book more than three appointment in different server...";
                        } else {
                            System.out.println("Inside Logic");
                            HashMap<String, Integer> inner = outer.get(appointmentType);
                            inner.put(appointmentID, tmp);
                            aList.add(appointmentID + "," + appointmentType + "," + patient_Id);
                            System.out.println("List after add" + aList);
                            //User already book appointment within this server for specific type
                            msg = "Appointment Booked....";
                        }
                    } else {
                        System.out.println("Inside Logic");
                        HashMap<String, Integer> inner = outer.get(appointmentType);
                        inner.put(appointmentID, tmp);
                        aList.add(appointmentID + "," + appointmentType + "," + patient_Id);
                        System.out.println("List after add" + aList);
                        //User already book appointment within this server for specific type
                        msg = "Appointment Booked....";
                    }
                } else {
                    msg = "There is no slot available for your appointment ID...";
                }
            } else {
                msg = "please enter appropriate appointment ID!!!";
            }
        } else {
            msg = "You can not book more than one appointment in a day with same type...";
        }
        return msg.trim();
    }
    public synchronized String book_appointment(String patient_Id, String appointmentID, String appointmentType) {
        String result_mtl_appointments = "";
        String result_que_appointments = "";
        String value12 = "";
        String x = appointmentID.substring(0, 3);
        boolean can = true;
        for (int i = 0; i < aList.size(); i++) {
            //str = MTLM110222,Physician,QUEA1234
            String str = String.valueOf(aList.get(i));
            String date_raw = str.split(",")[0].trim().substring(4, 10);
            String type = str.split(",")[1].trim();
            String user_id = str.split(",")[2].trim();
            System.out.println(type);
            System.out.println(user_id);
            System.out.println(date_raw);
            String date_raw_patient_entered = appointmentID.split(",")[0].trim().substring(4, 10);
            if (patient_Id.equals(user_id) && appointmentType.equals(type) && date_raw_patient_entered.equals(date_raw)) {
                can = false;
            }
        }
        if (x.equals("SHE")) {
            //Logic SHE
            if (can) {
                if (outer.containsKey(appointmentType)) {
                    slots = outer.get(appointmentType).get(appointmentID);
                    if (slots >= 1) {
                        int tmp = slots - 1;
                        if (aList.contains(appointmentID + "," + appointmentType + "," + patient_Id)) {
                            msg = "You have already booked appointment with this appointment type,appointment id and patient id...";
                        } else {
                            HashMap<String, Integer> inner = outer.get(appointmentType);
                            inner.put(appointmentID, tmp);
                            aList.add(appointmentID + "," + appointmentType + "," + patient_Id);
                            //User already book appointment within this server for specific type
                            msg = "Appointment Booked....";
                        }
                    } else {
                        msg = "There is no slot available for your appointment ID...";
                    }
                } else {
                    msg = "please enter appropriate appointment ID!!!";
                }
            } else {
                msg = "You can not book more than one appointment in a day with same type...";
            }
        } else if (x.equals("MTL")) {
            //Logic MTL
            HashMap<String, String> map = new HashMap<>();
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id + "," + appointmentID + "," + appointmentType;
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 678;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_book = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_book);
                result_mtl_appointments = new String(reply_book.getData()).trim();
                msg = result_mtl_appointments;
            } catch (SocketException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (x.equals("QUE")) {
            //Logic SHE
            HashMap<String, String> map = new HashMap<>();
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id + "," + appointmentID + "," + appointmentType;
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 456;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_book = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_book);
                result_que_appointments = new String(reply_book.getData()).trim();
                msg = result_que_appointments;
            } catch (SocketException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return msg.trim();
    }
    String result_she = "";
    String result_she_que = "";
    String result_mtl = "";
    public synchronized String get_appointment_schedule(String patient_Id) {
        System.out.println("aList" + aList);
        System.out.println(patient_Id + "patient_Id");
        aList_add.clear();
        String mainstr = aList.toString();
        if (mainstr.contains(patient_Id)) {
            List<String> values = aList;
            System.out.println(values + "values");
            List<Integer> indexes = values.stream().filter(v -> v.contains(patient_Id)).map(v -> values.indexOf(v))
                    .collect(Collectors.toList());
            System.out.println(indexes + "indexed");
//            int pass_index = indexes.indexOf(0);
            for (int i = 0; i < indexes.size(); i++) {
                String x = (String) aList.get(i);
                aList_add.add(x);
            }
            result_she = aList_add.toString();
        }
        //Logic QUE
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patient_Id;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1111;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_get_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_get_scheduled);
            if (!(new String(reply_get_scheduled.getData()).trim().isEmpty()) || !(result_she.trim().isEmpty())) {
                result_she_que = new String(reply_get_scheduled.getData()).trim() + "\n" + result_she.trim();
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic MTL
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patient_Id;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1112;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_get_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_get_scheduled);
            result_mtl = new String(reply_get_scheduled.getData()).trim();
            if (!(result_mtl.trim().isEmpty()) || !(result_she_que.trim().isEmpty())) {
                msg1 = result_mtl.trim() + "\n" + result_she_que.trim();
            } else {
                msg1 = "No scheduled Appointment...";
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg1.trim();
    }
    public synchronized String get_she_schedule_data(String patient_Id) {
        aList_add.clear();
        String mainstr = aList.toString();
        if (mainstr.contains(patient_Id)) {
            List<String> values = aList;
            System.out.println(values + "values");
            List<Integer> indexes = values.stream().filter(v -> v.contains(patient_Id)).map(v -> values.indexOf(v))
                    .collect(Collectors.toList());
            System.out.println(indexes + "indexed");
            for (int i = 0; i < indexes.size(); i++) {
                String x = (String) aList.get(i);
                aList_add.add(x);
            }
//            msg1 = aList_add.toString();
        }
        return aList.toString();
    }
    String msg2 = "";
    public synchronized String get_she_cancel_scheduled_data(String patientID, String appointmentID, String appointmentType) {
        String mainstr = aList.toString();
        if (mainstr.contains(patientID) && mainstr.contains(appointmentID)) {
            List<String> values = aList;
            List<Integer> indexes = values.stream().filter(v -> v.contains(patientID) && v.contains(appointmentID)).map(v -> values.indexOf(v))
                    .collect(Collectors.toList());
            int pass_index = indexes.indexOf(0);
            String y = (String) aList.get(pass_index);
            aList.remove(y);
            msg2 = "Appointment Cancelled...";
            int tmp = outer.get(appointmentType).get(appointmentID);
            HashMap<String, Integer> inner = outer.get(appointmentType);
            inner.put(appointmentID, tmp + 1);
        }
        return msg2;
    }
    String result_she_que_mtl;
    String result_que;
    public synchronized String cancel_appointment(String patientID, String appointmentID, String appointmentType) {
        String result_mtl_cancel_scheduled = "";
        String result_que_cancel_scheduled = "";
        String mainstr = aList.toString();
        if (mainstr.contains(patientID) && mainstr.contains(appointmentID)) {
            List<String> values = aList;
            List<Integer> indexes = values.stream().filter(v -> v.contains(patientID) && v.contains(appointmentID)).map(v -> values.indexOf(v))
                    .collect(Collectors.toList());
            int pass_index = indexes.indexOf(0);
            String y = (String) aList.get(pass_index);
            aList.remove(y);
            result_she = "Appointment Cancelled...";
            int tmp = outer.get(appointmentType).get(appointmentID);
            HashMap<String, Integer> inner = outer.get(appointmentType);
            inner.put(appointmentID, tmp + 1);
        }
        //Logic MTL
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patientID + "," + appointmentID + "," + appointmentType;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1950;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_cancel_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_cancel_scheduled);
            if (!(new String(reply_cancel_scheduled.getData()).trim().isEmpty()) || !(result_she.trim().isEmpty())) {
                result_she_que_mtl = new String(reply_cancel_scheduled.getData()).trim() + "\n" + result_she.trim();
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic QUE
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patientID + "," + appointmentID + "," + appointmentType;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1949;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_cancel_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_cancel_scheduled);
            result_que = new String(reply_cancel_scheduled.getData());
            if (!(result_que.trim().isEmpty()) || !(result_she_que_mtl.trim().isEmpty())) {
                msg2 = result_que.trim() + "\n" + result_she_que_mtl.trim();
            } else {
                msg2 = "There is no such slot booked..";
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg2.trim();
    }
    String msg4 = "";
    public synchronized String get_she_remove_appointment_data(String appointmentID, String appointmentType, String adminID) {
        if (outer.containsKey(appointmentType)) {
            outer.remove(appointmentType);
            String result = outer.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            msg4 = "Appointment Remove Successfully!!!";
        }
        return msg4;
    }
    public synchronized String remove_appointment(String appointmentID, String appointmentType, String adminID) {
        if (outer.containsKey(appointmentType)) {
            outer.remove(appointmentType);
            String result = outer.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            result_she = "Appointment Remove Successfully!!!";
        }
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = appointmentID + "," + appointmentType + "," + adminID;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 2021;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_remove_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_remove_scheduled);
            if (!(new String(reply_remove_scheduled.getData()).trim().isEmpty()) || !(result_she.trim().isEmpty())) {
                result_she_que = new String(reply_remove_scheduled.getData()).trim() + "\n" + result_she.trim();
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic MTL
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = appointmentID + "," + appointmentType + "," + adminID;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 2022;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_remove_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_remove_scheduled);
            result_mtl = new String(reply_remove_scheduled.getData()).trim();
            if (!(result_mtl.trim().isEmpty()) || !(result_she_que.trim().isEmpty())) {
                msg4 = result_mtl.trim() + "\n" + result_she_que.trim();
            } else {
                msg4 = "No records found!!!";
            }
        } catch (SocketException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg4.trim();
    }
    public synchronized String sayHello() {
        return null;
    }
    public void shutdown() {
    }
    public synchronized String swap_appointment(String patient_Id, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) {
        String x = old_appointmentID.substring(0, 3);
        System.out.println(aList + "aList_swap");
        if (x.contains("SHE")) {
            if (aList.contains(old_appointmentID.trim() + "," + old_appointmentType.trim() + "," + patient_Id.trim())) {
                System.out.println("Inside Swap");
                String cancel_response = cancel_appointment(patient_Id, old_appointmentID, old_appointmentType);
                if (cancel_response.equals("Appointment Cancelled...")) {
                    String response_swap = book_appointment(patient_Id, new_appointmentID, new_appointmentType);
//                msg = response_swap;
                    if (response_swap.equals("Appointment Booked....")) {
                        msg = "Appointment Swapped Successfully...";
                    } else {
                        String old_book_again = book_appointment(patient_Id, old_appointmentID, old_appointmentType);
                        msg = old_book_again;
                    }
                } else {
                    msg = "Swap Appointment Not Possible";
                }
            } else {
                msg = "You haven't booked any appointment yet OR You have entered incorrect input";
            }
        } else if (x.contains("QUE")) {
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id.trim() + "," + old_appointmentID.trim() + "," + old_appointmentType.trim() + "," + new_appointmentID.trim() + "," + new_appointmentType.trim();
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 3021;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_swap_scheduled1 = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_swap_scheduled1);
                msg = new String(reply_swap_scheduled1.getData()).trim();
            } catch (SocketException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (x.contains("MTL")) {
            //Logic SHE
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id.trim() + "," + old_appointmentID.trim() + "," + old_appointmentType.trim() + "," + new_appointmentID.trim() + "," + new_appointmentType.trim();
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 3022;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_swap_scheduled = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_swap_scheduled);
                msg = new String(reply_swap_scheduled.getData()).trim();
            } catch (SocketException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(she_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            msg = "Incorrect Input.";
        }
        return msg;
    }
    public synchronized String get_she_swap_appointment_data(String patient_Id, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) {
        if (aList.contains(old_appointmentID.trim() + "," + old_appointmentType.trim() + "," + patient_Id.trim())) {
            System.out.println("Inside Swap");
            String cancel_response = cancel_appointment(patient_Id, old_appointmentID, old_appointmentType);
            if (cancel_response.equals("Appointment Cancelled...")) {
                String response_swap = book_appointment(patient_Id, new_appointmentID, new_appointmentType);
                if (response_swap.equals("Appointment Booked....")) {
                    msg = "Appointment Swapped Successfully...";
                } else {
                    String old_book_again = book_appointment(patient_Id, old_appointmentID, old_appointmentType);
                    msg = old_book_again;
                }
            } else {
                msg = "Swap Appointment Not Possible";
            }
        } else {
            msg = "You haven't booked any appointment yet OR You have entered incorrect input";
        }
        return msg;
    }
    public static boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        System.out.println(date);
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return week == targetWeek && year == targetYear;
    }
    public String shutDown() throws RemoteException {
        outer = new HashMap<>();
        aList = new ArrayList();
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
}
