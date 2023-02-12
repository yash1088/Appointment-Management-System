package Sequencer;

import model.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author Yash Radadiya
 * @created 02/04/2022
 */

public class Sequencer {
	private static int sequencerId = 0;
	private static final String sequencerIP = "192.168.2.17";
	static int temp = 0;
	public static String last_message = "";

	public static void main(String[] args) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1333);
			byte[] buffer = new byte[5000];
			System.out.println("Sequencer UDP Server Started");
			while (true) {

				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length);

				aSocket.receive(request);

				String object = new String(request.getData(), 0,
						request.getLength());

				System.out.println("Object received from FrontEnd:- "+object);

				String[] parts = object.split(";");
				int sequencerId1 = Integer.parseInt(parts[0]);
				temp = sequencerId1;
				String ip = request.getAddress().getHostAddress();

				String temp_object = ip + ";" + parts[2] + ";" + parts[3] + ";" + parts[4] + ";" + parts[5] + ";" + parts[6] + ";" + parts[7] + ";" + parts[8] + ";" + parts[9] + ";";

				sendMessage(temp_object, sequencerId1, parts[2].equalsIgnoreCase("00"));

				byte[] SeqId = (Integer.toString(sequencerId)).getBytes();
				InetAddress aHost1 = request.getAddress();
				int port1 = request.getPort();

				DatagramPacket request_to_front = new DatagramPacket(SeqId,
						SeqId.length, aHost1, port1);
				aSocket.send(request_to_front);
			}

		} catch (SocketException e) {
			System.out.println("Socket Exception in Sequencer: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO Exception in Sequencer: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public static void sendMessage(String message, int sequencerId1, boolean isRequest) {
		int port = Utils.SEQUENCER_MULTICAST_PORT;
		String finalMessage = "";
		if(!last_message.equals(message)) {
			if (sequencerId1 == 0 && isRequest) {
				sequencerId1 = ++sequencerId;
			}
			finalMessage = sequencerId1 + ";" + message;
			last_message = message;
		}else{
			finalMessage = sequencerId + ";" + message;
		}

		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket();
			byte[] messages_RMS = finalMessage.getBytes();
			InetAddress aHost = InetAddress.getByName(Utils.SEQUENCER_MULTICAST_IP);

			DatagramPacket request_to_RMS = new DatagramPacket(messages_RMS,
					messages_RMS.length, aHost, port);
			aSocket.send(request_to_RMS);
			System.out.println("Packet send to RMS");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
