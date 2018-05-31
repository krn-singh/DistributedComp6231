/**
 * The package contains the all the clients which have an access to the server
 */
package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.CenterServer;

/**
 * The class represents the manager client. Depending upon the region to which
 * manager belongs, the data access is provided to the manager to perform the
 * required operations after successful validation.
 * 
 * @author karan
 */
public class ManagerClient {

	public void fetchServer(String serverName) {

		try {
			//
			Registry registry = LocateRegistry.getRegistry(1234);
			CenterServer serverObj = (CenterServer) registry.lookup(serverName);
			System.out.println(serverObj.getClass().toString());
			serverObj.createTRecord("Lei", "Shan", "Rue Mackay", "514514", "french", "mtl");
			serverObj.createTRecord("Lei", "Vhan", "Rue Mackay", "514514", "french", "mtl");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
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

		ManagerClient client = new ManagerClient();
		client.fetchServer("MTL");
		
	}
}
