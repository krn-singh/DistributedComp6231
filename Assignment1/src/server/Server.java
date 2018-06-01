package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

	/**
	 * Starts the server
	 * 
	 * @throws Exception
	 */
	
	public void startServer() {

		try {
			Registry registry = LocateRegistry.createRegistry(1234);
			DDOServer dollard = new DDOServer();
			LVLServer laval = new LVLServer();
			MTLServer montreal = new MTLServer();
			registry.bind("DDO", dollard);
			registry.bind("LVL", laval);
			registry.bind("MTL", montreal);
			System.out.println(registry.list().length);
			System.out.println("Server is started");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main Method.
	 * 
	 * @param args
	 *            (No arguments are needed to launch)
	 */
	public static void main(String[] args) {

		Server server = new Server();
		server.startServer();

	}

}