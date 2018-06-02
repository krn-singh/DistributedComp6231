/**
 * The package contains the server classes 
 */
package server;

import java.rmi.AlreadyBoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import logger.LogManager;

import utility.Record;
import utility.Teacher;
import utility.Student;

/**
 * The class performs the operations regarding the Montreal(MTL) center.
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class MTLServer extends UnicastRemoteObject implements CenterServer {

	public static HashMap<String, ArrayList<Record>> mtlDB;
	private static int count;
	private LogManager mtlLogger;
	static String location = "mtl";
	public static int MTLport = 1234;

	public MTLServer() throws Exception {
		super();
		mtlDB = new HashMap<String, ArrayList<Record>>();
		count = 0;
		mtlLogger = new LogManager("mtl");
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) throws RemoteException {

		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);
		
		//checking if the key already exists in hash map
		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			mtlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		count++;
		
		//adding the operation to the log file
		mtlLogger.mLogger.info(managerId + " sent request to create Teacher Record with First Name: "+ firstName + " Last name: "
				+ lastName + " Address: " + address + " Phone number: " + phone
				+ " Specialization: " + specialization + " location: " + location + '\n');
		
		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate, String managerId) throws RemoteException {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		//checking if the key already exists in hash map
		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			mtlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}
		
		count++;

		//adding the operation to the log file
		mtlLogger.mLogger.info(managerId + " sent request to create Student Record with First Name: "+ firstName + " Last name: "
				+ lastName + " Course: " + courseRegistered + " Status: " + status
				+ " Status Date: " + statusDate + '\n');
		return true;
	}

	@Override
	public String getRecordCounts(String managerId) throws RemoteException {
		

		String str = location + " " + count + "\n";

		DatagramSocket socket1 = null;
		DatagramSocket socket2 = null;
		byte[] message1 = location.getBytes();
		byte[] message2 = location.getBytes();
		
			try {
				mtlLogger.mLogger.info(managerId + " sent request for total record count" + '\n');
				
				socket1 = new DatagramSocket();
				socket2 = new DatagramSocket();
				InetAddress address = InetAddress.getByName("localhost");
				
				DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, LVLServer.LVLport);
				socket1.send(request1);
				mtlLogger.mLogger.info(location + " sever sending request to laval sever for total record count" + '\n');

				byte[] receive1 = new byte[1000];
				DatagramPacket reply1 = new DatagramPacket(receive1, receive1.length);
				socket1.receive(reply1);
				mtlLogger.mLogger.info("laval server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply1.getData()));
				str = str.trim();
				str = str.concat("\n");

				DatagramPacket request2 = new DatagramPacket(message2, message2.length, address, DDOServer.DDOport);
				socket2.send(request2);
				mtlLogger.mLogger.info(location + " sever sending request to ddo sever for total record count" + '\n');

				byte[] receive2 = new byte[1000];
				DatagramPacket reply2 = new DatagramPacket(receive2, receive2.length);
				socket2.receive(reply2);
				mtlLogger.mLogger.info("ddo server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply2.getData()));
				str = str.trim();
				str = str.concat("\n");
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				socket1.close();
				socket2.close();
			}

		mtlLogger.mLogger.info("Total record count is : \n" + str + '\n');
		return str;
	}
	

	@Override
	public boolean editRecord(String recordId, String fieldName, String newValue, String managerId) throws RemoteException {
		// TODO add previous value here;
		mtlLogger.mLogger.info(managerId + " sent request to edit Record with ID:"+ recordId + " previous value was: " + " "  + "new value is: " + newValue +'\n');
		// TODO Auto-generated method stub
		
		return true;
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
			registry.bind(location, montreal);
			System.out.println("Montreal Server is started");
			
			DatagramSocket socket = null;
				try {
					
					socket = new DatagramSocket(MTLport);
					byte[] get = new byte[256];
					byte[] send = new byte[1000];
					
					while(true) {
						DatagramPacket request = new DatagramPacket(get, get.length);
						socket.receive(request);

						send = (location + " " + count).getBytes();
						DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(), request.getPort());
						socket.send(reply);
					}
					
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					socket.close();
				}
				
			
		} catch (RemoteException e)			{	e.printStackTrace();		}
		  catch (AlreadyBoundException e) 	{	e.printStackTrace();		}
		  catch (Exception e) 				{	e.printStackTrace();		}
	}

}