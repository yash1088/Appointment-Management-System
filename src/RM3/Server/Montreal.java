package RM3.Server;

import RM3.ImplementRemoteInterface.mtl_interface_imp;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Montreal {
	static mtl_interface_imp authentication_mtl = null;
	//For interserver Communication using UDP
	InetAddress clientAddress;
	int port;

	public Montreal(InetAddress clientAddress, int port) {
		this.clientAddress = clientAddress;
		this.port = port;
	}

	//For interserver Communication using UDP
	// Port number through which we are doing communication
	private static final int PORT = 1010;

	public static void main(String args[]) {
		try {
			//For Identification of Server based on Client wants to connect through which server
			// Object definition
			authentication_mtl = new mtl_interface_imp();
			// Creation of RMI Registry with Port
			Registry registry_mtl = LocateRegistry.createRegistry(PORT);
			// Object Binding
			registry_mtl.bind("MTL", authentication_mtl);
			System.out.println("Montreal Server running at " + PORT + " port!!!");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}


		new Thread() {
			public void run() {
				getData();
			}
		}.start();

		new Thread() {
			public void run() {
				appointment_mtl();
			}
		}.start();

		new Thread() {
			public void run() {
				get_scheduled_mtl();
			}
		}.start();

		new Thread() {
			public void run() {
				cancel_scheduled_mtl();
			}
		}.start();

		new Thread() {
			public void run() {
				remove_appointment_mtl();
			}
		}.start();

		new Thread() {
			public void run() {
				swap_appointment_mtl();
			}
		}.start();
	}

	public static void appointment_mtl() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(678)) {
			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				String str_patientId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding.
				String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
				String str_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding
				String mtl_response_book1 = authentication_mtl.get_data_book_appointment(str_patientId, str_appointmentId, str_appointmentType);
				byte[] outer_byte_array1 = mtl_response_book1.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array1, outer_byte_array1.length, clientAddress, port);
				aSocket.send(reply);
				System.out.println(new String(reply.getData()) + "reply");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void getData() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(111)) {
			authentication_mtl = new mtl_interface_imp();
			byte[] buffer = new byte[1500];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				HashMap<String, Integer> mtl_response = authentication_mtl.get_mtl_server_data(new String(request.getData()).trim());
				System.out.println(new String(request.getData()).trim() + "que to mtl_request");
				String result = mtl_response.entrySet().stream()
						.map(e -> e.getKey() + "=" + e.getValue())
						.collect(Collectors.joining("&"));
				byte[] outer_byte_array = result.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array, outer_byte_array.length, clientAddress, port);
				aSocket.send(reply);
				System.out.println(new String(reply.getData()).trim() + "fsfft");
				System.out.println(new String(reply.getData()).trim() + "Montreal");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void get_scheduled_mtl() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(1112)) {
			authentication_mtl = new mtl_interface_imp();
			byte[] buffer = new byte[1500];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				String mtl_response = authentication_mtl.get_mtl_scheduled_data(new String(request.getData()).trim());
				byte[] outer_byte_array = mtl_response.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array, outer_byte_array.length, clientAddress, port);
				aSocket.send(reply);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void cancel_scheduled_mtl() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(1950)) {
			authentication_mtl = new mtl_interface_imp();
			byte[] buffer = new byte[1500];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				String str_patientId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding.
				String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
				String str_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding
				String mtl_response = authentication_mtl.get_mtl_cancel_scheduled_data(str_patientId, str_appointmentId, str_appointmentType);
				byte[] outer_byte_array = mtl_response.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array, outer_byte_array.length, clientAddress, port);
				aSocket.send(reply);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void remove_appointment_mtl() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(2022)) {
			authentication_mtl = new mtl_interface_imp();
			byte[] buffer = new byte[1500];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding
				String str_appointmentIType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
				String str_adminId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding.
				String mtl_response = authentication_mtl.get_mtl_remove_appointment_data(str_appointmentId, str_appointmentIType, str_adminId);
				byte[] outer_byte_array = mtl_response.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array, outer_byte_array.length, clientAddress, port);
				aSocket.send(reply);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void swap_appointment_mtl() {
		List<Montreal> clientList = new ArrayList<>();
		try (DatagramSocket aSocket = new DatagramSocket(3022)) {
			authentication_mtl = new mtl_interface_imp();
			byte[] buffer = new byte[1500];
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				InetAddress clientAddress = request.getAddress();
				int port = request.getPort();
				clientList.add(new Montreal(clientAddress, port));
				String str_patientID = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding
				String str_old_appointmentID = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
				String str_old_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding.
				String str_new_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[3].trim(); // for UTF-8 encoding
				String str_new_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[4].trim(); // for UTF-8 encoding
				String que_response = authentication_mtl.get_mtl_swap_appointment_data(str_patientID, str_old_appointmentID, str_old_appointmentType, str_new_appointmentId, str_new_appointmentType);
				byte[] outer_byte_array = que_response.getBytes();
				// Implementation of the logic.
				DatagramPacket reply = new DatagramPacket(outer_byte_array, outer_byte_array.length, clientAddress, port);
				aSocket.send(reply);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
