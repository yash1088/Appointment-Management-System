package Client;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * @author Krishna Patel
 * @created 06/04/2022
 */

public class Client {
    static FileWriter writer;
    private Client() {
    }

    static FE.SendData.SendData_interface servant;
    public static void main(String[] args) throws IOException {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            //FE
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            servant = FE.SendData.SendData_interfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }
        String result_city_str_patient = "";
        String result_identity_str_patient = "";
        String result_city_str_admin = "";
        String result_identity_str_admin = "";
        String main_str_patient = "";
        String main_str_admin = "";
        String str_server_segregation_patient = "";
        String str_server_segregation_admin = "";
        try {
            HashMap<String, HashMap<String, Integer>> new_hash_map = new HashMap<String, HashMap<String, Integer>>();
            Scanner scanner = new Scanner(System.in);
            //Admin or Patient
            System.out.println("Select your identity from below options \nPress 1: Patient (P) \nPress 2: Admin (A) ");
            int client_identity = scanner.nextInt();
            String options_identity = "";
            String options_cities_patient = "";
            String options_cities_admin = "";
            switch (client_identity) {
                case 1:
                    options_identity = "Patient (P)";
                    //Client's location (City)
                    options_cities_patient = select_server();
                    String city_str_patient = options_cities_patient;
                    result_city_str_patient = city_str_patient.substring(city_str_patient.indexOf("(") + 1, city_str_patient.indexOf(")"));
                    result_identity_str_patient = "P";
                    main_str_patient = result_city_str_patient + result_identity_str_patient;
                    str_server_segregation_patient = result_city_str_patient + result_identity_str_patient;
                    break;
                case 2:

                    options_identity = "Admin (A)";
                    //Client's location (City)
                    options_cities_admin = select_server();
                    String city_str_admin = options_cities_admin;
                    result_city_str_admin = city_str_admin.substring(city_str_admin.indexOf("(") + 1, city_str_admin.indexOf(")"));
                    result_identity_str_admin = "A";
                    main_str_admin = result_city_str_admin + result_identity_str_admin;
                    str_server_segregation_admin = result_city_str_admin + result_identity_str_admin;
                    System.out.println(str_server_segregation_admin);

                    break;
                default:
                    System.out.println("You have Entered Incorrect Option");
            }
            System.out.println(options_identity);

            if (main_str_patient.contains("P")) {
                System.out.println("You are an authorized Patient of Distributed Appointment Management System....");
                //Select Option
                while (true) {
                    System.out.println("Select From Below Options : \nPress 1: Book Appointment \nPress 2: Get Schedule Appointment \nPress 3: Cancel Appointment \nPress 4: Swap Appointment");
                    int select_patient_operations = scanner.nextInt();
                    String patient_roles = "";
                    switch (select_patient_operations) {
                        case 1:
                            patient_roles = "Book Appointment";
                            book_appointment(result_city_str_patient, result_identity_str_patient);
                            break;
                        case 2:
                            patient_roles = "Get Schedule Appointment";
                            get_schedule_appointment(result_city_str_patient, result_identity_str_patient);
                            break;
                        case 3:
                            patient_roles = "Cancel Appointment";
                            cancel_appointment(result_city_str_patient, result_identity_str_patient);
                            break;
                        case 4:
                            patient_roles = "Swap Appointment";
                            swap_appointment(result_city_str_patient, result_identity_str_patient);
                            break;
                        default:
                            System.out.println("You have Entered Incorrect Option");
                    }
                    System.out.println("Go again?");
                    String goAgain = scanner.next();
                    if (!goAgain.equals("y")) {
                        break;
                    }
                }
            }
            else {
                System.out.println("You are an authorized Admin of Distributed Appointment Management System...");
                //Select Option
                while (true) {
                    System.out.println("Select From Below Options : \nPress 1: Add Appointment \nPress 2: List Appointments Availability\nPress 3: Remove Appointment \nPress 4: Book Appointment \nPress 5: Get Schedule Appointment \nPress 6: Cancel Appointment\nPress 7: Swap Appointment\nPress 8: Fault\nPress 9: Crash\nPress 8: General");
                    int select_admin_operations = scanner.nextInt();
                    String admin_roles = "";
                    switch (select_admin_operations) {
                        case 1:
                            admin_roles = "Add Appointment";
                            String str_que_patient_admin = servant.add_appointment("Physician", "MTLM101010", 10,"MTLA1234");
                            System.out.println(str_que_patient_admin);
                            //add_appointment(result_city_str_admin, result_identity_str_admin);
                            break;

                        case 2:
                            admin_roles = "List Appointments Availability";
                            list_available_appointments(result_city_str_admin, result_identity_str_admin);
                            break;
                        case 3:
                            admin_roles = "Remove Appointment";
                            remove_appointment(result_city_str_admin, result_identity_str_admin);
                            break;
                        case 4:
                            admin_roles = "Book Appointment";
                            book_appointment(result_city_str_admin, result_identity_str_admin);
                            break;
                        case 5:
                            admin_roles = "Get Schedule Appointment";
                            get_schedule_appointment(result_city_str_admin, result_identity_str_admin);
                            break;
                        case 6:
                            admin_roles = "Cancel Appointment";
                            cancel_appointment(result_city_str_admin, result_identity_str_admin);
                            break;
                        case 7:
                            admin_roles = "Swap Appointment";
                            swap_appointment(result_city_str_admin, result_identity_str_admin);
                            break;

                        case 8:
                            admin_roles = "Fault";
                            fault(result_city_str_admin,result_identity_str_admin);
                            break;

                        default:
                            System.out.println("You have Entered Incorrect Option");
                    }
                    System.out.println("Go again?");
                    String goAgain = scanner.next();
                    if (!goAgain.equals("y")) {
                        break;
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void fault(String result_city_str_admin, String result_identity_str_admin) {
        String Userid = get_input();
        String admin_Id = result_city_str_admin + result_identity_str_admin + Userid;
        String str_que_patient_admin = servant.fault(admin_Id);
        // String str_que_patient_admin = stub_que_patient_admin.printMsg("For Quebec Appointment Created");
        System.out.println(str_que_patient_admin);
        if(str_que_patient_admin.contains("restarting")){
            String str_que_patient_admin1 = servant.fault(admin_Id.concat("aa"));
            System.out.println(str_que_patient_admin1);
        }
        else {

        }

    }

    //Admin Operations
    static void add_appointment(String result_city_str_admin, String result_identity_str_admin) throws
            RemoteException, NotBoundException, IOException {
        String method_name = "Add Appointment";
        String options_appointment_time_slot = "";
        String options_appointment_type_admin = "";
        //Appointment Type Selection
        options_appointment_type_admin = select_appointment_type();
        Scanner scanner = new Scanner(System.in);
        //Taking User ID from user and creating user id
        String Userid = get_input();
        String admin_Id = result_city_str_admin + result_identity_str_admin + Userid;
        System.out.println(admin_Id + "  hyyy");//Select Appointment ID
        //For Creation of Appointment ID
        // Time Slot Creation

            System.out.println("Select From Below Options : \nPress 1: Morning (M) \nPress 2: Afternoon (A) \nPress 3: Evening (E)");
            int admin_slot_time = scanner.nextInt();
            switch (admin_slot_time) {
                case 1:
                    options_appointment_time_slot = "Morning (M)";
                    break;
                case 2:
                    options_appointment_time_slot = "Afternoon (A)";
                    break;
                case 3:
                    options_appointment_time_slot = "Evening (E)";
                    break;
                default:
                    System.out.println("You have Entered Incorrect Option");
            }
            System.out.println("You have selected: " + options_appointment_time_slot);

        //Creating Strings from user input for ID creation
        String time_stot_admin = options_appointment_time_slot;
        //time slot accronym for admin: result_time_slot_str_admin
        String result_time_slot_str_admin = time_stot_admin.substring(time_stot_admin.indexOf("(") + 1, time_stot_admin.indexOf(")"));
        //Date Selection for appointment slot creation
        System.out.println("Enter Date (ddmmyy) : ");
        String date = scanner.next();
        //Slot Capacity for the specifc time
        System.out.println("Enter Number of Slots : ");
        Integer slot_numbers = scanner.nextInt();
        String final_appointmentID_admin = result_city_str_admin + result_time_slot_str_admin + date;
        System.out.println(final_appointmentID_admin);
        if (result_city_str_admin.equals("QUE")) {
            System.out.println("QUE Server");
// Calling the remote method using the obtained object
            String str_que_patient_admin = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers,admin_Id);
            // String str_que_patient_admin = stub_que_patient_admin.printMsg("For Quebec Appointment Created");
            System.out.println(str_que_patient_admin);
            try {
                For_logs.client_logs(admin_Id, method_name, str_que_patient_admin);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType," + "capacity,";
            String server_name = "QUE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, str_que_patient_admin, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_admin.equals("MTL")) {
            System.out.println("MTL Server");
            // Calling the remote method using the obtained object
            String str_mtl_patient_admin = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers,admin_Id);
            // String str_que_patient_admin = stub_que_patient_admin.printMsg("For Quebec Appointment Created");
            System.out.println(str_mtl_patient_admin);
            try {
                For_logs.client_logs(admin_Id, method_name, str_mtl_patient_admin);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType," + "capacity,";
            String server_name = "MTL";
            try {
                For_logs.server_logs(admin_Id, method_name, params, str_mtl_patient_admin, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
            // Calling the remote method using the obtained object
            String str_she_patient_admin = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers,admin_Id);
            // String str_que_patient_admin = stub_que_patient_admin.printMsg("For Quebec Appointment Created");
            System.out.println(str_she_patient_admin);
            try {
                For_logs.client_logs(admin_Id, method_name, str_she_patient_admin);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType," + "capacity,";
            String server_name = "SHE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, str_she_patient_admin, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void list_available_appointments(String result_city_str_admin, String
            result_identity_str_admin) throws RemoteException, NotBoundException {
        String method_name = "List Availability";
        String options_availability_appointment = "";
        //List of available appointments
        //Appointment Type Selection
        options_availability_appointment = select_appointment_type();
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String admin_Id = result_city_str_admin + result_identity_str_admin + Userid;
        System.out.println(admin_Id + "  hyyy");
        if (result_city_str_admin.equals("QUE")) {
            System.out.println("QUE Server");
            // Method Invocation (this method will get available appointment for specific type
            String list_appointment_availability_que = servant.list_appointment_availability(options_availability_appointment,admin_Id);
            try {
                For_logs.client_logs(admin_Id, method_name, list_appointment_availability_que);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointType,";
            String server_name = "QUE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, list_appointment_availability_que, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list_appointment_availability_que == null) {
                System.out.println("No Data Found!!");
            } else {
                System.out.println(list_appointment_availability_que.replaceAll("[{}]", ""));
            }
        } else if (result_city_str_admin.equals("MTL")) {
            System.out.println("MTL Server");
            // Method Invocation (this method will get available appointment for specific type
            String list_appointment_availability_mtl = servant.list_appointment_availability(options_availability_appointment,admin_Id);
            try {
                For_logs.client_logs(admin_Id, method_name, list_appointment_availability_mtl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointType,";
            String server_name = "MTL";
            try {
                For_logs.server_logs(admin_Id, method_name, params, list_appointment_availability_mtl, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list_appointment_availability_mtl == null) {
                System.out.println("No Data Found!!");
            } else {
                System.out.println(list_appointment_availability_mtl.replaceAll("[{}]", ""));
            }
        } else {
            System.out.println("SHE Server");
// Method Invocation (this method will get available appointment for specific type
            String list_appointment_availability_she = servant.list_appointment_availability(options_availability_appointment,admin_Id);
            try {
                For_logs.client_logs(admin_Id, method_name, list_appointment_availability_she);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointType,";
            String server_name = "SHE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, list_appointment_availability_she, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list_appointment_availability_she == null) {
                System.out.println("No Data Found!!");
            } else {
                System.out.println(list_appointment_availability_she.replaceAll("[{}]", ""));
            }
        }
    }
    static void remove_appointment(String result_city_str_admin, String result_identity_str_admin) throws
            RemoteException, NotBoundException {
        String method_name = "Remove Appointment";
        String appointmentType = "";
        //List of scheduled appointment
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String admin_Id = result_city_str_admin + result_identity_str_admin + Userid;
        System.out.println("Your ID" + admin_Id);
        //Appointment Type Selection
        appointmentType = select_appointment_type();
        if (result_city_str_admin.equals("QUE")) {
            System.out.println("QUE Server");
//            if (list_appointment_availability == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Appointment ID:");
            String appointmentID = scanner.next();
            String response_que_remove = servant.remove_appointment(appointmentID, appointmentType, admin_Id);
            System.out.println(response_que_remove);
            try {
                For_logs.client_logs(admin_Id, method_name, response_que_remove);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType,";
            String server_name = "QUE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, response_que_remove, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_admin.equals("MTL")) {
            System.out.println("MTL Server");
//            if (list_appointment_availability == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Appointment ID:");
            String appointmentID = scanner.next();
            String response_mtl_remove = servant.remove_appointment(appointmentID, appointmentType, admin_Id);
            System.out.println(response_mtl_remove);
            try {
                For_logs.client_logs(admin_Id, method_name, response_mtl_remove);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType,";
            String server_name = "MTL";
            try {
                For_logs.server_logs(admin_Id, method_name, params, response_mtl_remove, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
//            if (list_appointment_availability == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Appointment ID:");
            String appointmentID = scanner.next();
            String response_she_remove = servant.remove_appointment(appointmentID, appointmentType, admin_Id);
            System.out.println(response_she_remove);
            try {
                For_logs.client_logs(admin_Id, method_name, response_she_remove);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "appointmentID," + "appointType,";
            String server_name = "SHE";
            try {
                For_logs.server_logs(admin_Id, method_name, params, response_she_remove, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //Patient Operations
    static void book_appointment(String result_city_str_patient, String result_identity_str_patient) throws
            RemoteException, NotBoundException, IOException {
        String serverResponse;
        String method_name = "Book Appointment";
        String response = "No Data Found For Quebec Sever";
        String appointmentType = "";
        //List of available appointments
        //Taking User ID from user and creating user id
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String patient_Id = result_city_str_patient + result_identity_str_patient + Userid;
        System.out.println(patient_Id + "  hyyy");
        //Appointment Type Selection
        appointmentType = select_appointment_type();
        if (result_city_str_patient.equals("QUE")) {
            System.out.println("QUE Server");
//            if (list_appointment_availability == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Wanted Appointment ID:");
            String appointmentID = scanner.next();
            String response_que_book_appointment = servant.book_appointment(patient_Id, appointmentID, appointmentType);
            System.out.println(response_que_book_appointment);
            try {
                For_logs.client_logs(patient_Id, method_name, response_que_book_appointment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID," + "appointType,";
            String server_name = "QUE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_que_book_appointment, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_patient.equals("MTL")) {
            System.out.println("MTL Server");
            // Method Invocation (this method will get available appointment for specific type
//            String list_appointment_availability_mtl = servant.list_appointment_availability(appointmentType);
//            if (list_appointment_availability_mtl == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability_mtl.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Wanted Appointment ID:");
            String appointmentID = scanner.next();
            serverResponse = servant.book_appointment(patient_Id, appointmentID, appointmentType);
//            String response_mtl_book_appointment = servant.book_appointment_mtl(patient_Id, appointmentID, appointmentType);
//            System.out.println(response_mtl_book_appointment);
            try {
//                For_logs.client_logs(patient_Id, method_name, response_mtl_book_appointment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID," + "appointType,";
            String server_name = "MTL";
            try {
//                For_logs.server_logs(patient_Id, method_name, params, response_mtl_book_appointment, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
//            if (list_appointment_availability == null) {
//                System.out.println("No Data Found!!");
//            } else {
//                System.out.println(list_appointment_availability.replaceAll("[{}]", ""));
//            }
            //From listed availability user will enter wanted appointment booking id
            System.out.println("Enter Wanted Appointment ID:");
            String appointmentID = scanner.next();
            String response_she_book_appointment = servant.book_appointment(patient_Id, appointmentID, appointmentType);
            System.out.println(response_she_book_appointment);
            try {
                For_logs.client_logs(patient_Id, method_name, response_she_book_appointment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID," + "appointType,";
            String server_name = "SHE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_she_book_appointment, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void get_schedule_appointment(String result_city_str_patient, String result_identity_str_patient) throws
            RemoteException, NotBoundException, IOException {
        String method_name = "Schedule Appointment";
        //List of scheduled appointment
        //Taking User ID from user and creating user id
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String patient_Id = result_city_str_patient + result_identity_str_patient + Userid;
        if (result_city_str_patient.equals("QUE")) {
            System.out.println("QUE Server");
            String response_que_get_schedule = servant.get_appointment_schedule(patient_Id);
            System.out.println(response_que_get_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_que_get_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID,";
            String server_name = "QUE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_que_get_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_patient.equals("MTL")) {
            System.out.println("MTL Server");
            String response_mtl_get_schedule = servant.get_appointment_schedule(patient_Id);
            System.out.println(response_mtl_get_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_mtl_get_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID,";
            String server_name = "MTL";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_mtl_get_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
            String response_she_get_schedule = servant.get_appointment_schedule(patient_Id);
            System.out.println(response_she_get_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_she_get_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID,";
            String server_name = "SHE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_she_get_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void cancel_appointment(String result_city_str_patient, String result_identity_str_patient) throws
            RemoteException, NotBoundException, IOException {
        String method_name = "Cancel Appointment";
        String appointmentType = "";
        //List of scheduled appointment
        //Taking User ID from user and creating user id
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String patient_Id = result_city_str_patient + result_identity_str_patient + Userid;
        System.out.println(patient_Id);

        //Appointment Type Selection
        appointmentType = select_appointment_type();
        System.out.println("Enter Appointment ID:");
        String appointmentID = scanner.next();
        if (result_city_str_patient.equals("QUE")) {
            System.out.println("QUE Server");
            String response_que_cancel_schedule = servant.cancel_appointment(patient_Id, appointmentID, appointmentType);
            System.out.println(response_que_cancel_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_que_cancel_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID" + "appointmentType";
            String server_name = "QUE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_que_cancel_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_patient.equals("MTL")) {
            System.out.println("MTL Server");
            String response_mtl_cancel_schedule = servant.cancel_appointment(patient_Id, appointmentID, appointmentType);
            System.out.println(response_mtl_cancel_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_mtl_cancel_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID," + "appointmentType";
            String server_name = "MTL";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_mtl_cancel_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
            String response_she_cancel_schedule = servant.cancel_appointment(patient_Id, appointmentID, appointmentType);
            System.out.println(response_she_cancel_schedule);
            try {
                For_logs.client_logs(patient_Id, method_name, response_she_cancel_schedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "appointmentID," + "appointmentType";
            String server_name = "SHE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, response_she_cancel_schedule, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void swap_appointment(String result_city_str_patient, String result_identity_str_patient) throws
            RemoteException, NotBoundException, IOException {
        String method_name = "Swap Appointment";
        String new_appointmentType = "";
        //List of scheduled appointment
        //Taking User ID from user and creating user id
        Scanner scanner = new Scanner(System.in);
        String Userid = get_input();
        String patient_Id = result_city_str_patient + result_identity_str_patient + Userid;
        System.out.println("Booked Appointments List\n");
        String response_que_get_schedule = servant.get_appointment_schedule(patient_Id);
        System.out.println(response_que_get_schedule);
        System.out.println("Enter your old appointment type from above List :");
        String old_appointmentType = scanner.next() + "\n ";
        if (old_appointmentType.isEmpty()){
            System.out.println("Please Enter appropriate Input");
        }
        System.out.println("Enter your old appointment ID from above List :");
        String old_appointmentID = scanner.next() + "\n ";
        if (old_appointmentID.isEmpty()){
            System.out.println("Please Enter appropriate Input");
        }
        System.out.println("Options to book available appointments from all different servers: \n");
        //Appointment Type Selection
        new_appointmentType = select_appointment_type();
        if (result_city_str_patient.equals("QUE")) {
            System.out.println("QUE Server");
            System.out.println("Enter your new appointment ID from above List :");
            String new_appointmentID = scanner.next() + "\n ";
            if (new_appointmentID.isEmpty()){
                System.out.println("Please Enter appropriate Input");
            }
            String swap_apo_que = servant.swap_appointment(patient_Id.trim(), old_appointmentID.trim(), old_appointmentType.trim(), new_appointmentID.trim(), new_appointmentType.trim());
            System.out.println(swap_apo_que);
            try {
                For_logs.client_logs(patient_Id, method_name, swap_apo_que);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "Old AppointmentID," + "Old AppointmentType," + "New AppointmentID," + "New AppointmentType";
            String server_name = "QUE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, swap_apo_que, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result_city_str_patient.equals("MTL")) {
            System.out.println("MTL Server");
            System.out.println("Enter your new appointment ID from above List :");
            String new_appointmentID = scanner.next() + "\n ";
            if (new_appointmentID.isEmpty()){
                System.out.println("Please Enter appropriate Input");
            }
            String swap_apo_mtl = servant.swap_appointment(patient_Id.trim(), old_appointmentID.trim(), old_appointmentType.trim(), new_appointmentID.trim(), new_appointmentType.trim());
            System.out.println(swap_apo_mtl);
            try {
                For_logs.client_logs(patient_Id, method_name, swap_apo_mtl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "Old AppointmentID," + "Old AppointmentType," + "New AppointmentID," + "New AppointmentType";
            String server_name = "MTL";
            try {
                For_logs.server_logs(patient_Id, method_name, params, swap_apo_mtl, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("SHE Server");
            System.out.println("Enter your new appointment ID from above List :");
            String new_appointmentID = scanner.next() + "\n ";
            if (new_appointmentID.isEmpty()){
                System.out.println("Please Enter appropriate Input");
            }
            String swap_apo_she = servant.swap_appointment(patient_Id.trim(), old_appointmentID.trim(), old_appointmentType.trim(), new_appointmentID.trim(), new_appointmentType.trim());
            System.out.println(swap_apo_she);
            try {
                For_logs.client_logs(patient_Id, method_name, swap_apo_she);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String params = "PatientID," + "Old AppointmentID," + "Old AppointmentType," + "New AppointmentID," + "New AppointmentType";
            String server_name = "SHE";
            try {
                For_logs.server_logs(patient_Id, method_name, params, swap_apo_she, server_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static String select_appointment_type() {
        String options_appointment_type = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select From Below Options : \nPress 1: Physician \nPress 2: Surgeon \nPress 3: Dental");
        int availability_appointment = scanner.nextInt();
        switch (availability_appointment) {
            case 1:
                options_appointment_type = "Physician";
                break;
            case 2:
                options_appointment_type = "Surgeon";
                break;
            case 3:
                options_appointment_type = "Dental";
                break;
            default:
                System.out.println("You have Entered Incorrect Option");
        }
        System.out.println("You have selected: " + options_appointment_type);

        return options_appointment_type;
    }
    private static String select_server() {
        String options_cities_patient = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select from below options : \nPress 1: Montreal (MTL) \nPress 2: Quebec (QUE) \nPress 3: Sherbrooke (SHE)");
        int client_city_patient = scanner.nextInt();
        switch (client_city_patient) {
            case 1:
                options_cities_patient = "Montreal (MTL)";
                break;
            case 2:
                options_cities_patient = "Quebec (QUE)";
                break;
            case 3:
                options_cities_patient = "Sherbrooke (SHE)";
                break;
            case 4:
                options_cities_patient = "Back";
                break;
            default:
                System.out.println("You have Entered Incorrect Option");
        }
        System.out.println("You have selected: " + options_cities_patient);

        return options_cities_patient;
    }
    private static String get_input() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the 4 digit unique number: ");
        String Userid = scanner.next();
        if (Userid.isEmpty()){
            System.out.println("Please Enter appropriate Input");
        }
        return Userid;
    }
}