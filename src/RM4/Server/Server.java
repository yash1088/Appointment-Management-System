package RM4.Server;

import RM2.Server.Sherbrooke;

public class Server {
	public static void main(String[] args) {
		new Thread() {
			@Override 
			public void run() {
				try {
					MTLServer.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		new Thread() {
			@Override 
			public void run() {
				try {
					QUEServer.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		
		new Thread() {
			@Override 
			public void run() {
				try {
					SHEServer.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		
/*		new Thread() {
			@Override 
			public void run() {
				try {
					Client1.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();*/
	}
}
