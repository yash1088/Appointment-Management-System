package RM1.damsInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DamsInterface extends Remote {

	public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) throws RemoteException;

	public String helloWorld(String name) throws RemoteException;
	
	public String addAppointment (String appointmentID, String appointmentType, int capacity) throws RemoteException;
	
	public String removeAppointment (String appointmentID, String appointmentType) throws RemoteException;
	
	public String listAppointmentAvailability (String appointmentType) throws RemoteException;
	
	public String bookAppointment (String patientID, String appointmentID,String appointmentType) throws RemoteException;
	
	public String getAppointmentSchedule (String patientID) throws RemoteException;
	
	public String cancelAppointment (String patientID, String appointmentID) throws RemoteException;

	public String shutDown() throws RemoteException;
	 
}
