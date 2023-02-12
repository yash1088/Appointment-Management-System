package Test.Client;

import FE.FrontEnd_Implementation;
import FE.FrontEnd_interface;
import RM4.damsInterface.DamsInterface;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Krishna Patel
 * @created 2022-04-07
 */

class ClientTest {
    static FE.SendData.SendData_interface servant;
    public static Registry registry_mtl_patient_admin;
    static DamsInterface DamsInterface = null;
    String options_appointment_type_admin = "Physician";
    String final_appointmentID_admin = "MTLM050822";
    int slot_numbers = 10;
    String admin_id = "MTLA1234";
    String old_appointmentID = "MTLM050822";
    String old_appointmentType = "Physician";
    String new_appointmentID = "MTLA060822";
    String new_appointmentType = "Physician";
    String patient_Id = "MTLP1234";


    @BeforeAll
    public static void setUp() throws Exception {
            System.out.println("Connecting to Server...");

            String[] args = {};
            ORB orb = ORB.init(args,null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            //FE
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            servant = FE.SendData.SendData_interfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));
            //ServerObjectInterface servant = ServerObjectInterfaceHelper.narrow(ncRef.resolve_str("MTL"));

            };

    @Test
    public void testAddAppointment() throws InterruptedException {
        Runnable task1 = () -> {
            System.out.println("Testing Start for AddAppointment");
            String Adata = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers, admin_id);
            String M1 = "Appointment added";
            assertEquals(M1, Adata);

        };

        Thread thread1 = new Thread(task1);
        thread1.start();

