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
import java.util.Iterator;
import java.util.Map;

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

	public static HashMap<String, ArrayList<Record>> mtlDB = new HashMap<String, ArrayList<Record>>();
	private static HashMap<String, String> idToLastName = new HashMap<String, String>();
	private static int count = 0;
	private LogManager mtlLogger;
	static String location = "mtl";
	public static int MTLport = 1234;

	public MTLServer() throws Exception {
		super();
		mtlLogger = new LogManager("mtl");
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location) throws RemoteException {

		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		// checking if the key already exists in hash map
		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			mtlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;
		
		// adding the operation to the log file
		mtlLogger.mLogger.info("Creating Teacher Record with First Name: " + firstName + " Last name: " + lastName
				+ " Address: " + address + " Phone number: " + phone + " Specialization: " + specialization + '\n');

		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate) throws RemoteException {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		// checking if the key already exists in hash map
		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			mtlDB.get(lastName.substring(0, 1)).add(objRecord);
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;

		// adding the operation to the log file
		mtlLogger.mLogger.info("Creating Student Record with First Name: " + firstName + " Last name: " + lastName
				+ " Course: " + courseRegistered + " Status: " + status + " Status Date: " + statusDate + '\n');
		return true;
	}

	@Override
	public String getRecordCounts() throws RemoteException {

		String str = location + " " + count + "\n";

		DatagramSocket socket1 = null;
		DatagramSocket socket2 = null;
		byte[] message1 = location.getBytes();
		byte[] message2 = location.getBytes();

		try {
			socket1 = new DatagramSocket();
			socket2 = new DatagramSocket();
			InetAddress address = InetAddress.getByName("localhost");

			DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, LVLServer.LVLport);
			socket1.send(request1);

			byte[] receive1 = new byte[1000];
			DatagramPacket reply1 = new DatagramPacket(receive1, receive1.length);
			socket1.receive(reply1);

			str = str.concat(new String(reply1.getData()));
			str = str.trim();
			str = str.concat("\n");

			DatagramPacket request2 = new DatagramPacket(message2, message2.length, address, DDOServer.DDOport);
			socket2.send(request2);

			byte[] receive2 = new byte[1000];
			DatagramPacket reply2 = new DatagramPacket(receive2, receive2.length);
			socket2.receive(reply2);

			str = str.concat(new String(reply2.getData()));
			str = str.trim();
			str = str.concat("\n");

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			socket1.close();
			socket2.close();
		}

		mtlLogger.mLogger.info("Get record count query is used, total count is : \n" + str + '\n');
		return str;
	}

	// Method to edit status,statusdate(Student) &&
	// Address,phone,specialization(Teacher)
	@Override
	public String editRecord(String recordId, String fieldName, String newValue) throws RemoteException {

		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else
			return "The given record id doesn't exist";

		StringBuilder output = new StringBuilder();
		for (Record temp : mtlDB.get(key)) {
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

		mtlLogger.mLogger.info("Editing Record with ID:" + recordId + " previous value was: " + " " + "new value is: "
				+ newValue + '\n');
		return output.toString();
	}

	public String printMessage(String str1, String str2) {
		return "Old Value:" + str1 + " " + " New value updated:" + str2;

	}

	// Method to add Course registered (Student)
	public String editRecord(String recordId, String fieldName, ArrayList<String> newValue) throws RemoteException {

		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else
			return "The given record id doesn't exist";
		StringBuilder output = new StringBuilder();
		for (Record temp : mtlDB.get(key)) {
			if (temp.getRecordId() == recordId && recordId.startsWith("ST")
					&& fieldName.equalsIgnoreCase("courseRegistered")) {
				output.append(printMessage(((Student) temp).getCourseRegistered().toString(), newValue.toString()));
				((Student) temp).setCourseRegistered(newValue);
			} else {
				return "The given field name is invalid for student record";
			}
		}
		mtlLogger.mLogger.info("Editing Record with ID:" + recordId + " previous value was: " + " " + "new value is: "
				+ newValue + '\n');

		return output.toString();
	}

	public void printHashMap() throws RemoteException {
				 
		for (Map.Entry<String, ArrayList<Record>> map : mtlDB.entrySet()) {
				
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
	
	/**
	 * Main Method.
	 * 
	 * @param args
	 *            (No arguments are needed to launch)
	 */
	public static void main(String[] args) {

		try {
			Registry registry = LocateRegistry.createRegistry(1234);
			MTLServer montreal = new MTLServer();
			registry.bind(location, montreal);
			System.out.println("Montreal Server is started");
			MTLServer serverObj = new MTLServer();
			serverObj.createTRecord("firstName", "lastName", "vfgfggdfg", "status", "statusDate", "fsf");
			System.out.println("created");
			serverObj.editRecord("TR100", "phone", "active1213");
			serverObj.printHashMap();
			
			DatagramSocket socket = null;
			try {

				socket = new DatagramSocket(MTLport);
				byte[] get = new byte[256];
				byte[] send = new byte[1000];

				while (true) {
					DatagramPacket request = new DatagramPacket(get, get.length);
					socket.receive(request);

					send = (location + " " + count).getBytes();
					DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(),
							request.getPort());
					socket.send(reply);
				}

			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				socket.close();
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}