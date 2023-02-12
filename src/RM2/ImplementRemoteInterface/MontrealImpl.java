package RM2.ImplementRemoteInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MontrealImpl extends UnicastRemoteObject implements MainInterface {

	HashMap<String, String> inner;
	HashMap<String, HashMap<String,String>> outer;
	HashMap<String, String> msg = null;
	HashMap<String, String> tmp = null;
	HashMap<String, String> tmp_patient = new HashMap<>();
	HashMap<String, HashMap<String,String>> outer_patient;
	HashMap<String, String> inner_patient = null;
	HashMap<String, String> store_previous = null;
	HashMap<String,String> hp;
	String pre_pid = "";
	FileWriter writer;
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();
	String send = "",var;
	DatagramSocket ds_swap;
	String msg1;
	String msg_patient="";
	DatagramSocket ds_que,ds_she;
	DatagramPacket reply_que,reply_she;
	String rep_que = "";
	String rep_she="";
	String msg_udp=  "";
	String require_data="";
	int msgfromshe = 0;
	int cntmtl = 0;
	String log="";//172.30.119.225
	int cnt_fault = 0;

	public MontrealImpl() throws IOException {
		super();
		inner = new HashMap<String, String>();
		outer = new HashMap<String, HashMap<String,String>>();
		msg = new HashMap<String, String>();
		tmp = new HashMap<String, String>();
		inner_patient = new HashMap<String, String>();
		outer_patient = new HashMap<String, HashMap<String,String>>();
		store_previous = new HashMap<String, String>();

//		inner.put("MTLA101010", "1000");
//		outer.put("P", inner);
//
//		inner = new HashMap<String, String>();
//
//		inner.put("MTLM211022", "15");
//		outer.put("S",inner);
//		inner = new HashMap<String, String>();
//
//		inner.put("MTLE300100", "20");
//		outer.put("D",inner);

		//inner.clear();

		File folder = new File("ServerList");
		folder.mkdir();

		File f = new File(folder,"Montreal" + ".txt");

		writer = new FileWriter(f, true);
		writer.write("You are connected to Distributed Appointment Management System");

	}

	@Override
	public synchronized String bookappointment(String pid, String str) {
//		str = str.trim();
//
//		String tempp  = pid.substring(8);//MTLP12342
//		pid = pid.substring(0,8);
//
//		if(tempp.equals("2")){
//			require_data = "book".concat(pid).concat(str);
//			try {
//				ds_que = new DatagramSocket();
//				//System.out.println(apptype);
//				byte[] arr = require_data.getBytes();
//				InetAddress add = InetAddress.getLocalHost();
//				DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);
//
//				ds_que.send(requestQUE);
//
//				byte[] fetchdataque = new byte[1000];
//				reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
//				ds_que.receive(reply_que);
//
//				rep_que = new String(reply_que.getData());
//				msg_patient = rep_que.trim();
//			}
//			catch(Exception e){
//				System.out.println("Catch");
//				System.out.println(e.getMessage());
//			}
//		}
//		else if(tempp.equals("3")){
//			//System.out.println("Hello bookshe");
//			require_data = "book".concat(pid).concat(str);
//			try {
//				ds_she = new DatagramSocket();
//				//System.out.println(apptype);
//				byte[] arr = require_data.getBytes();
//				InetAddress add = InetAddress.getLocalHost();
//				DatagramPacket requestshe = new DatagramPacket(arr,arr.length,add,9596);
//
//				ds_she.send(requestshe);
//
//				byte[] fetchdataque = new byte[1000];
//				reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
//				ds_she.receive(reply_she);
//
//				rep_she = new String(reply_she.getData());
//				msg_patient = rep_she.trim();
//			}
//			catch(Exception e){
//				System.out.println("Catch");
//				System.out.println(e.getMessage());
//			}
//		}
//		else {
//			log = "\n\nbookappointment called by "+ pid +"on "+dtf.format(now);
//			printLog(log);
//			pre_pid = pid;
//			tmp_patient = outer.get(str);
//
//			if(outer.size()==0){
//				msg_patient = "there is no appointment";
//			}
//			else{
//				Set<String> str1 = tmp_patient.keySet();
//				String str2 = str1.toString();
//
//				HashMap<String, String> mm = outer_patient.get(pid);
//				if(mm==null){
//					cntmtl = 0;
//				}
//				else {
//					cntmtl = mm.size();
//				}
//				try {
//					require_data = "send".concat(pid);
//					ds_she = new DatagramSocket();
//					//System.out.println(apptype);
//					byte[] arr = require_data.getBytes();
//					InetAddress add = InetAddress.getLocalHost();
//					DatagramPacket requestSHE = null;
//
//					if(pid.contains("SHE")){
//						requestSHE = new DatagramPacket(arr,arr.length,add,9595);
//					}
//					else if(pid.contains("QUE")) {
//						requestSHE = new DatagramPacket(arr,arr.length,add,9596);
//					}
//
//					ds_she.send(requestSHE);
//
//					byte[] fetchdataque = new byte[1000];
//					reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
//					ds_she.receive(reply_she);
//
//					rep_she = new String(reply_she.getData());
//					msgfromshe = Integer.parseInt(rep_she.trim());
//					System.out.println("Cnt mtl"+cntmtl);
//					System.out.println("Msgfromshe"+msgfromshe);
//				}
//				catch(Exception e){
//					System.out.println("Catch");
//					//System.out.println(e.getMessage());
//				}
//				//System.out.println("mm"+mm);
//				if(outer_patient.containsKey(pid)){
//					if(cntmtl+msgfromshe==3){
//						System.out.println("Ypu cant book more than 3 appointments");
//						msg_patient = "You can`t book more than 3 appointments except your city!!!";
//						printLog("\nYou can`t book more than 3");
//					}
//					else{
//						HashMap<String,String> testdata = outer_patient.get(pid);
//						if(testdata.containsKey(str)){
//							msg_patient = "You can`t book appointment with same type";
//							printLog("\nYou can`t book appointment with same type");
//
//						}
//						else{
//							hp = outer_patient.get(pid);
//							hp.put(str, str2.substring(1,11));
//
//							HashMap<String,String> hh = outer.get(str);    //p={MTLM100200=[10]}
//
//							Set<String> set_str = hh.keySet();
//							String str11 = set_str.toString();
//							//System.out.println(str11);
//
//							//System.out.println(hh.values());
//							Collection<String> hhvalue = hh.values();
//							String ff = String.valueOf(hhvalue);
//
//
//							//System.out.println(ff1);
//							String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))-1);
//
//							HashMap<String,String> to_replce = new HashMap<>();
//							to_replce.put(str11.substring(1,11), temp);
//
//							HashMap<String, String> tmp = outer.get(str);
//							tmp.put(str2.substring(1,11),temp);
//
//							outer.put(str,tmp);
//							System.out.println("After adding"+outer);
//
//							printLog("\nYour slot has been booked on"+ str2);
//
//							msg_patient = "Your slot has been booked on"+ str2;
//
//						}
//					}
//				}
//				else{
//					HashMap<String,String> tmpp = new HashMap<>();
//					tmpp.put(str, str2.substring(1,11));
//					outer_patient.put(pid, tmpp);
//
//					HashMap<String,String> hh = outer.get(str);    //p={MTLM100200=[10]}
//					System.out.println(hh);
//
//					Set<String> set_str = hh.keySet();
//					String str11 = set_str.toString();
//					System.out.println(str11);
//
//					//System.out.println(hh.values());
//					Collection<String> hhvalue = hh.values();
//					String ff = String.valueOf(hhvalue);
//
//					//System.out.println(ff.substring(1,ff.length()-1));//[10]
//					String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))-1);
//
//					HashMap<String,String> to_replce = new HashMap<>();
//					to_replce.put(str11.substring(1,11), temp);
//					outer.replace(str,to_replce);
//					System.out.println("After adding"+outer);
//					printLog("\nYour slot has been booked on"+ str2);
//
//					msg_patient = "Your slot has been booked on"+ str2;
//
//				}
//			}
//		}
//
//		System.out.println(outer_patient);

		//return msg_patient;
		return "Appointment Booked";

	}

	@Override
	public  String sendData(){
		return outer.toString();
	}

	@Override
	public synchronized String addappointment(String apptype, String appid, String capicity) {
		String msg  = "";
		try{

			System.out.println("add");

			if(appid.contains("MTL")){
				if(outer.containsKey(apptype)){
					HashMap<String, String> inner = outer.get(apptype);
					inner.put(appid, capicity);
				}
				else{
					HashMap<String, String> tmp = new HashMap<>();
					tmp.put(appid, capicity);
					outer.put(apptype, tmp);
				}

			}
			else if(appid.contains("QUE")){
				require_data = "add".concat(apptype).concat(appid).concat(capicity);//addPQUEE30010050
				try {
					ds_que = new DatagramSocket();
					//System.out.println(apptype);
					byte[] arr = require_data.getBytes();
					InetAddress add = InetAddress.getLocalHost();
					DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

					ds_que.send(requestQUE);

					byte[] fetchdataque = new byte[1000];
					reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
					ds_que.receive(reply_que);

					rep_que = new String(reply_que.getData());
					msg = rep_que.trim();
				}
				catch(Exception e){
					System.out.println("Catch");
					System.out.println(e.getMessage());
				}
			}
			else if(appid.contains("SHE")){
				require_data = "add".concat(apptype).concat(appid).concat(capicity);
				try {
					ds_she = new DatagramSocket();
					//System.out.println(apptype);
					byte[] arr = require_data.getBytes();
					InetAddress add = InetAddress.getLocalHost();
					DatagramPacket requestshe = new DatagramPacket(arr,arr.length,add,9596);

					ds_she.send(requestshe);

					byte[] fetchdataque = new byte[1000];
					reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
					ds_she.receive(reply_she);

					rep_she = new String(reply_she.getData());
					msg = rep_she.trim();
				}
				catch(Exception e){
					System.out.println("Catch");
					System.out.println(e.getMessage());
				}
			}
			System.out.println(outer);

			if(outer==null){
				System.out.println("Empty");
				msg = "null";
			}
			else {
				msg = "Appointment added";
			}

			log = "\n\naddappointment called by admin on"+dtf.format(now).concat("\nParameter for this method is  "+ apptype+"(Appointment type)"+appid+"(Appointment id)"+capicity+"(Capicity)").concat("\nappointment is added");
			printLog(log);

		}
		catch (Exception e){
			msg = "Exception";
		}
		return msg;
	}

	@Override
	public  String patientappointmentschedule(String pid) {
		System.out.println();
		String msg = "";
		String tmp_pid = pid.substring(0,8);
		log = "\n\n patientappointmentschedule called "+dtf.format(now).concat("\n Parameter for this method is  "+ pid+"(Patient id)");
		printLog(log);

		HashMap<String,String> tmpp = new HashMap<>();
		System.out.println("Hello.."+outer_patient);
		tmpp = outer_patient.get(tmp_pid);

		if(pid.length()>9) {
			System.out.println("inside if");
			if(tmpp==null){
				System.out.println("Empty");
				msg = "You don`t have an appointment here";
			}
			else {
				msg = tmpp.toString();
			}
		}
		else {
			if(tmpp==null||tmpp.isEmpty()){
				System.out.println("Empty");
				msg = "You don`t have an appointment here";
			}
			else {
				msg = tmpp.toString();
			}

			try{
				//msg = tmpp.toString();
				ds_que = new DatagramSocket();

				//System.out.println(apptype);
				String tempp = "get".concat(pid);
				byte[] arr = tempp.getBytes();
				InetAddress add = InetAddress.getLocalHost();

				DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);
				ds_que.send(requestQUE);

				byte[] fetchdataque = new byte[1000];
				reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
				ds_que.receive(reply_que);

				ds_she = new DatagramSocket();
				DatagramPacket requestSHE = new DatagramPacket(arr,arr.length,add,9596);
				ds_she.send(requestSHE);

				byte[] fetchdatashe = new byte[1000];
				reply_she = new DatagramPacket(fetchdatashe, fetchdatashe.length);
				ds_she.receive(reply_she);

				rep_que = new String(reply_que.getData());
				rep_she = new String(reply_she.getData());
				//            System.out.println("Hello rep que"+rep_que.trim());
				//            System.out.println("rep she"+rep_she.trim());
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}

//        requestSHE(apptype);
//        System.out.println("Hello"+outer.get(temp)+rep_que+var);
			msg = "Montreal Result: "+msg+"\n"+"Quebec Result: ".concat(rep_que.trim())+"\n"+"Sherbrook Result: ".concat(rep_she.trim());
		}
		//return msg;
		return "Schedule Found";
	}

	@Override
	public synchronized String cancelPatientAppointment(String p_id, String cancel_id, String cancel_type) {
		log = "\n\n CancelAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ p_id+"(Patient id)"+ cancel_id+"(cancel id)"+ cancel_type+"(App Type)");
		printLog(log);

		String cancel_msg = "";
		HashMap<String,String> old = null;
		if(cancel_id.contains("MTL")){
			if(outer_patient.containsKey(p_id)){
				old = outer_patient.get(p_id);// {MTLP123={P=[MTLM300222]}}
				if(old.containsKey(cancel_type)){  //{P={MTLM1111=[10]}}
					old.remove(cancel_type);
					outer_patient.get(p_id).remove(cancel_type);
					HashMap<String,String> hh = outer.get(cancel_type);    //p={MTLM100200=[10]}
					//System.out.println(hh);

					Set<String> set_str = hh.keySet();
					String str11 = set_str.toString();
					//System.out.println(str11);

					//System.out.println(hh.values());
					Collection<String> hhvalue = hh.values();
					String ff = String.valueOf(hhvalue);
					//System.out.println(ff);//[10]

					String temp = String.valueOf(Integer.valueOf(ff.substring(1,ff.length()-1))+1);
					HashMap<String,String> to_replce = new HashMap<>();
					to_replce.put(str11.substring(1,11), temp);
					outer.replace(cancel_type,to_replce);
					System.out.println("After adding"+outer);
					printLog("\nRequest succesfully compiled");

					cancel_msg = "Your appointment successfully cancelled!!!!!!";
				}

			}

			else
			{
				cancel_msg = "You can`t cancel appointment because there is no appointmnet found for this type";
				printLog("\nYou can`t cancel appointment because there is no appointmnet found for this type");
			}
		}
		else if(cancel_id.contains("QUE")){
			require_data = "cancel".concat(p_id).concat(cancel_id).concat(cancel_type);
			try {
				ds_que = new DatagramSocket();
				//System.out.println(apptype);
				byte[] arr = require_data.getBytes();
				InetAddress add = InetAddress.getLocalHost();
				DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

				ds_que.send(requestQUE);

				byte[] fetchdataque = new byte[1000];
				reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
				ds_que.receive(reply_que);

				rep_que = new String(reply_que.getData());
				cancel_msg = rep_que.trim();
			}
			catch(Exception e){
				System.out.println("Catch");
				System.out.println(e.getMessage());
			}

		}
		else if(cancel_id.contains("SHE")){
			require_data = "cancel".concat(p_id).concat(cancel_id).concat(cancel_type); //cancelMTLP1234MTLE300100P
			try {
				ds_she = new DatagramSocket();
				//System.out.println(apptype);
				byte[] arr = require_data.getBytes();
				InetAddress add = InetAddress.getLocalHost();
				DatagramPacket requestshe = new DatagramPacket(arr,arr.length,add,9596);

				ds_she.send(requestshe);

				byte[] fetchdataque = new byte[1000];
				reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
				ds_she.receive(reply_she);

				rep_she = new String(reply_she.getData());
				cancel_msg = rep_she.trim();
			}
			catch(Exception e){
				System.out.println("Catch");
				System.out.println(e.getMessage());
			}
		}
		else {

		}

		System.out.println("Helllo cancel");
		System.out.println(outer_patient);


		//return cancel_msg;
		return "Cancel Successful";
	}

	@Override
	public synchronized String removeAppointment(String remove_id, String remove_type) {

			log = "\n\n removeAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ remove_id+"(App id)"+ remove_type+"(App type)");
			printLog(log);

			String remove_msg = "";
			if(remove_id.contains("MTL")){
				if(outer.isEmpty()){
					remove_msg = "You can not delete appoitment since there is no appointment found";
					printLog("\nYou can not delete appoitment since there is no appointment found");
				}
				else
				{
					outer.remove(remove_type);
					remove_msg = "Appointment for given type has been removed";
					System.out.println(outer);
					printLog("\nRequest done");
				}
			}
			else if(remove_id.contains("QUE")){
				require_data = "remove".concat(remove_id).concat(remove_type);//removeQUEE300100P
				try {
					ds_que = new DatagramSocket();
					//System.out.println(apptype);
					byte[] arr = require_data.getBytes();
					InetAddress add = InetAddress.getLocalHost();
					DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

					ds_que.send(requestQUE);

					byte[] fetchdataque = new byte[1000];
					reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
					ds_que.receive(reply_que);

					rep_que = new String(reply_que.getData());
					remove_msg = rep_que.trim();
				}
				catch(Exception e){
					System.out.println("Catch");
					System.out.println(e.getMessage());
				}
			}
			else if(remove_id.contains("SHE")){
				require_data = "remove".concat(remove_id).concat(remove_type);//removeQUEE300100P
				try {
					ds_she = new DatagramSocket();
					//System.out.println(apptype);
					byte[] arr = require_data.getBytes();
					InetAddress add = InetAddress.getLocalHost();
					DatagramPacket requestshe = new DatagramPacket(arr,arr.length,add,9596);

					ds_she.send(requestshe);

					byte[] fetchdataque = new byte[1000];
					reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
					ds_she.receive(reply_she);

					rep_she = new String(reply_she.getData());
					remove_msg = rep_she.trim();
				}
				catch(Exception e){
					System.out.println("Catch");
					System.out.println(e.getMessage());
				}
			}
			else {}


		return "Remove Successful";
	}

	@Override
	public  void invoke() {
		try {
			writer.write("\n");
			writer.close();

		} catch (IOException ex) {
			Logger.getLogger(MontrealImpl.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String listAppointment(String apptype) {
		printLog("\n\nList appointment has been called by mtl admin for the type "+apptype);
		String temp = "";
		if(apptype.contains("Phy")){
			temp = "P";
		}
		else if(apptype.contains("Sur")){
			temp = "S";
		}
		else if(apptype.contains("Den")){
			temp = "D";
		}
		String mtldata = "";

		System.out.println("HelloMTL"+outer.get(temp));
		if(outer.get(temp)==null){
			mtldata = "No Appointment Available";
		}
		else {
			mtldata = outer.get(temp).toString();
		}

		DatagramSocket ds_que = null;
		DatagramSocket ds_she = null;
		DatagramPacket reply_que = null;
		DatagramPacket reply_she = null;
		String rep_que = null;
		String rep_she = null;

		try {
			ds_que = new DatagramSocket();

			//System.out.println(apptype);
			String tempp = "list".concat(apptype);
			byte[] arr = tempp.getBytes();
			InetAddress add = InetAddress.getLocalHost();

			DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);
			ds_que.send(requestQUE);

			byte[] fetchdataque = new byte[1000];
			reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
			ds_que.receive(reply_que);

			ds_she = new DatagramSocket();
			DatagramPacket requestSHE = new DatagramPacket(arr,arr.length,add,9596);
			ds_she.send(requestSHE);

			byte[] fetchdatashe = new byte[1000];
			reply_she = new DatagramPacket(fetchdatashe, fetchdatashe.length);
			ds_she.receive(reply_she);

			rep_que = new String(reply_que.getData());
			rep_she = new String(reply_she.getData());
			System.out.println("Hello rep que"+rep_que.trim());
			System.out.println("rep she"+rep_she.trim());
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

//        requestSHE(apptype);
//        System.out.println("Hello"+outer.get(temp)+rep_que+var);
		send = "Montreal Result: "+mtldata+"\n"+"Quebec Result: ".concat(rep_que.trim())+"\n"+"Sherbrook Result: ".concat(rep_she.trim());
		//return send;
		return "List Found";
	}

	@Override
	public synchronized String swapAppointment(String p_id, String oldAppID, String oldAppType, String newAppID, String newAppType) {
		log = "\n\n SwapAppointment called "+dtf.format(now).concat("\n Parameter for this method is  "+ p_id+"(P_id)"+ oldAppID+"(Old App id)"+oldAppType+" "+newAppID+" "+newAppType);
		printLog(log);

		String msg = "";
		if(outer_patient.containsKey(p_id)){ //MTLP1234= P = [MTLM101010]
			HashMap<String, String> inner_patient = outer_patient.get(p_id);
			if(newAppType.equals(oldAppType)&&newAppID.contains("MTL")){
				msg = "You can`t swap appointment with same type in your city";
			}
			else {
				if(inner_patient.containsKey(oldAppType)){
					if(newAppID.contains("QUE")){

						require_data = p_id.concat(newAppID).concat(newAppType).concat("swap");
						System.out.println(require_data);

						try {
							ds_que = new DatagramSocket();

							//System.out.println(apptype);
							byte[] arr = require_data.getBytes();
							InetAddress add = InetAddress.getLocalHost();
							DatagramPacket requestQUE = new DatagramPacket(arr,arr.length,add,9595);

							ds_que.send(requestQUE);

							byte[] fetchdataque = new byte[1000];
							reply_que = new DatagramPacket(fetchdataque, fetchdataque.length);
							ds_que.receive(reply_que);//

							rep_que = new String(reply_que.getData());
							cancelPatientAppointment(p_id,oldAppID,oldAppType);
							msg = "Old Appointment has been replaced by New Appointment!!!";

						}
						catch(Exception e){
							System.out.println("Catch");
							System.out.println(e.getMessage());
						}
					}
					else if(newAppID.contains("SHE")) {
						DatagramSocket ds_SHE = null;
						DatagramPacket reply_she = null;
						String rep_she = null;
						String require_data = p_id.concat(newAppID).concat(newAppType).concat("swap");

						try {
							ds_SHE = new DatagramSocket();

							//System.out.println(apptype);
							byte[] arr = require_data.getBytes();
							InetAddress add = InetAddress.getLocalHost();
							DatagramPacket requestSHE = new DatagramPacket(arr,arr.length,add,9596);

							ds_SHE.send(requestSHE);

							byte[] fetchdataque = new byte[1000];
							reply_she = new DatagramPacket(fetchdataque, fetchdataque.length);
							ds_SHE.receive(reply_she);//

							rep_she = new String(reply_she.getData());
							cancelPatientAppointment(p_id,oldAppID,oldAppType);
							msg = "Old Appointment has been replaced by New Appointment!!!";

						}
						catch(Exception e){
							System.out.println(e.getMessage());
						}


					}
					else
					{
						cancelPatientAppointment(p_id,oldAppID,oldAppType);
						bookappointment(p_id,newAppType);
						msg = "Old Appointment has been replaced by New Appointment!!!";
					}
				}
				else{
					msg = "Something went wrong";
				}
			}

		}
		//return msg;
		return "Swap Successful";
	}

	public void acceptUDP() {
		try{
			ds_swap = new DatagramSocket(9597);
			byte [] arr = new byte[1000];
			while(true){
				// System.out.println("Sendswap");
				DatagramPacket req = new DatagramPacket(arr,arr.length);
				ds_swap.receive(req);
				//System.out.println("Sendswapafterreq");
				String msg1 = new String(req.getData());
				System.out.println(msg1.trim());
				//System.out.println("Sendswapafterall");
				if(msg1.contains("book")){
					System.out.println("book");//bookMTLP1234P
					//bookMTLP1234
					msg_udp = bookappointment(msg1.substring(4,12), msg1.substring(12).trim());
					byte [] data = msg_udp.getBytes();
					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("get")){
					msg_udp = patientappointmentschedule(msg1.substring(3).trim().concat("udp")); //getMTLP1234
					byte [] data = msg_udp.getBytes();
					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("cancel")){
					msg_udp = cancelPatientAppointment(msg1.substring(6,14),msg1.substring(14,24),msg1.substring(24).trim()); ////cancelMTLP1234MTLE300100P
					byte [] data = msg_udp.getBytes();
					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("swap")){
					System.out.println("crgvhbjn");
					String p_id = msg1.substring(0,8);  //MTLP1234QUEE300100P
					String apptype = msg1.substring(18,19);
					//String p_id = msg1.substring(0,7);

					bookappointment(p_id,apptype);
					msg_udp = "done";

					byte [] data = msg_udp.getBytes();

					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("add")){//addPQUEE30010050
					msg_udp = addappointment(msg1.substring(3,4),msg1.substring(4,14),msg1.substring(14).trim()); ////cancelMTLP1234MTLE300100P
					byte [] data = msg_udp.getBytes();
					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("remove")){//removeQUEE300100P
					msg_udp = removeAppointment(msg1.substring(6,16),msg1.substring(16).trim());
					byte [] data = msg_udp.getBytes();
					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}
				else if(msg1.contains("list")){
					System.out.println("inside if");
					System.out.println(msg1.substring(4,5));
					System.out.println(outer);
					if(outer.get(msg1.substring(4,5))==null){
						String msg = "No Appointments Available";
						byte [] data = msg.getBytes();
						DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
						ds_swap.send(reply);
					}
					else{
						String msg = outer.get(msg1.substring(4,5)).toString();

						byte [] data = msg.getBytes();

						DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
						ds_swap.send(reply);
					}

				}
				else if(msg1.contains("send")){//sendMTLP1234
					byte [] data;
					HashMap<String,String> hashMap = outer_patient.get(msg1.substring(4,12).trim());

					if(hashMap==null){
						data = "0".getBytes();
					}
					else{

						msg_udp = String.valueOf(hashMap.keySet().stream().count());
						System.out.println(msg_udp);
						data = msg_udp.getBytes();
					}

					DatagramPacket reply = new DatagramPacket(data,data.length,req.getAddress(),req.getPort());
					ds_swap.send(reply);
				}


			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void printLog(String log){
		try {
			writer.write(log);
			Logger.getLogger(MontrealImpl.class.getName()).log(Level.SEVERE, log);

		} catch (IOException ex) {
			Logger.getLogger(MontrealImpl.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String fault(String adminid){
		String msg = "";
		try{
			int x = 5/0;
		}
		catch (Exception e){
			msg = "Fault";
			cnt_fault++;
		}
		return msg.concat(String.valueOf(cnt_fault));
	}

	@Override
	public String shutDown() throws RemoteException {
		outer = new HashMap<>();
		outer_patient = new HashMap<>();
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