//        Runnable task2 = () -> {
//            String patient_Id = "MTLP4567";
//            // Admin_Test
//            System.out.println("Testing Start for AddAppointment");
//            String Adata = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers, admin_id);
//            String M1 = "Appointment added";
//            assertEquals(M1, Adata);
//
//            System.out.println("Testing Start for RemoveAppointment");
//            String Rdata = servant.remove_appointment(final_appointmentID_admin,options_appointment_type_admin,admin_id);
//                //System.out.println(Rdata);
//            String M2 = "Remove Successful";
//            assertEquals(M2,Rdata);
//
//            System.out.println("Testing Start for ListAppointment");
//            String Ldata = servant.list_appointment_availability(options_appointment_type_admin,admin_id);
//                //System.out.println(Ldata);
//            String M3 = "List Found";
//            assertEquals(M3,Ldata);
//
//            // Admin + Patient_Test
//            System.out.println("Testing Start for BookAppointment");
//            String Bdata = servant.book_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Bdata);
//            String M4 = "Appointment Booked";
//            assertEquals(M4, Bdata);
//
//            System.out.println("Testing Start for CancleAppointment");
//            String Cdata = servant.cancel_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Cdata);
//            String M5 = "Cancle Successful";
//            assertEquals(M5, Cdata);
//
//            System.out.println("Testing Start for GetAppointmentSchedule");
//            String Gdata = servant.get_appointment_schedule(patient_Id);
//                //System.out.println(Gdata);
//            String M6 = "Schedule Found";
//            assertEquals(M6, Gdata);
//
//            System.out.println("Testing Start for SwapAppointment");
//            String Sdata = servant.swap_appointment(patient_Id,old_appointmentID,old_appointmentType,new_appointmentID,new_appointmentType);
//                //System.out.println(Sdata);
//            String M7 = "Swap Successful";
//            assertEquals(M7, Sdata);
//        };
//
//        Runnable task3 = () -> {
//            String patient_Id = "MTLP7890";
//            // Admin_Test
//            System.out.println("Testing Start for AddAppointment");
//            String Adata = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers, admin_id);
//            String M1 = "Appointment added";
//            assertEquals(M1, Adata);
//
//            System.out.println("Testing Start for RemoveAppointment");
//            String Rdata = servant.remove_appointment(final_appointmentID_admin,options_appointment_type_admin,admin_id);
//                //System.out.println(Rdata);
//            String M2 = "Remove Successful";
//            assertEquals(M2,Rdata);
//
//            System.out.println("Testing Start for ListAppointment");
//            String Ldata = servant.list_appointment_availability(options_appointment_type_admin,admin_id);
//                //System.out.println(Ldata);
//            String M3 = "List Found";
//            assertEquals(M3,Ldata);
//
//            // Admin + Patient_Test
//            System.out.println("Testing Start for BookAppointment");
//            String Bdata = servant.book_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Bdata);
//            String M4 = "Appointment Booked";
//            assertEquals(M4, Bdata);
//
//            System.out.println("Testing Start for CancleAppointment");
//            String Cdata = servant.cancel_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Cdata);
//            String M5 = "Cancle Successful";
//            assertEquals(M5, Cdata);
//
//            System.out.println("Testing Start for GetAppointmentSchedule");
//            String Gdata = servant.get_appointment_schedule(patient_Id);
//                //System.out.println(Gdata);
//            String M6 = "Schedule Found";
//            assertEquals(M6, Gdata);
//
//            System.out.println("Testing Start for SwapAppointment");
//            String Sdata = servant.swap_appointment(patient_Id,old_appointmentID,old_appointmentType,new_appointmentID,new_appointmentType);
//                //System.out.println(Sdata);
//            String M7 = "Swap Successful";
//            assertEquals(M7, Sdata);
//        };
//
//        Runnable task4 = () -> {
//            String patient_Id = "MTLP0987";
//            // Admin_Test
//            System.out.println("Testing Start for AddAppointment");
//            String Adata = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers, admin_id);
//            String M1 = "Appointment added";
//            assertEquals(M1, Adata);
//
//            System.out.println("Testing Start for RemoveAppointment");
//            String Rdata = servant.remove_appointment(final_appointmentID_admin,options_appointment_type_admin,admin_id);
//                //System.out.println(Rdata);
//            String M2 = "Remove Successful";
//            assertEquals(M2,Rdata);
//
//            System.out.println("Testing Start for ListAppointment");
//            String Ldata = servant.list_appointment_availability(options_appointment_type_admin,admin_id);
//                //System.out.println(Ldata);
//            String M3 = "List Found";
//            assertEquals(M3,Ldata);
//
//            // Admin + Patient_Test
//            System.out.println("Testing Start for BookAppointment");
//            String Bdata = servant.book_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Bdata);
//            String M4 = "Appointment Booked";
//            assertEquals(M4, Bdata);
//
//            System.out.println("Testing Start for CancleAppointment");
//            String Cdata = servant.cancel_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Cdata);
//            String M5 = "Cancle Successful";
//            assertEquals(M5, Cdata);
//
//            System.out.println("Testing Start for GetAppointmentSchedule");
//            String Gdata = servant.get_appointment_schedule(patient_Id);
//                //System.out.println(Gdata);
//            String M6 = "Schedule Found";
//            assertEquals(M6, Gdata);
//
//            System.out.println("Testing Start for SwapAppointment");
//            String Sdata = servant.swap_appointment(patient_Id,old_appointmentID,old_appointmentType,new_appointmentID,new_appointmentType);
//                //System.out.println(Sdata);
//            String M7 = "Swap Successful";
//            assertEquals(M7, Sdata);
//        };
//
//        Runnable task5 = () -> {
//            String patient_Id = "MTLP7654";
//            // Admin_Test
//            System.out.println("Testing Start for AddAppointment");
//            String Adata = servant.add_appointment(options_appointment_type_admin, final_appointmentID_admin, slot_numbers, admin_id);
//            String M1 = "Appointment added";
//            assertEquals(M1, Adata);
//
//            System.out.println("Testing Start for RemoveAppointment");
//            String Rdata = servant.remove_appointment(final_appointmentID_admin,options_appointment_type_admin,admin_id);
//                //System.out.println(Rdata);
//            String M2 = "Remove Successful";
//            assertEquals(M2,Rdata);
//
//            System.out.println("Testing Start for ListAppointment");
//            String Ldata = servant.list_appointment_availability(options_appointment_type_admin,admin_id);
//                //System.out.println(Ldata);
//            String M3 = "List Found";
//            assertEquals(M3,Ldata);
//
//            // Admin + Patient_Test
//            System.out.println("Testing Start for BookAppointment");
//            String Bdata = servant.book_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Bdata);
//            String M4 = "Appointment Booked";
//            assertEquals(M4, Bdata);
//
//            System.out.println("Testing Start for CancleAppointment");
//            String Cdata = servant.cancel_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
//                //System.out.println(Cdata);
//            String M5 = "Cancle Successful";
//            assertEquals(M5, Cdata);
//
//            System.out.println("Testing Start for GetAppointmentSchedule");
//            String Gdata = servant.get_appointment_schedule(patient_Id);
//                //System.out.println(Gdata);
//            String M6 = "Schedule Found";
//            assertEquals(M6, Gdata);
//
//            System.out.println("Testing Start for SwapAppointment");
//            String Sdata = servant.swap_appointment(patient_Id,old_appointmentID,old_appointmentType,new_appointmentID,new_appointmentType);
//                //System.out.println(Sdata);
//            String M7 = "Swap Successful";
//            assertEquals(M7, Sdata);
//        };
    }

    @Test
    public void testRemoveAppointment() throws InterruptedException{
        Runnable task2 = () -> {
            System.out.println("Testing Start for RemoveAppointment");
            String Rdata = servant.remove_appointment(final_appointmentID_admin,options_appointment_type_admin,admin_id);
            // System.out.println(Rdata);
            String M2 = "Remove Successful";
            assertEquals(M2,Rdata);
        };
        Thread thread2 = new Thread(task2);
        thread2.start();
    }

    @Test
    public void testListAppointment() throws InterruptedException{
        Runnable task3 = () -> {
            System.out.println("Testing Start for ListAppointment");
            String Ldata = servant.list_appointment_availability(options_appointment_type_admin,admin_id);
            //System.out.println(Ldata);
            String M3 = "List Found";
            assertEquals(M3,Ldata);

        };
        Thread thread3 = new Thread(task3);
        thread3.start();
    }

    @Test
    public void testBookAppointment() throws InterruptedException{
        Runnable task4 = () -> {
            System.out.println("Testing Start for BookAppointment");
            String Bdata = servant.book_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
            //System.out.println(Bdata);
            String M4 = "Appointment Booked";
            assertEquals(M4, Bdata);
        };
        Thread thread4 = new Thread(task4);
        thread4.start();
    }

    @Test
    public void testCancleAppointment() throws InterruptedException{
        Runnable task5 = () -> {
            System.out.println("Testing Start for CancleAppointment");
            String Cdata = servant.cancel_appointment(patient_Id,final_appointmentID_admin,options_appointment_type_admin);
            //System.out.println(Cdata);
            String M5 = "Cancel Successful";
            assertEquals(M5, Cdata);

        };

        Thread thread5 = new Thread(task5);
        thread5.start();
    }

    @Test
    public void testGetAppointmentSchedule() throws InterruptedException{
        Runnable task6 = () -> {
            System.out.println("Testing Start for GetAppointmentSchedule");
            String Gdata = servant.get_appointment_schedule(patient_Id);
            //System.out.println(Gdata);
            String M6 = "Schedule Found";
            assertEquals(M6, Gdata);
        };
        Thread thread6 = new Thread(task6);
        thread6.start();
    }

    @Test
    public void testSwapAppointment() throws InterruptedException{
        Runnable task7 = () -> {
            System.out.println("Testing Start for SwapAppointment");
            String Sdata = servant.swap_appointment(patient_Id,old_appointmentID,old_appointmentType,new_appointmentID,new_appointmentType);
            //System.out.println(Sdata);
            String M7 = "Swap Successful";
            assertEquals(M7, Sdata);
        };
        Thread thread7 = new Thread(task7);
        thread7.start();

    }

    @AfterAll
    public static void endTest() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("End of testcase");
    }
}