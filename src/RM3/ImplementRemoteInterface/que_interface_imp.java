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

public class que_interface_imp extends UnicastRemoteObject implements Main_interface {
    static HashMap<String, HashMap<String, Integer>> outer = null;
    static HashMap<String, Integer> inner = null;
    static HashMap<String, HashMap<String, Integer>> list_availability_outer = null;
    static HashMap<String, String> map = null;
    static List aList = null;
    List temp_aList = null;
    static List aList_add = null;
    static int year = 0;
    static int week = 0;
    static int targetWeek = 0;
    String str = "";
    public que_interface_imp() throws RemoteException, IOException {
        outer = new HashMap<>();
        inner = new HashMap<>();
        list_availability_outer = new HashMap<>();
//        inner.put("QUEM150422", 4);
//        outer.put("Physician", inner);
//        System.out.println(outer);
//        inner = new HashMap<>();
//        inner.put("QUEM080422", 4);
//        outer.put("Surgeon", inner);
//        inner = new HashMap<>();
//        inner.put("QUEM090422", 4);
//        outer.put("Dental", inner);
        map = new HashMap<>();
        aList = new ArrayList();
//        aList.add("QUEM210222,Surgeon,QUEP1234");
//        aList.add("QUEA200222,Physician,MTLP1234");
        temp_aList = new ArrayList();
        aList_add = new ArrayList();
        temp_aList = new ArrayList();
    }
    // Implementing the interface methods
    public boolean authenticate(String client_userName, String client_password) {
        if ((client_userName != null && !client_userName.isEmpty()) && (client_password != null && !client_password.isEmpty())) {
            if ((client_userName.equalsIgnoreCase("userid")) && (client_password.equalsIgnoreCase("password"))) {
                return true;
            }
        }
        return false;
    }
    //QUE Admin
    // Implementing the interface method for que_admin_client
    public synchronized String printMsg(String msg) {
        return msg;
    }
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
        String result_que_she = "";
        int slots = 0;
        String result_book_appointment = "";
        HashMap<String, Integer> store = new HashMap<>();
        if (outer.containsKey(appointment_type)) {
            store = outer.get(appointment_type);
        }
        //UDP Client side code for Montreal
        HashMap<String, String> map = new HashMap<>();
        try (DatagramSocket aSocket = new DatagramSocket()) {
            String message = appointment_type;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 111;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            System.out.println(new String(request.getData()));
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            System.out.println(new String(reply.getData()) + "replyfrommontreal");
            if (!(new String(reply.getData()).trim().isEmpty()) || store.size() > 0) {
                result = store.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));
                result_mtl_que = new String(reply.getData()).trim() + "\n" + result;
                System.out.println(new String(reply.getData()) + "montreal");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //UDP Client side code for Sherbrooke
        try (DatagramSocket aSocket = new DatagramSocket()) {
            String message = appointment_type;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 112;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            if (!(new String(reply.getData()).trim().isEmpty()) || result_mtl_que != null) {
                String value = new String(reply.getData()).trim() + "\n" + result_mtl_que.trim();
                map_final = value;
                System.out.println(map_final + "Hyymap_final");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map_final;
    }
    public synchronized HashMap<String, Integer> get_que_server_data(String appointment_type) {
        HashMap<String, Integer> que_response = new HashMap<String, Integer>();
        if (outer.containsKey(appointment_type)) {
            que_response = outer.get(appointment_type);
        }
        return que_response;
    }
    int slots = 0;
    String msg = "";
    String msg1 = "";
    String msg2 = "";
    String msg3 = "";
    String msg4 = "";
    public synchronized String book_appointment(String patient_Id, String appointmentID, String appointmentType) {
        String result_mtl_appointments = "";
        String result_she_appointments = "";
        String value12 = "";
        HashMap<String, String> map_mtl_appointment = new HashMap<>();
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
        if (x.equals("QUE")) {
            System.out.println("Inside Quebec");
            //Logic Quebec
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
            System.out.println("Inside Montreal");
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
                String result_mtl_appointments1 = new String(reply_book.getData()).trim();
                msg = result_mtl_appointments1;
                System.out.println("msg_mtl_response_in_que" + msg);
            } catch (SocketException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (x.equals("SHE")) {
            System.out.println("Inside Sherbrooke");
            //Logic SHE
            HashMap<String, String> map = new HashMap<>();
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id + "," + appointmentID + "," + appointmentType;
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 789;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_book = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_book);
                result_she_appointments = new String(reply_book.getData()).trim();
                msg = result_she_appointments;
            } catch (SocketException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return msg.trim();
    }
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
            System.out.println(parts[x].substring(0, 3));
            if (parts[x + 2].substring(0, 3).contains("QUE")) {
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
    String result_que = "";
    String result_mtl_que = "";
    String result_she = "";
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
            for (int i = 0; i < indexes.size(); i++) {
                String x = (String) aList.get(i);
                aList_add.add(x);
            }
            result_que = aList_add.toString();
            System.out.println("result of quebec local" + result_que + ".....");
        }
        //Logic MTL
        HashMap<String, String> map = new HashMap<>();
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
            DatagramPacket reply_get_scheduled1 = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_get_scheduled1);
            if (!(new String(reply_get_scheduled1.getData()).trim().isEmpty()) || !(result_que.trim().isEmpty())) {
                result_mtl_que = new String(reply_get_scheduled1.getData()).trim() + "\n" + result_que.trim();
                System.out.println(result_que + "result_que");
                System.out.println(new String(reply_get_scheduled1.getData()).trim() + "result_mtl");
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic SHE
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patient_Id;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1113;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_get_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_get_scheduled);
            result_she = new String(reply_get_scheduled.getData());
            if (!(result_she.trim().isEmpty()) || !(result_mtl_que.trim().isEmpty())) {
                msg1 = result_she.trim() + "\n" + result_mtl_que.trim();
                System.out.println(msg1 + "msg1");
            } else {
                msg1 = "No scheduled Appointment...";
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg1.trim();
    }
    public synchronized String get_que_scheduled_data(String patient_Id) {
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
        }
        return aList.toString();
    }
    public synchronized String cancel_appointment(String patientID, String appointmentID, String appointmentType) {
        System.out.println("List before cancel" + aList);
        String mainstr = aList.toString();
        if (mainstr.contains(patientID) && mainstr.contains(appointmentID) && mainstr.contains(appointmentType)) {
            System.out.println("Inside");
            List<String> values = aList;
            List<String> indexes = values.stream().filter(v -> v.contains(patientID) && v.contains(appointmentID) && v.contains(appointmentType))
                    .collect(Collectors.toList());
            System.out.println(indexes + "indexes");
            for (String v : indexes) {
                aList.remove(v);
            }
            result_que = "Appointment Cancelled...";
            int tmp = outer.get(appointmentType).get(appointmentID);
            HashMap<String, Integer> inner = outer.get(appointmentType);
            inner.put(appointmentID, tmp + 1);
        }
        //Logic MTL
        HashMap<String, String> map = new HashMap<>();
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
            DatagramPacket reply_cancel_scheduled1 = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_cancel_scheduled1);
            if (!(new String(reply_cancel_scheduled1.getData()).trim().isEmpty()) || !(result_que.trim().isEmpty())) {
                result_mtl_que = new String(reply_cancel_scheduled1.getData()).trim() + "\n" + result_que.trim();
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic SHE
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = patientID + "," + appointmentID + "," + appointmentType;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 1951;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_cancel_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_cancel_scheduled);
            result_she = new String(reply_cancel_scheduled.getData());
            if (!(result_she.trim().isEmpty()) || !(result_mtl_que.trim().isEmpty())) {
                msg2 = result_she.trim() + "\n" + result_mtl_que.trim();
            } else {
                msg2 = "There is no such slot booked..";
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg2.trim();
    }
    public synchronized String get_que_cancel_scheduled_data(String patientID, String appointmentID, String appointmentType) {
        String mainstr = aList.toString();
        System.out.println(mainstr + "outside");
        if (mainstr.contains(patientID) && mainstr.contains(appointmentID)) {
            System.out.println("Inside");
            List<String> values = aList;
            List<String> indexes = values.stream().filter(v -> v.contains(patientID) && v.contains(appointmentID) && v.contains(appointmentType))
                    .collect(Collectors.toList());
            System.out.println(indexes + "indexes");
            for (String v : indexes) {
                aList.remove(v);
            }
            msg2 = "Appointment Cancelled...";
            int tmp = outer.get(appointmentType).get(appointmentID);
            HashMap<String, Integer> inner = outer.get(appointmentType);
            inner.put(appointmentID, tmp + 1);
        }
        return msg2;
    }
    public synchronized String get_que_remove_appointment_data(String appointmentID, String appointmentType, String adminID) {
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
            result_que = "Appointment Remove Successfully!!!";
        }
        //Logic MTL
        HashMap<String, String> map = new HashMap<>();
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
            if (!(new String(reply_remove_scheduled.getData()).trim().isEmpty()) || !(result_que.trim().isEmpty())) {
                result_mtl_que = new String(reply_remove_scheduled.getData()).trim() + "\n" + result_que.trim();
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logic SHE
        try (DatagramSocket aSocket = new DatagramSocket()) {
            //Pass parameter in order to book appointment
            String message = appointmentID + "," + appointmentType + "," + adminID;
            byte[] m = message.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = 2023;
            DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
            // Blocking call
            aSocket.send(request);
            byte[] buffer = new byte[1500];
            DatagramPacket reply_remove_scheduled = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply_remove_scheduled);
            result_she = new String(reply_remove_scheduled.getData());
            if (!(result_she.trim().isEmpty()) || !(result_mtl_que.trim().isEmpty())) {
                msg4 = result_she.trim() + "\n" + result_mtl_que.trim();
            } else {
                msg4 = "No records found!!!";
            }
        } catch (SocketException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg4.trim();
    }
    public synchronized String swap_appointment(String patient_Id, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) {
        String x = old_appointmentID.substring(0, 3);
        System.out.println(aList + "aList_swap");
        if (x.contains("QUE")) {
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
        } else if (x.contains("MTL")) {
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
                DatagramPacket reply_swap_scheduled1 = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_swap_scheduled1);
                msg = new String(reply_swap_scheduled1.getData()).trim();
            } catch (SocketException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (x.contains("SHE")) {
            //Logic SHE
            try (DatagramSocket aSocket = new DatagramSocket()) {
                //Pass parameter in order to book appointment
                String message = patient_Id.trim() + "," + old_appointmentID.trim() + "," + old_appointmentType.trim() + "," + new_appointmentID.trim() + "," + new_appointmentType.trim();
                byte[] m = message.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 3023;
                DatagramPacket request = new DatagramPacket(m, message.length(), aHost, serverPort);
                // Blocking call
                aSocket.send(request);
                byte[] buffer = new byte[1500];
                DatagramPacket reply_swap_scheduled = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply_swap_scheduled);
                msg = new String(reply_swap_scheduled.getData()).trim();
            } catch (SocketException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(que_interface_imp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            msg = "Incorrect Input.";
        }
        return msg;
    }
    public synchronized String get_que_swap_appointment_data(String patient_Id, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) {
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
