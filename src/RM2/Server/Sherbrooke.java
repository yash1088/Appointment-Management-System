package RM2.Server;

import RM2.ImplementRemoteInterface.SherbrookeImple;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Sherbrooke {
	public static void main(String args[]) throws Exception
	{
		try {
			SherbrookeImple stub = new SherbrookeImple();
			Registry registry = LocateRegistry.createRegistry(9993);
			registry.bind("SHE", stub);

			Runnable task = () -> {
				stub.acceptUDP();
			};
			Thread thread = new Thread(task);
			thread.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
