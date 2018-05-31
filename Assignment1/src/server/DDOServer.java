/**
 * The package contains the server classes 
 */
package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * The class performs the operations regarding the Dollard(MTL) center
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class DDOServer extends UnicastRemoteObject implements CenterServer {

	public DDOServer() throws Exception {
		super();
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRecordCounts() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean editRecord(String recordId, String fieldName, String newValue) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Main Method.
	 * 
	 * @param args (No arguments are needed to launch)
	 */
	public static void main(String[] args) {
		
		try {
			Registry registry = LocateRegistry.createRegistry(3456);
			DDOServer dollard = new DDOServer();
			registry.bind("DDO", dollard);

			System.out.println("Dollard Server is started");
		} catch (RemoteException e)			{	e.printStackTrace();		}
		  catch (AlreadyBoundException e) 	{	e.printStackTrace();		}
		  catch (Exception e) 				{	e.printStackTrace();		}
	}
}
