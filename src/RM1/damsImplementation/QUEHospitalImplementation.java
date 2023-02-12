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
@SuppressWarnings("serial")
public class QUEHospitalImplementation extends UnicastRemoteObject implements DamsInterface {

	public HashMap<String, HashMap<String, AptDetails>> QUEdata = null;
	public HashMap<String, AptDetails> tmpMap = null;
	public List<String> patientList = null;
	DateTimeFormatter dateFormat = null;
	FileWriter myWriter = null;
	TimeUnit time = TimeUnit.SECONDS;
	public String she_result = "", mon_result = "";
	Calendar calender = null;

	/**
	 * @throws RemoteException
	 */
	public QUEHospitalImplementation() throws RemoteException {
		super();
		calender = Calendar.getInstance(Locale.CANADA);
		dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		QUEdata = new HashMap<>();
		loadData();
	}

	private void loadData() {
		tmpMap = new HashMap<>();
		patientList = new ArrayList<>();

		patientList.add("MTLP1234");
		patientList.add("QUEP0733");
		patientList.add("QUEP2345");
		tmpMap.put("QUEA020222", new AptDetails(patientList, 3));

		patientList = new ArrayList<>();
		patientList.add("MTLP1234");
		patientList.add("QUEP0733");
		patientList.add("QUEP2345");
		tmpMap.put("QUEM020222", new AptDetails(patientList, 4));
		QUEdata.put("Physician", tmpMap);

		tmpMap = new HashMap<>();
		patientList = new ArrayList<>();
		patientList.add("QUEP1234");
		patientList.add("QUEP0733");
		patientList.add("QUEP2345");
		tmpMap.put("QUEE020222", new AptDetails(patientList, 4));
		QUEdata.put("Dental", tmpMap);
	}

