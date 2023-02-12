	package RM2.Server;

	import RM2.ImplementRemoteInterface.QuebecImple;

	import java.rmi.registry.Registry;
	import java.rmi.registry.LocateRegistry;


    public class Quebec {
		public static void main(String args[]) throws Exception
		{
			try {
				QuebecImple stub = new QuebecImple();
				Registry registry = LocateRegistry.createRegistry(9991);
				registry.rebind("QUE", stub);

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
