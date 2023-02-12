package RM4.damsInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DamsInterface extends Remote {

		public String bookAppointment (String Uid, String apid, String Apt) throws RemoteException;

		public String GetAppointment(String Uid) throws RemoteException;
		public String CancleAppointment (String Uid, String apid) throws RemoteException;

		public String AddAppointment(String Apid, String apt, int slots) throws RemoteException;

		public String RemoveAppointment (String apid, String Apt) throws RemoteException;

		public String ListAppointment(String Apt) throws RemoteException;

		public String SwapAppointment(String Uid, String oApid, String oapt, String nApid, String napt) throws RemoteException;

	String shutDown() throws RemoteException;
}

