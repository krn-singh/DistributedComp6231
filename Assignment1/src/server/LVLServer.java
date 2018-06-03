/**
 * The package contains the server classes 
 */
package server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
import utility.Student;
import utility.Teacher;

/**
 * The class performs the operations regarding the Laval(MTL) center.
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class LVLServer extends UnicastRemoteObject implements CenterServer {
	public static HashMap<String, ArrayList<Record>> lvlDB;
	private static HashMap<String, String> idToLastName;	
	private static int count;
	private LogManager lvlLogger;
	static String location = "lvl";
	public static int LVLport = 2345;

	public LVLServer() throws Exception {
		super();
		lvlDB = new HashMap<String, ArrayList<Record>>();
		count = 0;
		lvlLogger = new LogManager("lvl");
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) throws RemoteException {
		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		if (lvlDB.containsKey(lastName.substring(0, 1))) {
			lvlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			lvlDB.put(lastName.substring(0, 1), alRecord);
		}

//		for (Map.Entry<String, ArrayList<Record>> map : lvlDB.entrySet()) {
//
//			System.out.println("Map key & value" + map.getKey() + "," + map.getValue().size());
//
//		}
		
		idToLastName.put(objRecord.getRecordId(), lastName.substring(0,1));


		lvlLogger.mLogger.info("Creating Teacher Record with First Name: "+ firstName + " Last name: " + lastName + " Address: " +
			address + " Phone number: " + phone + " Specialization: " + specialization + " location: " + location + '\n');
		
		count++;
		
		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate, String managerId) throws RemoteException {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		//checking if the key already exists in hash map
		if (lvlDB.containsKey(lastName.substring(0, 1))) {
			lvlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			lvlDB.put(lastName.substring(0, 1), alRecord);
		}
		
		idToLastName.put(objRecord.getRecordId(), lastName.substring(0,1));

		count++;
		
		lvlLogger.mLogger.info(managerId + " sent request to create Student Record with First Name: "+ firstName + " Last name: "
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
				lvlLogger.mLogger.info(managerId + " sent request for total record count" + '\n');
				socket1 = new DatagramSocket();
				socket2 = new DatagramSocket();
				InetAddress address = InetAddress.getByName("localhost");
				
				DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, MTLServer.MTLport);
				socket1.send(request1);
				lvlLogger.mLogger.info(location + " sever sending request to mtl sever for total record count" + '\n');

				byte[] receive1 = new byte[1000];
				DatagramPacket reply1 = new DatagramPacket(receive1, receive1.length);
				socket1.receive(reply1);
				lvlLogger.mLogger.info("mtl server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply1.getData()));
				str = str.trim();
				str = str.concat("\n");

				DatagramPacket request2 = new DatagramPacket(message2, message2.length, address, DDOServer.DDOport);
				socket2.send(request2);
				lvlLogger.mLogger.info(location + " sever sending request to ddo sever for total record count" + '\n');
				
				byte[] receive2 = new byte[1000];
				DatagramPacket reply2 = new DatagramPacket(receive2, receive2.length);
				socket2.receive(reply2);
				lvlLogger.mLogger.info("ddo server sent response to " + location + " sever for total record count " + '\n');

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
			
		System.out.println(str);
		lvlLogger.mLogger.info("Get record count query is used, total count is : \n" + str + '\n');
		return str;
	}

	// Method to edit status,statusdate(Student) &&
	// Address,phone,specialization(Teacher)
	@Override
	public String editRecord(String recordId, String fieldName, String newValue, String managerId) throws RemoteException {

		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else
			return "The given record id doesn't exist";

		StringBuilder output = new StringBuilder();
		for (Record temp : lvlDB.get(key)) {
			String id = temp.getRecordId();
			if (id.equalsIgnoreCase(recordId)) {
				if (recordId.startsWith("ST")) {
					if (fieldName.equalsIgnoreCase("status")) {
						output.append(printMessage(((Student) temp).getStatus(), newValue));
						((Student) temp).setStatus(newValue);
					} else if (fieldName.equalsIgnoreCase("statusDate")) {
						output.append(printMessage(((Student) temp).getStatusDate(), newValue));
						((Student) temp).setStatusDate(newValue);
					} else {
						return "The given field name is invalid for student record";
					}
				} else if (recordId.startsWith("TR")) {
					if (fieldName.equalsIgnoreCase("address")) {
						output.append(printMessage(((Teacher) temp).getAddress(), newValue));
						((Teacher) temp).setAddress(newValue);
					} else if (fieldName.equalsIgnoreCase("phone")) {
						output.append(printMessage(((Teacher) temp).getPhone(), newValue));
						((Teacher) temp).setPhone(newValue);
					} else if (fieldName.equalsIgnoreCase("specialization")) {
						output.append(printMessage(((Teacher) temp).getSpecialization(), newValue));
						((Teacher) temp).setSpecialization(newValue);
					} else {
						return "The given field name is invalid for teacher record";
					}
				}
			}
		}

		lvlLogger.mLogger.info(managerId + " sent request to edit Record with ID: "+ recordId +  " new value is: " + newValue +'\n');
		return output.toString();
	}

	public String printMessage(String str1, String str2) {
		return "Old Value:" + str1 + " " + " New value updated:" + str2;

	}

	// Method to add Course registered (Student)
	public String editRecord(String recordId, String fieldName, ArrayList<String> newValue, String managerId) throws RemoteException {

		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else
			return "The given record id doesn't exist";
		StringBuilder output = new StringBuilder();
		for (Record temp : lvlDB.get(key)) {
			if (temp.getRecordId() == recordId && recordId.startsWith("ST")
					&& fieldName.equalsIgnoreCase("courseRegistered")) {
				output.append(printMessage(((Student) temp).getCourseRegistered().toString(), newValue.toString()));
				((Student) temp).setCourseRegistered(newValue);
			} else {
				return "The given field name is invalid for student record";
			}
		}
		lvlLogger.mLogger.info(managerId + " sent request to edit Record with ID: "+ recordId +  " new value is: " + newValue +'\n');

		return output.toString();
	}

	public void printHashMap() throws RemoteException {
				 
		for (Map.Entry<String, ArrayList<Record>> map : lvlDB.entrySet()) {
				
				 System.out.println("Key: " + map.getKey());
				 for(Record r:map.getValue()) {
					 System.out.println();
					 if(r.getRecordId().startsWith("ST"))
						 System.out.println(String.format("LN: %s\nFN: %s\nID: %s\nStatus: %s\nStatus Date: %s\n",r.getLastName(),r.getFirstName(),r.getRecordId(), ((Student)r).getStatus(), ((Student)r).getStatusDate()));
					 else if(r.getRecordId().startsWith("TR"))
						 System.out.println(String.format("LN: %s\nFN: %s\nID: %s\naddress: %s\nphone: %s\n",r.getLastName(),r.getFirstName(),r.getRecordId(), ((Teacher)r).getAddress(), ((Teacher)r).getPhone()));
				 }
				System.out.println();
				 }

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
			Registry registry = LocateRegistry.createRegistry(LVLport);
			LVLServer laval = new LVLServer();
			registry.bind(location, laval);
			System.out.println("Laval Server is started");
			
			DatagramSocket socket = null;
			try {
				
				socket = new DatagramSocket(LVLport);
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
