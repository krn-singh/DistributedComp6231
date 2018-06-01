/**
 * The package contains the server classes 
 */
package server;

import java.lang.reflect.Field;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import logger.LogManager;
import utility.Record;
import utility.Teacher;

/**
 * The class performs the operations regarding the Laval(MTL) center.
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class LVLServer extends UnicastRemoteObject implements CenterServer {
	public static HashMap<String, ArrayList<Record>> lvlDB;
	private static int count;
	private LogManager lvlLogger;

	public LVLServer() throws Exception {
		super();
		lvlDB = new HashMap<String, ArrayList<Record>>();
		count = 0;
		lvlLogger = new LogManager("LVL");
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location) throws RemoteException {
		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		if (lvlDB.containsKey(lastName.substring(0, 1))) {
			lvlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			lvlDB.put(lastName.substring(0, 1), alRecord);
		}

		for (Map.Entry<String, ArrayList<Record>> map : lvlDB.entrySet()) {

			System.out.println("Map key & value" + map.getKey() + "," + map.getValue().size());

		}
		lvlLogger.mLogger.info("Creating Teacher Record with First Name: "+ firstName + " Last name: "
				+ lastName + " Address: " + address + " Phone number: " + phone
				+ " Specialization: " + specialization + '\n');
		
		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate) throws RemoteException {
		// TODO Auto-generated method stub
		lvlLogger.mLogger.info("Creating Student Record with First Name: "+ firstName + " Last name: "
				+ lastName + " Course: " + courseRegistered + " Status: " + status
				+ " Status Date: " + statusDate + '\n');
		return true;
	}

	@Override
	public String getRecordCounts() throws RemoteException {
		// TODO Auto-generated method stub
		for (Map.Entry<String, ArrayList<Record>> map : lvlDB.entrySet()) {

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
		
		lvlLogger.mLogger.info("Get record count query is used, total count is : " + count + '\n');
		return null;
	}

	@Override
	public boolean editRecord(String recordId, String fieldName, String newValue) throws RemoteException {
		// TODO add previous value here;
		lvlLogger.mLogger.info("Editing Record with ID:"+ recordId + " previous value was: " + " "  + "new value is: " + newValue +'\n');
		// TODO Auto-generated method stub
		return false;
	}
	
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
			Registry registry = LocateRegistry.createRegistry(2345);
			LVLServer laval = new LVLServer();
			registry.bind("LVL", laval);

			System.out.println("Laval Server is started");
		} catch (RemoteException e)			{	e.printStackTrace();		}
		  catch (AlreadyBoundException e) 	{	e.printStackTrace();		}
		  catch (Exception e) 				{	e.printStackTrace();		}
	}
}
