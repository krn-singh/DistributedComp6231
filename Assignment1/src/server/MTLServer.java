/**
 * The package contains the server classes 
 */
package server;

import java.rmi.AlreadyBoundException;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import utility.Record;
import utility.Teacher;

/**
 * The class performs the operations regarding the Montreal(MTL) center.
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class MTLServer extends UnicastRemoteObject implements CenterServer {

	public static HashMap<String, ArrayList<Record>> mtlDB;
	private static int count;

	public MTLServer() throws Exception {
		super();
		mtlDB = new HashMap<String, ArrayList<Record>>();
		count = 0;
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location) throws RemoteException {

		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			mtlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		for (Map.Entry<String, ArrayList<Record>> map : mtlDB.entrySet()) {

			System.out.println("Map key & value" + map.getKey() + "," + map.getValue().size());

		}

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
		for (Map.Entry<String, ArrayList<Record>> map : mtlDB.entrySet()) {

			for (Record eachRec : map.getValue()) {
				if (returnStringAfterDot(eachRec.getClass().getName(), ".").equals("Student")) {
					System.out.println("\nRetrieving Student Data");
					getDataFromRunTimeClass(eachRec);

				} else if (returnStringAfterDot(eachRec.getClass().getName(), ".").equals("Teacher")) {
					System.out.println("\nRetrieving Teacher Data");
					getDataFromRunTimeClass(eachRec);
				}
			}
		}
		System.out.println("\nTotal No.of records retrieved:" + count);
		return null;
	}

	@Override
	public boolean editRecord(String recordId, String fieldName, String newValue) throws RemoteException {
		// TODO Auto-generated method stub

		return false;
	}

	// static user defined methods

	static String returnStringAfterDot(String value, String a) {
		int posA = value.lastIndexOf(a);
		if (posA == -1) {
			return "";
		}
		int adjustedPosA = posA + a.length();
		if (adjustedPosA >= value.length()) {
			return "";
		}
		return value.substring(adjustedPosA);
	}

	static void getDataFromRunTimeClass(Object obj) {
		Class<?> objClass = obj.getClass();
		Field[] fields = objClass.getFields();
		for (Field field : fields) {
			String name = field.getName(); // objects of class - eg:id
			Object value = null;
			try {
				value = field.get(obj);// id value eg:1
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			System.out.println(name + ": " + value.toString());
		}
		count = count + 1;
	}

	/**
	 * Main Method.
	 * 
	 * @param args (No arguments are needed to launch)
	 */
	public static void main(String[] args) {
		
		try {
			Registry registry = LocateRegistry.createRegistry(1234);
			MTLServer montreal = new MTLServer();
			registry.bind("MTL", montreal);

			System.out.println("Montreal Server is started");
		} catch (RemoteException e)			{	e.printStackTrace();		}
		  catch (AlreadyBoundException e) 	{	e.printStackTrace();		}
		  catch (Exception e) 				{	e.printStackTrace();		}
	}

}