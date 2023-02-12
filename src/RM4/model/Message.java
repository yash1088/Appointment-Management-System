package RM4.model;

public class Message {
	public String FrontIpAddress,Function , MessageType, patientID, newAppointmentID, newAppointmentType, oldAppointmentID, oldAppointmentType;
	public int bookingCapacity, sequenceId;

	public Message(int sequenceId, String frontIpAddress, String messageType, String function,  String patientID, String newAppointmentID, String newAppointmentType, String oldAppointmentID, String oldAppointmentType, int bookingCapacity) {

		FrontIpAddress = frontIpAddress;
		Function = function;
		MessageType = messageType;
		this.patientID = patientID;
		this.newAppointmentID = newAppointmentID;
		this.newAppointmentType = newAppointmentType;
		this.oldAppointmentID = oldAppointmentID;
		this.oldAppointmentType = oldAppointmentType;
		this.bookingCapacity = bookingCapacity;
		this.sequenceId = sequenceId;
	}

	public String getFrontIpAddress() {
		return FrontIpAddress;
	}

	public void setFrontIpAddress(String frontIpAddress) {
		FrontIpAddress = frontIpAddress;
	}

	public String getFunction() {
		return Function;
	}

	public void setFunction(String function) {
		Function = function;
	}

	public String getMessageType() {
		return MessageType;
	}

	public void setMessageType(String messageType) {
		MessageType = messageType;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getNewAppointmentID() {
		return newAppointmentID;
	}

	public void setNewAppointmentID(String newAppointmentID) {
		this.newAppointmentID = newAppointmentID;
	}

	public String getNewAppointmentType() {
		return newAppointmentType;
	}

	public void setNewAppointmentType(String newAppointmentType) {
		this.newAppointmentType = newAppointmentType;
	}

	public String getOldAppointmentID() {
		return oldAppointmentID;
	}

	public void setOldAppointmentID(String oldAppointmentID) {
		this.oldAppointmentID = oldAppointmentID;
	}

	public String getOldAppointmentType() {
		return oldAppointmentType;
	}

	public void setOldAppointmentType(String oldAppointmentType) {
		this.oldAppointmentType = oldAppointmentType;
	}

	public int getBookingCapacity() {
		return bookingCapacity;
	}

	public void setBookingCapacity(int bookingCapacity) {
		this.bookingCapacity = bookingCapacity;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	@Override
	public String toString() {
		return sequenceId + ";" + FrontIpAddress + ";" +MessageType + ";" +Function + ";" +patientID + ";" +newAppointmentID +
				";" +newAppointmentType + ";" +oldAppointmentID + ";" +oldAppointmentType + ";" +bookingCapacity;
	}
}
