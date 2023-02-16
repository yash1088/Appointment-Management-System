/**
 *
 */
package RM4.Server;

import RM4.config.Configuration;
import RM4.damsImplementation.MTL_Impl;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MTLServer {

	/**
	 * @param args
	 */
		private static RM4.damsImplementation.MTL_Impl MTL_Impl;

		public static void  main(String[] args) {
			try {
				Registry registry = LocateRegistry.createRegistry(1030);

				String URL = "rmi://localhost:1030/Mtl";
				MTL_Impl obj = new MTL_Impl();
				Naming.rebind(URL, obj);
				listRegistry(URL);
				System.out.println("Server is running");
				new Thread(() -> listenToUDP(MTL_Impl)).start();
				DatagramSocket asocket = null;
				try{
					asocket = new DatagramSocket(6794);
					byte[] buffer =  new byte[100];

					while(true){
						DatagramPacket request = new DatagramPacket(buffer, buffer.length);
						asocket.receive(request);
						DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
						asocket.send(reply);
					} }catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					if(asocket!=null){
						asocket.close();
					}
				}




			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
			System.out.println("Registry " + registryURL + " contains: ");
			String[] names = Naming.list(registryURL);
			for (int i = 0; i < names.length; i++)
				System.out.println(names[i]);
		}
		private static void listenToUDP(RM4.damsImplementation.MTL_Impl Mtl_Impl) {
			try{
				String result= "";
				DatagramSocket asocket = new DatagramSocket(6792);
				byte[] buffer =  new byte[10000];

				while(true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					asocket.receive(request);
					System.out.println("MTL GOT : " + new String(request.getData()).trim());
					String request_data = new String(request.getData()).trim();
					System.out.println(request_data+"data");
					//data : "list:Physician
					// data : book:MTLP1234-MTLA030322-Physician
					String op = request_data.split(":")[0].trim();
					String data = request_data.split(":")[1].trim();
					//for book
					String op1 = request_data.split(":")[0].trim();
					//for swap
					String op2 =  request_data.split(":")[0].trim();
					if (op.equals("list")) {
						result = Mtl_Impl.get_listappointmentdata(data);
					}

					else if (op1.equals("book")) {

						String data1 = request_data.split(":")[1].trim();
						System.out.println(data1+"data1");
						String User_id = data1.split("-")[0].trim();
						String ap_id = data1.split("-")[1].trim();
						String ap_t = data1.split("-")[2].trim();
						result = Mtl_Impl.get_bookappointmentdata(User_id, ap_id, ap_t);

					}
					//Swap:Mtlp1234-mtlm030322-physician-quem030222-physician
					else if (op2.equals("Swap")) {

						String data1 = request_data.split(":")[1].trim();
						String User_id = data1.split("-")[0].trim();
						String Oap_id = data1.split("-")[1].trim();
						String Oap_t = data1.split("-")[2].trim();
						String nap_id = data1.split("-")[3].trim();
						String nap_t= data1.split("-")[4].trim();
						result = Mtl_Impl.get_swapappointmentdata(User_id,Oap_id,Oap_t,nap_id,nap_t);

					}


					byte[] rp = result.getBytes();
					DatagramPacket reply = new DatagramPacket(rp, rp.length, request.getAddress(), request.getPort());
					System.out.println("MTL SENT : " + new String(rp).trim());
					asocket.send(reply);

				} }catch (Exception e) {
				e.printStackTrace();
			}
		}

	}