	@Override
	public synchronized String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException {
		String result = "";
		String status = "";
		try {
			if (!(appointmentID.startsWith("QUE"))) {
				result = "Invalid appointmentID!!!";
				status = "failed";
			} else if (!QUEdata.containsKey(appointmentType)) {
				HashMap<String, AptDetails> tmp = new HashMap<>();
				tmp.put(appointmentID, new AptDetails(new ArrayList<>(), capacity));
				QUEdata.put(appointmentType, tmp);
				result = "Appointment added for " + appointmentType;
				status = "success";
			} else {
				if (QUEdata.get(appointmentType).containsKey(appointmentID)) {
					result = "Appointment is already present with the appointmentID!!!";
					status = "failed";
				} else {
					HashMap<String, AptDetails> tmp = QUEdata.get(appointmentType);
					tmp.put(appointmentID, new AptDetails(new ArrayList<>(), capacity));
					QUEdata.put(appointmentType, tmp);
					result = "Appointment added for " + appointmentType;
					status = "success";
				}
			}

			writeToLog("ADD APPOINTMENT", appointmentType + "-" + appointmentID + "-" + capacity, status, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public synchronized String removeAppointment(String appointmentID, String appointmentType) throws RemoteException {
		String result;
		boolean transfer = false;
		String status = "false";
		String log = "";
		int day = Integer.parseInt(appointmentID.substring(4, 6));
		int month = Integer.parseInt(appointmentID.substring(6, 8));

		if (!QUEdata.containsKey(appointmentType)) {
			result = "No Appointment is available for this type!!!";
			log = "No Appointment is available.";
			status = "failed";
		} else {
			HashMap<String, AptDetails> tmp = QUEdata.get(appointmentType);
			if (tmp.containsKey(appointmentID)) {
				if (tmp.get(appointmentID).getPatientID().size() == 0) {
					result = "Appointment found, no patient booked, cancelling it";
					QUEdata.get(appointmentType).remove(appointmentID);
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

								QUEdata.get(appointmentType).get(data.getKey()).setPatientID(tmp_patient);
								QUEdata.get(appointmentType).remove(appointmentID);

								result = "Appointment found, patient has booked this appointment, transferred the appointments from "+appointmentID+" to "+data.getKey();
								break;
							}
						}
					}
					if (transfer) {
						log = "Appointment deleted, transfered appointments to new schedule.";
						status = "success";
					} else {
						log = "patients have booked this appointment, Not found any date to assign the patients";
						status = "failed";
					}
				}

			} else {
				result = "No Appointment is available for this type!!!";
				QUEdata.get(appointmentType).remove(appointmentID);
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
		String log;
		if (QUEdata.containsKey(appointmentType)) {
			for (Map.Entry<String, AptDetails> data : QUEdata.get(appointmentType).entrySet()) {
				if ((data.getValue().getCapacity() - data.getValue().getPatientID().size() > 0))
					result.append(data.getKey()).append(" : ").append(data.getValue().getCapacity() - data.getValue().getPatientID().size()).append(",");

			}
		}

		Runnable mtlThread = () -> mon_result = udpThread("list:" + appointmentType, Configuration.MTL_LISTENER);
		Thread mtlThreadObject = new Thread(mtlThread);
		mtlThreadObject.start();

		Runnable sheThread = () -> she_result = udpThread("list:" + appointmentType, Configuration.SHE_LISTENER);
		Thread sheThreadObj = new Thread(sheThread);
		sheThreadObj.start();
		while (sheThreadObj.isAlive() || mtlThreadObject.isAlive()) ;

		String final_result = result + mon_result + she_result;
		log = final_result.trim().isEmpty() ? "No result Found!!" : "data found [" + final_result + "]";

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
		if (appointments.size() > 0 ) {
			for (String appointment : appointments) {
				if (!appointment.isEmpty()) {
					type = appointment.split(":")[0].trim();
					ID = appointment.split(":")[1].trim();
					System.out.println(type + "---" + ID);
					if (!(ID.startsWith("QUE"))) {
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
//		System.out.println(appointments);
//		System.out.println(appointments_week);

		if (appointmentID.startsWith("QUE")) {
			if (QUEdata.containsKey(appointmentType) && QUEdata.get(appointmentType).containsKey(appointmentID)) {
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
					if ((QUEdata.get(appointmentType).get(appointmentID).getCapacity()
							- QUEdata.get(appointmentType).get(appointmentID).getPatientID().size()) > 0) {
						AptDetails aptDetails = QUEdata.get(appointmentType).get(appointmentID);
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
							break;
						} else if (type.equals(appointmentType) && ID.substring(4).equals(appointmentID.substring(4))) {
							result = "You have already booked appointment for " + appointmentType + "  for "
									+ appointmentID.substring(4, 6) + "/" + appointmentID.substring(6, 8);
							status = "failed";
							canBook = false;
							break;
						} else if (!(appointmentID.startsWith("QUE")) && !(ID.startsWith("QUE"))) {
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
									break;
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
				if (serverName.equals("MTL")) {
					Runnable mtlThread = () -> mon_result = udpThread("book:" + appointmentType + "-" + appointmentID + "-" + patientID, Configuration.MTL_LISTENER);
					Thread mtlThreadObject = new Thread(mtlThread);
					mtlThreadObject.start();
					while (mtlThreadObject.isAlive()) ;
					result = mon_result;
					status = (result.startsWith("No") ? "failed" : "success");

				} else if (serverName.equals("SHE")) {
					Runnable sheThread = () -> she_result = udpThread("book:" + appointmentType + "-" + appointmentID + "-" + patientID, Configuration.SHE_LISTENER);
					Thread sheThreadObj = new Thread(sheThread);
					sheThreadObj.start();
					while (sheThreadObj.isAlive()) ;
					result = she_result;
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
		String key = "";
		StringBuilder result = new StringBuilder();
		String log = "";
		String status = "failed";
		for (Map.Entry<String, HashMap<String, AptDetails>> set : QUEdata.entrySet()) {
			key = set.getKey();
			for (Map.Entry<String, AptDetails> data : set.getValue().entrySet()) {
				if ((data.getValue().getPatientID()).contains(patientID)) {
					result.append(key).append(" : ").append(data.getKey()).append(",");
				}
			}
		}

		Runnable sheThread = () -> she_result = udpThread("gets:" + patientID, Configuration.SHE_LISTENER);
		Thread sheThreadObj = new Thread(sheThread);
		sheThreadObj.start();
		while (sheThreadObj.isAlive());

		Runnable mtlThread = () -> mon_result = udpThread("gets:" + patientID, Configuration.MTL_LISTENER);
		Thread mtlThreadObject = new Thread(mtlThread);
		mtlThreadObject.start();
		while (mtlThreadObject.isAlive()) ;

		String finalString = result + mon_result + she_result;
		if (finalString.trim().isEmpty()) {
			log = "No Appointment found for the patient " + patientID;
			status = "failed";
		} else {
			log = "Appointment found for the patient " + patientID + " [" + finalString + "]";
			status = "success";
		}
		writeToLog("GET APPOINTMENT SCHEDULE", patientID, status, log);
		return finalString;
	}

	@Override
	public synchronized String cancelAppointment(String patientID, String appointmentID) throws RemoteException {
		String status = "failed";
		String log = "Appointment not found";
		String result = "Appointment not found!!!";
		if (appointmentID.startsWith("QUE")) {
			for (Map.Entry<String, HashMap<String, AptDetails>> set : QUEdata.entrySet()) {
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
		} else if (appointmentID.startsWith("MTL")) {
			Runnable mtlThread = () -> mon_result = udpThread("cancel:" + patientID + "-" + appointmentID, Configuration.MTL_LISTENER);
			Thread mtlThreadObject = new Thread(mtlThread);
			mtlThreadObject.start();
			while (mtlThreadObject.isAlive()) ;

			result = mon_result;
			log = (result.equals("Appointment canceled!!!") ? "Appointment canceled!!!" : log);
			status = (result.equals("Appointment canceled!!!") ? "success" : status);

		} else if (appointmentID.startsWith("SHE")) {
			Runnable sheThread = () -> she_result = udpThread("cancel:" + patientID + "-" + appointmentID, Configuration.SHE_LISTENER);
			Thread sheThreadObj = new Thread(sheThread);
			sheThreadObj.start();
			while (sheThreadObj.isAlive()) ;

			result = she_result;
			log = (result.equals("Appointment canceled!!!") ? "Appointment canceled!!!" : log);
			status = (result.equals("Appointment canceled!!!") ? "success" : status);

		} else {
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
		String cancel_result = "", book_result = "";
		String log = "", status = "failed";

		cancel_result = cancelAppointment(patientID, oldAppointmentID);
		if (cancel_result.startsWith("Appointment canceled")) {
			book_result = bookAppointment(patientID, newAppointmentID, newAppointmentType);
			if (book_result.startsWith("Appointment booked")) {
				result = "Appointment swaped from " + oldAppointmentID + " to " + newAppointmentID + " for " + patientID;
				log = result;
				status = "success";
			} else {
				String tmp = bookAppointment(patientID, oldAppointmentID, oldAppointmentType);
				result = "Can't swap the appointment: " + book_result;
				log = result;
				status = "failed";
			}
		} else {
			result = cancel_result;
			log = result;
			status = "failed";
		}

		writeToLog("SWAP APPOINTMENT", patientID + " OLD:{" + oldAppointmentID + "," + oldAppointmentType + "}, NEW:{" + newAppointmentID + "," + newAppointmentType + "}", status, log);
		return result;
	}

	@Override
	public String helloWorld(String name) throws RemoteException {
		return "Hello Quebec";
	}

	public String listAvailableForServer(String appointmentType) {
		StringBuilder result = new StringBuilder();
		if (QUEdata.containsKey(appointmentType)) {
			for (Map.Entry<String, AptDetails> data : QUEdata.get(appointmentType.trim()).entrySet()) {
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
		for (Map.Entry<String, HashMap<String, AptDetails>> set : QUEdata.entrySet()) {
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

		if (QUEdata.containsKey(appointmentType) && (QUEdata.get(appointmentType).containsKey(appointmentID))) {
			if ((QUEdata.get(appointmentType).get(appointmentID).getCapacity()
					- QUEdata.get(appointmentType).get(appointmentID).getPatientID().size()) > 0) {
				AptDetails aptDetails = QUEdata.get(appointmentType).get(appointmentID);
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
		if (appointmentID.startsWith("QUE")) {
			for (Map.Entry<String, HashMap<String, AptDetails>> set : QUEdata.entrySet()) {
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
			FileWriter myWriter = new FileWriter(Configuration.MTLLOGFILE, true);
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
			System.out.println("data sent to " + port);

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
		QUEdata = new HashMap<>();
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
