
package RM2.ImplementRemoteInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface MainInterface extends Remote{

    public String sendData() throws RemoteException;
    public String addappointment(String appid, String apptype, String capicity) throws RemoteException;

    public String bookappointment(String pid, String str) throws RemoteException;
    public String patientappointmentschedule(String pid) throws RemoteException;

    public String cancelPatientAppointment(String p_id,String cancel_id,String cancel_type) throws RemoteException;

    public String removeAppointment(String remove_id, String remove_type) throws RemoteException;

    public void invoke() throws RemoteException;

    public String listAppointment(String apptype) throws RemoteException;
    public String swapAppointment (String p_id, String oldAppID, String oldAppType,  String newAppID,
                            String newAppType) throws RemoteException;

    public String shutDown() throws RemoteException;

    public String fault(String adminid) throws RemoteException;
    
}
