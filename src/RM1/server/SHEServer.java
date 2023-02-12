/**
 * 
 */
package RM1.server;

import RM1.config.Configuration;
import RM1.damsImplementation.SHEHospitalImplementation;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Sumit Monapara
 *
 */
public class SHEServer {

	public static void main(String[] args) {
		SHEHospitalImplementation obj = null;
		try {
			startRegistry(7777);
			String URL = "rmi://" + Configuration.HOSTNAME + ":" + Configuration.port + "/" + Configuration.SHESERVER;
			obj = new SHEHospitalImplementation();
			Naming.rebind(URL, obj);
			System.out.println("Sherbrook Server is ready to treat!!!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			try (DatagramSocket aSocket = new DatagramSocket(Configuration.SHE_LISTENER)) {
				String result = "";
				System.out.println("Socket created Sherbrook"+aSocket);
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				System.out.println("received");

				String type = new String(request.getData()).trim().split(":")[0];
				String data = new String(request.getData()).trim().split(":")[1];
				System.out.println(data + "---" + type);
				switch (type) {
					case "list":
						result = obj.listAvailableForServer(data);
						break;
					case "gets":
						result = obj.getScheduleForServer(data);
						break;
					case "book":
						result = obj.bookAppointmentForServer(data);
						break;
					case "cancel":
						result = obj.cancelAppointmentForServer(data);
						break;
				}
				System.out.println(result);
				DatagramPacket reply = new DatagramPacket(result.getBytes(), result.length(), request.getAddress(), request.getPort());
				aSocket.send(reply);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void startRegistry(int RMIPortNum) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
			registry.list();

		} catch (RemoteException e) {
			System.out.println("RMI registry cannot be located at port "+ RMIPortNum);
			LocateRegistry.createRegistry(RMIPortNum);
			System.out.println("RMI registry created at port " + RMIPortNum);
		}
	}
}
