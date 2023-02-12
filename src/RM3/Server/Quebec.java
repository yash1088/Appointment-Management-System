	package RM3.Server;

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

	import RM3.ImplementRemoteInterface.que_interface_imp;
	public class Quebec {

		//For interserver Communication using UDP
		static que_interface_imp authentication_que = null;
		InetAddress clientAddress;
		int port;

		public Quebec(InetAddress clientAddress, int port) {
			this.clientAddress = clientAddress;
			this.port = port;
		}
		// Port number through which we are doing communication
		private static final int PORT = 1000;

		public static void main(String args[]) {

			try {

				// Object definition
				authentication_que = new que_interface_imp();
				// Creation of RMI Registry with Port
				Registry registry_que = LocateRegistry.createRegistry(PORT);
				// Object Binding
				registry_que.bind("QUE", authentication_que);
				System.out.println("Quebec Server running at " + PORT + " port!!!");
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
					appointment_que();
				}
			}.start();
			new Thread() {
				public void run() {
					get_scheduled_que();
				}
			}.start();

			new Thread() {
				public void run() {
					cancel_scheduled_que();
				}
			}.start();

			new Thread() {
				public void run() {
					remove_appointment_que();
				}
			}.start();

			new Thread() {
				public void run() {
					swap_appointment_que();
				}
			}.start();


		}

		public static void getData() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(110)) {
				authentication_que = new que_interface_imp();
				byte[] buffer = new byte[1500];
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					HashMap<String, Integer> que_response = authentication_que.get_que_server_data(new String(request.getData()).trim());
					String result = que_response.entrySet().stream()
							.map(e -> e.getKey() + "=" + e.getValue())
							.collect(Collectors.joining("&"));
					byte[] outer_byte_array = result.getBytes();
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
		public static void appointment_que() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(456)) {
				while (true) {
					byte[] buffer = new byte[1000];
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					String str_patientId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding.
					String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
					String str_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding
					String que_response_book1 = authentication_que.get_data_book_appointment(str_patientId, str_appointmentId, str_appointmentType);
					byte[] outer_byte_array1 = que_response_book1.getBytes();
					// Implementation of the logic.
					DatagramPacket reply = new DatagramPacket(outer_byte_array1, outer_byte_array1.length, clientAddress, port);
					aSocket.send(reply);
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public static void get_scheduled_que() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(1111)) {
				authentication_que = new que_interface_imp();
				byte[] buffer = new byte[1500];
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					String que_response = authentication_que.get_que_scheduled_data(new String(request.getData()).trim());
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
		public static void cancel_scheduled_que() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(1949)) {
				authentication_que = new que_interface_imp();
				byte[] buffer = new byte[1500];
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					String str_patientId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding.
					String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
					String str_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding
					String que_response = authentication_que.get_que_cancel_scheduled_data(str_patientId, str_appointmentId, str_appointmentType);
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
		public static void remove_appointment_que() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(2021)) {
				authentication_que = new que_interface_imp();
				byte[] buffer = new byte[1500];
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					String str_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding
					String str_appointmentIType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
					String str_adminId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding.
					String que_response = authentication_que.get_que_remove_appointment_data(str_appointmentId, str_appointmentIType, str_adminId);
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
		public static void swap_appointment_que() {
			List<Quebec> clientList = new ArrayList<>();
			try (DatagramSocket aSocket = new DatagramSocket(3021)) {
				authentication_que = new que_interface_imp();
				byte[] buffer = new byte[1500];
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(request);
					InetAddress clientAddress = request.getAddress();
					int port = request.getPort();
					clientList.add(new Quebec(clientAddress, port));
					String str_patientID = new String(request.getData(), StandardCharsets.UTF_8).split(",")[0].trim(); // for UTF-8 encoding
					String str_old_appointmentID = new String(request.getData(), StandardCharsets.UTF_8).split(",")[1].trim(); // for UTF-8 encoding
					String str_old_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[2].trim(); // for UTF-8 encoding.
					String str_new_appointmentId = new String(request.getData(), StandardCharsets.UTF_8).split(",")[3].trim(); // for UTF-8 encoding
					String str_new_appointmentType = new String(request.getData(), StandardCharsets.UTF_8).split(",")[4].trim(); // for UTF-8 encoding
					String que_response = authentication_que.get_que_swap_appointment_data(str_patientID, str_old_appointmentID, str_old_appointmentType,str_new_appointmentId,str_new_appointmentType);
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
