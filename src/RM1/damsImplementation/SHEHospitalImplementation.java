/**
 * 
 */
package RM1.damsImplementation;

import RM1.config.Configuration;
import RM1.damsInterface.DamsInterface;
import RM1.model.AptDetails;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Sumit Monapara
 *
 */
public class SHEHospitalImplementation extends UnicastRemoteObject implements DamsInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HashMap<String, HashMap<String, AptDetails>> SHEdata = null;
	public HashMap<String, AptDetails> tmpMap = null;
	public List<String> patientList = null;
	DateTimeFormatter dateFormat = null;
	FileWriter myWriter = null;
	TimeUnit time = TimeUnit.SECONDS;
	public String que_result = "", mon_result = "";
	Calendar calender = null;

	/**
	 * @throws RemoteException
	 */
	public SHEHospitalImplementation() throws RemoteException {
		super();
		calender = Calendar.getInstance(Locale.CANADA);
		dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		SHEdata = new HashMap<>();
		loadData();
	}

	private void loadData() {
		tmpMap = new HashMap<>();

		patientList = new ArrayList<>();
		patientList.add("MTLP1234");
		patientList.add("SHEP0733");
		patientList.add("SHEP2345");
		tmpMap.put("SHEA020222", new AptDetails(patientList, 3));

		patientList = new ArrayList<>();
		patientList.add("SHEP1234");
		patientList.add("SHEP0733");
		patientList.add("MTLP2345");
		tmpMap.put("SHEM020222", new AptDetails(patientList, 4));

		patientList = new ArrayList<>();
		patientList.add("SHEP4567");
		tmpMap.put("SHEA100222", new AptDetails(patientList, 10));

		SHEdata.put("Physician", tmpMap);

		tmpMap = new HashMap<>();
		patientList = new ArrayList<>();
		patientList.add("SHEP1234");
		patientList.add("SHEP0733");
		patientList.add("SHEP2345");
		tmpMap.put("SHEE020222", new AptDetails(patientList, 4));
		SHEdata.put("Dental", tmpMap);
	}

	@Override
	public synchronized String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException {
		String result = "";
		String status = "";
		try {
			if (!(appointmentID.startsWith("SHE"))) {
				result = "Invalid appointmentID!!!";
				status = "failed";
			} else if (!SHEdata.containsKey(appointmentType)) {
				HashMap<String, AptDetails> tmp = new HashMap<>();
				tmp.put(appointmentID, new AptDetails(new ArrayList<>(), capacity));
				SHEdata.put(appointmentType, tmp);
				result = "Appointment added for " + appointmentType;
				status = "success";
			} else {
				if (SHEdata.get(appointmentType).containsKey(appointmentID)) {
					result = "Appointment is already present with the appointmentID!!!";
					status = "failed";
				} else {
					HashMap<String, AptDetails> tmp = SHEdata.get(appointmentType);
					tmp.put(appointmentID, new AptDetails(new ArrayList<>(), capacity));
					SHEdata.put(appointmentType, tmp);
					result = "Appointment added for " + appointmentType;
					status = "success";
				}
			}

			writeToLog("ADD APPOINTMENT", appointmentType + "-" + appointmentID + "-" + capacity, status, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public synchronized String removeAppointment(String appointmentID, String appointmentType) throws RemoteException {
		String result = "";
		boolean transfer = false;
		String status = "false";
		String log = "";
		int day = Integer.parseInt(appointmentID.substring(4, 6));
		int month = Integer.parseInt(appointmentID.substring(6, 8));

		if (!SHEdata.containsKey(appointmentType)) {
			result = "No Appointment is available for this type!!!";
			log = "No Appointment is available.";
			status = "failed";
		} else {
			HashMap<String, AptDetails> tmp = SHEdata.get(appointmentType);
			if (tmp.containsKey(appointmentID)) {
				if (tmp.get(appointmentID).getPatientID().size() == 0) {
					result = "Appointment found, no patient booked, cancelling it";
					SHEdata.get(appointmentType).remove(appointmentID);
					log = "Appointment deleted.";
					status = "success";
				} else {
					result = "Appointment found, patients have booked this appointment, Not found any date to assign the patients";
					for (Map.Entry<String, AptDetails> data : tmp.entrySet()) {
						if ((Integer.parseInt(data.getKey().substring(6, 8)) > month)
								|| (Integer.parseInt(data.getKey().substring(6, 8)) == month && (Integer.parseInt(data.getKey().substring(4, 6)) > day))) {
							if ((data.getValue().getCapacity() - data.getValue().getPatientID().size()) >= tmp
									.get(appointmentID).getPatientID().size()) {
								transfer = true;
								List<String> tmp_patient = new ArrayList<String>();
								tmp_patient.addAll(data.getValue().getPatientID());
								tmp_patient.addAll(tmp.get(appointmentID).getPatientID());
								SHEdata.get(appointmentType).get(data.getKey()).setPatientID(tmp_patient);
								SHEdata.get(appointmentType).remove(appointmentID);
								result = "Appointment found, patient has booked this appointment, transferred the appointments from "+appointmentID+" to "+data.getKey();
								break;
							}
						}
					}
					if (transfer) {
						log = "Appointment deleted, transferred appointments to new schedule.";
						status = "success";
					} else {
						log = "patients have booked this appointment, Not found any date to assign the patients";
						status = "failed";
					}
				}

			} else {
				result = "No Appointment is available for this type!!!";
				SHEdata.get(appointmentType).remove(appointmentID);
				log = "No Appointment is available.";
				status = "failed";
			}
		}
		writeToLog("REMOVE APPOINTMENT", appointmentType + "-" + appointmentID, status, log);
		return result;
	}

	@Override
	public String listAppointmentAvailability(String appointmentType) throws RemoteException {
		StringBuilder result = new StringBuilder();
		String log = "";
		if (SHEdata.containsKey(appointmentType)) {
			for (Map.Entry<String, AptDetails> data : SHEdata.get(appointmentType).entrySet()) {
				if ((data.getValue().getCapacity() - data.getValue().getPatientID().size() > 0))
					result.append(data.getKey()).append(" : ").append(data.getValue().getCapacity() - data.getValue().getPatientID().size()).append(",");

			}
		}

		Runnable quebecThread = () -> que_result = udpThread("list:" + appointmentType, Configuration.QUE_LISTENER);
		Thread quebecThreadObj = new Thread(quebecThread);
		quebecThreadObj.start();

		Runnable mtlThread = () -> mon_result = udpThread("list:" + appointmentType, Configuration.MTL_LISTENER);
		Thread mtlThreadObject = new Thread(mtlThread);
		mtlThreadObject.start();
		while (mtlThreadObject.isAlive() || quebecThreadObj.isAlive());

		String final_result = result + que_result + mon_result;
		log = final_result.trim().isEmpty() ? "No result Found!!" : "data found ["+final_result+"]";

		writeToLog("LIST AVAILABLE APPOINTMENT", appointmentType, "success", log);
		return (final_result.trim().isEmpty() ? "No result Found!!" : final_result);
	}

	@Override
	public synchronized String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {
		String result = "";
		String status = "failed";
		boolean canBook = true;
		String type = "";
		String ID = "";

		List<String> tmp = Arrays.asList((getAppointmentSchedule(patientID).trim()).split(","));
		List<String> appointments = tmp.subList(0, tmp.size());
		HashMap<Integer, Integer> appointments_week = new HashMap<>();
		if (appointments.size() > 0) {
			for (String appointment : appointments) {
				if (!appointment.isEmpty()) {
					type = appointment.split(":")[0].trim();
					ID = appointment.split(":")[1].trim();
					System.out.println(type + "---" + ID);
					if (!(ID.startsWith("SHE"))) {
						calender.set(Integer.parseInt("20" + ID.substring(8, 10)), Integer.parseInt(ID.substring(6, 8)),
								Integer.parseInt(ID.substring(4, 6)));
						int week = calender.get(Calendar.WEEK_OF_YEAR);
						if (appointments_week.containsKey(week)) {
							appointments_week.put(week, appointments_week.get(week) + 1);
						} else {
							appointments_week.put(week, 1);
						}
					}
				}
			}
		}
		System.out.println(appointments);
		System.out.println(appointments_week);

		if (appointmentID.startsWith("SHE")) {
			if (SHEdata.containsKey(appointmentType) && SHEdata.get(appointmentType).containsKey(appointmentID)) {
				if (appointments.size() > 0) {
					for (String appointment : appointments) {
						if (!appointment.isEmpty()) {
							type = appointment.split(":")[0].trim();
							ID = appointment.split(":")[1].trim();
							if (type.equals(appointmentType) && ID.equals(appointmentID)) {
								result = "You have already booked appointment with this type and appointmentID!!";
								status = "failed";
								canBook = false;
							} else if (type.equals(appointmentType) && ID.substring(4).equals(appointmentID.substring(4))) {
								result = "You have already booked appointment for " + appointmentType + "  for "
										+ appointmentID.substring(4, 6) + "/" + appointmentID.substring(6, 8);
								status = "failed";
								canBook = false;
							}
						}
					}
				}
				if (canBook) {
					if ((SHEdata.get(appointmentType).get(appointmentID).getCapacity()
							- SHEdata.get(appointmentType).get(appointmentID).getPatientID().size()) > 0) {
						AptDetails aptDetails = SHEdata.get(appointmentType).get(appointmentID);
						aptDetails.getPatientID().add(patientID);
						result = "Appointment booked for {" + appointmentType + "} for {" + appointmentID.substring(4, 6)
								+ "/" + appointmentID.substring(6, 8) + "}";
						status = "success";
					} else {
						result = "No more slots available for this Appointment!!!";
						status = "failed";
					}
				}
			} else {
				result = "No Appointment available for this Type and with this appointmentID!!!";
				canBook = false;
				status = "failed";
			}
		} else {
			if (appointments.size() > 0) {
				for (String appointment : appointments) {
					if (!appointment.isEmpty()) {
						type = appointment.split(":")[0].trim();
						ID = appointment.split(":")[1].trim();
						if (type.equals(appointmentType) && ID.equals(appointmentID)) {
							result = "You have already booked appointment with this type and appointmentID!!";
							status = "failed";
							canBook = false;
						} else if (type.equals(appointmentType) && ID.substring(4).equals(appointmentID.substring(4))) {
							result = "You have already booked appointment for " + appointmentType + "  for "
									+ appointmentID.substring(4, 6) + "/" + appointmentID.substring(6, 8);
							status = "failed";
							canBook = false;
						} else if (!(appointmentID.startsWith("SHE")) && !(ID.startsWith("SHE"))) {
							calender.set(Integer.parseInt("20" + appointmentID.substring(8, 10)),
									Integer.parseInt(appointmentID.substring(6, 8)),
									Integer.parseInt(appointmentID.substring(4, 6)));
							int newID_Week = calender.get(Calendar.WEEK_OF_YEAR);
							if (appointments_week.containsKey(newID_Week)) {
								if (appointments_week.get(newID_Week) <= 2) {
									result = "Book the appointment";
									status = "success";
									canBook = true;
								} else {
									result = "You have already booked 3 appointments for other cities";
									status = "failed";
									canBook = false;
								}
							} else {
								canBook = true;
								status = "success";
								result = "Book the appointment for the other city";
							}
						}
					}
				}
			}
			if (canBook) {
				String serverName = appointmentID.substring(0, 3).trim();
				if (serverName.equals("QUE")) {
					Runnable quebecThread = () -> que_result = udpThread("book:" + appointmentType + "-" + appointmentID + "-" + patientID, Configuration.QUE_LISTENER);
					Thread quebecThreadObj = new Thread(quebecThread);
					quebecThreadObj.start();
					while(quebecThreadObj.isAlive());

					result = que_result;
					status = (result.startsWith("No") ? "failed" : "success");
				} else if (serverName.equals("MTL")) {
					Runnable mtlThread = () -> mon_result = udpThread("book:" + appointmentType + "-" + appointmentID + "-" + patientID, Configuration.MTL_LISTENER);
					Thread mtlThreadObject = new Thread(mtlThread);
					mtlThreadObject.start();
					while (mtlThreadObject.isAlive());
					result = mon_result;
					status = (result.startsWith("No") ? "failed" : "success");
				} else {
					result = "Invalid appointmentID";
					status = "failed";
				}
			}
		}
		writeToLog("BOOK APPOINTMENT", appointmentType + "-" + appointmentID + "-" + patientID, status, result);
		return result;
	}

	@Override
	public String getAppointmentSchedule(String patientID) throws RemoteException {
		String key = null;
		StringBuilder result = new StringBuilder();
		String log = "";
		String status = "failed";
		for (Map.Entry<String, HashMap<String, AptDetails>> set : SHEdata.entrySet()) {
			key = set.getKey();
			for (Map.Entry<String, AptDetails> data : set.getValue().entrySet()) {
				if ((data.getValue().getPatientID()).contains(patientID)) {
					result.append(key).append(" : ").append(data.getKey()).append(",");
				}
			}
		}

		Runnable quebecThread = () -> que_result = udpThread("gets:" + patientID, Configuration.QUE_LISTENER);
		Thread quebecThreadObj = new Thread(quebecThread);
		quebecThreadObj.start();
		while (quebecThreadObj.isAlive());

		Runnable mtlThread = () -> mon_result = udpThread("gets:" + patientID, Configuration.MTL_LISTENER);
		Thread mtlThreadObject = new Thread(mtlThread);
		mtlThreadObject.start();
		while(mtlThreadObject.isAlive());

		String finalString = result + que_result.trim() + mon_result.trim();
		if (finalString.trim().isEmpty()) {
			log = "No Appointment found for the patient " + patientID;
			status = "failed";
		} else {
			log = "Appointment found for the patient " + patientID + " ["+finalString+"]";
			status = "success";
		}
		writeToLog("Get Appointment Schedule", patientID, status, log);
		return finalString;
	}

	@Override
	public synchronized String cancelAppointment(String patientID, String appointmentID) throws RemoteException {
		String status = "failed";
		String log = "Appointment not found";
		String result = "Appointment not found!!!";
		if(appointmentID.startsWith("SHE")) {
			for (Map.Entry<String, HashMap<String, AptDetails>> set : SHEdata.entrySet()) {
				for (Map.Entry<String, AptDetails> data : set.getValue().entrySet()) {
					if ((data.getValue().getPatientID()).contains(patientID) && data.getKey().equals(appointmentID)) {
						System.out.println("Appointment Found");
						data.getValue().getPatientID().remove(patientID);
						log = "Appointment canceled.";
						status = "success";
						result = "Appointment canceled!!!";
					}
				}
			}
		} else if(appointmentID.startsWith("QUE")){
			Runnable quebecThread = () -> que_result = udpThread("cancel:" + patientID + "-" + appointmentID, Configuration.QUE_LISTENER);
			Thread quebecThreadObj = new Thread(quebecThread);
			quebecThreadObj.start();
			while(quebecThreadObj.isAlive());
			result = que_result;
			log = (result.equals("Appointment canceled!!!") ? "Appointment canceled!!!" : log);
			status = (result.equals("Appointment canceled!!!") ? "success" : status);

		} else if(appointmentID.startsWith("MTL")){
			Runnable mtlThread = () -> mon_result = udpThread("cancel:" + patientID+"-"+appointmentID, Configuration.MTL_LISTENER);
			Thread mtlThreadObject = new Thread(mtlThread);
			mtlThreadObject.start();
			while (mtlThreadObject.isAlive());
			result = mon_result;
			log = (result.equals("Appointment canceled!!!") ? "Appointment canceled!!!" : log);
			status = (result.equals("Appointment canceled!!!") ? "success" : status);

		}else{
			result = "Invalid AppointmentID";
			log = "Invalid AppointmentID";
			status = "failed";
		}
		writeToLog("CANCEL APPOINTMENT", appointmentID + " - " + patientID, status, log);
		return result;
	}

	@Override
	public synchronized String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) throws RemoteException {
		String result = "";
		String cancel_result="",book_result="";
		String log = "",status="failed";

		cancel_result = cancelAppointment(patientID, oldAppointmentID);
		if(cancel_result.startsWith("Appointment canceled")) {
			book_result = bookAppointment(patientID, newAppointmentID, newAppointmentType);
			if(book_result.startsWith("Appointment booked")){
				result = "Appointment swaped from "+oldAppointmentID+" to "+newAppointmentID+" for "+patientID;
				log = result;
				status = "success";
			}else{
				String tmp = bookAppointment(patientID, oldAppointmentID, oldAppointmentType);
				result = "Can't swap the appointment: "+book_result;
				log = result;
				status = "failed";
			}
		}else{
			result = cancel_result;
			log = result;
			status = "failed";
		}

		writeToLog("SWAP APPOINTMENT", patientID+" OLD:{"+oldAppointmentID+","+oldAppointmentType+"}, NEW:{"+newAppointmentID+","+newAppointmentType+"}", status, log);
		return result;
	}

	@Override
	public String helloWorld(String name) throws RemoteException {
		// TODO Auto-generated method stub
		return "Hello Shrebrook";
	}

	public String listAvailableForServer(String appointmentType) {
		StringBuilder result = new StringBuilder();
		if (SHEdata.containsKey(appointmentType)) {
			for (Map.Entry<String, AptDetails> data : SHEdata.get(appointmentType.trim()).entrySet()) {
				if ((data.getValue().getCapacity() - data.getValue().getPatientID().size() > 0)) {
					result.append(data.getKey()).append(" : ").append(data.getValue().getCapacity() - data.getValue().getPatientID().size()).append(",");
				}
			}
		}
		return result.toString();
	}

	public String getScheduleForServer(String patientID) {
		String key = null;
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, HashMap<String, AptDetails>> set : SHEdata.entrySet()) {
			key = set.getKey();
			for (Map.Entry<String, AptDetails> data : set.getValue().entrySet()) {
				if ((data.getValue().getPatientID()).contains(patientID)) {
					result.append(key).append(" : ").append(data.getKey()).append(",");
				}
			}
		}
		return ((result.length() == 0) ? "" : result.toString());
	}

	public synchronized String bookAppointmentForServer(String data) {
		String result = "";
		String appointmentType = data.split("-")[0].trim();
		String appointmentID = data.split("-")[1].trim();
		String patientID = data.split("-")[2].trim();

		if ((SHEdata.containsKey(appointmentType)) && (SHEdata.get(appointmentType).containsKey(appointmentID))) {
			if ((SHEdata.get(appointmentType).get(appointmentID).getCapacity()
					- SHEdata.get(appointmentType).get(appointmentID).getPatientID().size()) > 0) {
				AptDetails aptDetails = SHEdata.get(appointmentType).get(appointmentID);
				aptDetails.getPatientID().add(patientID);
				result = "Appointment booked for " + appointmentType + " for " + appointmentID.substring(4, 6) + "/"
						+ appointmentID.substring(6, 8);
			} else {
				result = "No more slots available for this Appointment!!!";
			}
		} else {
			result = "No Appointment available for this Type and with this appointmentID!!!";
		}
		return result;
	}

	public synchronized String cancelAppointmentForServer(String input) {
		String result = "Appointment not found!!!";
		String patientID = input.split("-")[0].trim();
		String appointmentID = input.split("-")[1].trim();
		if(appointmentID.startsWith("SHE")) {
			for (Map.Entry<String, HashMap<String, AptDetails>> set : SHEdata.entrySet()) {
				for (Map.Entry<String, AptDetails> data : set.getValue().entrySet()) {
					if ((data.getValue().getPatientID()).contains(patientID) && data.getKey().equals(appointmentID)) {
						System.out.println("Appointment Found");
						data.getValue().getPatientID().remove(patientID);
						result = "Appointment canceled!!!";
					}
				}
			}
		}
		return result;
	}

	public void writeToLog(String operation, String params, String status, String responceDetails) {
		try {
			FileWriter myWriter = new FileWriter(Configuration.SHELOGFILE, true);
			String log = dateFormat.format(LocalDateTime.now()) + " : " + operation + " : [ " + params + " ] : " + status
					+ " : " + responceDetails + "\n";
			myWriter.write(log);
			myWriter.flush();
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String udpThread(String data, int port) {
		String result = "";
		try (DatagramSocket aSocket = new DatagramSocket()) {
			DatagramPacket request = new DatagramPacket(data.getBytes(), data.getBytes().length,
					InetAddress.getByName("localhost"), port);
			aSocket.send(request);
			writeToLog("Request sent to UDP:" + port, data, "waiting...", "---");
			System.out.println("data sent to "+port);

			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			aSocket.close();
			result = new String(reply.getData()).trim();
			writeToLog("Response received from UDP:" + port, data, "success", result);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String shutDown() throws RemoteException {
		SHEdata = new HashMap<>();
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
