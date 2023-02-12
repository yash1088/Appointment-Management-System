package RM3.Interface;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * @author Pratik Gondaliya
 * @created 06/04/2022
 */
public interface Main_interface extends Remote {
    //public String login_credentials(String str) throws RemoteException;

    public boolean authenticate(String client_userName, String client_password) throws RemoteException;

    //MTL Admin
    public String printMsg(String msg) throws RemoteException;

    //Appointment Creation
    public String add_appointment(String options_appointment_type_admin, String final_appointmentID_admin, int slot_numbers) throws RemoteException;

    //List of Appointment
    public String list_appointment_availability(String appointment_type) throws RemoteException;

    //Book appointment
    public String book_appointment(String patient_Id, String appointmentID, String appointmentType) throws RemoteException;

    //Get Scheduled Appointment
    public String get_appointment_schedule(String patient_Id) throws RemoteException;

    //Get Cancel Appointment
    public String cancel_appointment(String patient_Id, String appointmentID, String appointmentType) throws RemoteException;

    //Get Remove Appointment
    public String remove_appointment(String appointmentID, String appointmentType, String patientID) throws RemoteException;

    //Swap Appointment
    public String swap_appointment(String patientID, String old_appointmentID, String old_appointmentType, String new_appointmentID, String new_appointmentType) throws RemoteException;

    public String shutDown() throws RemoteException;
}
