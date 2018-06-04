/**
 * The package contains the server classes 
 */
package server;

import java.rmi.AlreadyBoundException;
import java.io.IOException;
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
		mtlLogger.mLogger.setUseParentHandlers(true);

	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) throws RemoteException {

		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		// checking if the key already exists in hash map
		if (mtlDB.containsKey(lastName.substring(0, 1))) {

			// Synchronizing array list for particular key of hashmap
			synchronized (mtlDB.get(lastName).subList(0, 1)) {
				mtlDB.get(lastName.substring(0, 1)).add(objRecord);
			}

		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;

		// adding the operation to the log file
		mtlLogger.mLogger.info(managerId + " created Teacher record with values: " + objRecord + '\n');

		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate, String managerId) throws RemoteException {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		// checking if the key already exists in hash map

		if (mtlDB.containsKey(lastName.substring(0, 1))) {
			// Synchronizing array list for particular key of hashmap
			synchronized (mtlDB.get(lastName).subList(0, 1)) {
				mtlDB.get(lastName.substring(0, 1)).add(objRecord);
			}
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			mtlDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;

		// adding the operation to the log file
		mtlLogger.mLogger.info(managerId + " created Student record with values: " + objRecord + '\n');

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
			mtlLogger.mLogger
					.info("laval server sent response to " + location + " sever for total record count " + '\n');

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
		} finally {
			socket1.close();
			socket2.close();
		}

		mtlLogger.mLogger.info("Total record count is : \n" + str + '\n');
		return str;
	}

	// Method to edit status,statusdate(Student) &&
	// Address,phone,specialization(Teacher)
	@Override
	public String editRecord(String recordId, String fieldName, String newValue, String managerId)
			throws RemoteException {
		mtlLogger.mLogger.info(
				managerId + " sent request to edit Record with ID: " + recordId + " new value is: " + newValue + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			mtlLogger.mLogger.info("Record couldn't be updated as record value: " + recordId + " doesnt exist" + "\n");
			return "The given record id doesn't exist";
		}

		StringBuilder output = new StringBuilder();
		synchronized (mtlDB.get(key)) {

			for (Record temp : mtlDB.get(key)) {
				String id = temp.getRecordId();
				if (id.equalsIgnoreCase(recordId)) {
					if (recordId.startsWith("ST")) {
						if (fieldName.equalsIgnoreCase("status")) {
							output.append(printMessage(((Student) temp).getStatus(), newValue));
							((Student) temp).setStatus(newValue);
							mtlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("statusDate")) {
							output.append(printMessage(((Student) temp).getStatusDate(), newValue));
							((Student) temp).setStatusDate(newValue);
							mtlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else {
							return "The given field name is invalid for student record";
						}
					} else if (recordId.startsWith("TR")) {
						if (fieldName.equalsIgnoreCase("address")) {
							output.append(printMessage(((Teacher) temp).getAddress(), newValue));
							((Teacher) temp).setAddress(newValue);
							mtlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("phone")) {
							output.append(printMessage(((Teacher) temp).getPhone(), newValue));
							((Teacher) temp).setPhone(newValue);
							mtlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("specialization")) {
							output.append(printMessage(((Teacher) temp).getSpecialization(), newValue));
							((Teacher) temp).setSpecialization(newValue);
							mtlLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else {
							return "The given field name is invalid for teacher record";
						}
					}
				}
			}
		}
		return output.toString();
	}

	public String printMessage(String str1, String str2) {
		return "Old Value:" + str1 + " " + " New value updated:" + str2;

	}

	// Method to add Course registered (Student)
	public String editRecord(String recordId, String fieldName, ArrayList<String> newValue, String managerId)
			throws RemoteException {
		mtlLogger.mLogger.info(
				managerId + " sent request to edit Record with ID: " + recordId + " new value is: " + newValue + '\n');
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
				mtlLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
			} else {
				return "The given field name is invalid for student record";
			}
		}

		return output.toString();
	}

	public void printHashMap() throws RemoteException {

		for (Map.Entry<String, ArrayList<Record>> map : mtlDB.entrySet()) {

			System.out.println("Key: " + map.getKey());
			for (Record r : map.getValue()) {
				System.out.println();
				if (r.getRecordId().startsWith("ST"))
					System.out.println(String.format("LN: %s\nFN: %s\nID: %s\nStatus: %s\nStatus Date: %s\n",
							r.getLastName(), r.getFirstName(), r.getRecordId(), ((Student) r).getStatus(),
							((Student) r).getStatusDate()));
				else if (r.getRecordId().startsWith("TR"))
					System.out.println(String.format("LN: %s\nFN: %s\nID: %s\naddress: %s\nphone: %s\n",
							r.getLastName(), r.getFirstName(), r.getRecordId(), ((Teacher) r).getAddress(),
							((Teacher) r).getPhone()));
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
			ArrayList<Record> alRecordInitial = new ArrayList<Record>();
			alRecordInitial.add(new Student("fi", "lastName", new ArrayList<String>(), "active", "11/11/2015"));
			mtlDB.put("S", alRecordInitial);

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