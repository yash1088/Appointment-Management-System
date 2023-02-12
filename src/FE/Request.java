package FE;
import java.io.Serializable;
import java.util.ArrayList;
/**
 * @author Pratik Gondaliya
 * @created 30/03/2022
 */
public class Request implements Serializable {
    private String function = "null";
    private String clientID = "null";
    private String AppointmentType = "null";
    private String OldAppointmentType = "null";
    private String AppointmentID = "null";
    private String OldAppointmentID = "null";
    private String FeIpAddress = FrontEnd.FE_IP_Address;
    private int bookingCapacity = 0;
    private int sequenceNumber = 0;
    private String MessageType = "00";
    private int retryCount = 1;
    public ArrayList<String> getData() {
        return Data;
    }
    public void setData(ArrayList<String> data) {
        Data = data;
    }
    private ArrayList<String> Data;
    public Request(String function, String clientID) {
        setFunction(function);
        setClientID(clientID);
    }

    public Request(int rmNumber, String bugType) {
        setMessageType(bugType + rmNumber);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getAppointmentType() {
        return AppointmentType;
    }

    public void setAppointmentType(String AppointmentType) {
        this.AppointmentType = AppointmentType;
    }

    public String getOldAppointmentType() {
        return OldAppointmentType;
    }

    public void setOldAppointmentType(String OldAppointmentType) {
        this.OldAppointmentType = OldAppointmentType;
    }

    public String getAppointmentID() {
        return AppointmentID;
    }

    public void setAppointmentID(String AppointmentID) {
        this.AppointmentID = AppointmentID;
    }

    public String getOldAppointmentID() {
        return OldAppointmentID;
    }

    public void setOldAppointmentID(String OldAppointmentID) {
        this.OldAppointmentID = OldAppointmentID;
    }

    public int getBookingCapacity() {
        return bookingCapacity;
    }

    public void setBookingCapacity(int bookingCapacity) {
        this.bookingCapacity = bookingCapacity;
    }

    public String noRequestSendError() {
        return "request: " + getFunction() + " from " + getClientID() + " not sent";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFeIpAddress() {
        return FeIpAddress;
    }

    public void setFeIpAddress(String feIpAddress) {
        FeIpAddress = feIpAddress;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public boolean haveRetries() {
        return retryCount > 0;
    }

    public void countRetry() {
        retryCount--;
    }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addEvent,...);userID; newAppointmentID;newAppointmentType; oldAppointmentID; oldAppointmentType;bookingCapacity
    @Override
    public String toString() {
        return getSequenceNumber() + ";" +
                getFeIpAddress().toUpperCase() + ";" +
                getMessageType().toUpperCase() + ";" +
                getFunction().toUpperCase() + ";" +
                getClientID().toUpperCase() + ";" +
                getAppointmentID().toUpperCase() + ";" +
                getAppointmentType().toUpperCase() + ";" +
                getOldAppointmentID().toUpperCase() + ";" +
                getOldAppointmentType().toUpperCase() + ";" +
                getBookingCapacity();
    }
}
