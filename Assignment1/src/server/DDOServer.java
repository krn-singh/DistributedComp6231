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
 * The class performs the operations regarding the Dollard(MTL) center
 * 
 * @author karan
 */
@SuppressWarnings("serial")
public class DDOServer extends UnicastRemoteObject implements CenterServer {

	public static HashMap<String, ArrayList<Record>> ddoDB = new HashMap<String, ArrayList<Record>>();
	private static HashMap<String, String> idToLastName = new HashMap<String, String>();
	private static int count;
	private LogManager ddoLogger;
	static String location = "ddo";
	public static int DDOport = 3456;

	public DDOServer() throws Exception {
		super();
		ddoLogger = new LogManager("ddo");
		ddoLogger.mLogger.setUseParentHandlers(true);
	}

	@Override
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) throws RemoteException {
		Record objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);

		if (ddoDB.containsKey(lastName.substring(0, 1))) {
			// Synchronizing array list for particular key of hashmap
			synchronized (ddoDB.get(lastName).subList(0, 1)) {
				ddoDB.get(lastName.substring(0, 1)).add(objRecord);
			}
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			ddoDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;
		// adding the operation to the log file
		ddoLogger.mLogger.info(managerId + " created Teacher record with values: " + objRecord + '\n');
		return true;
	}

	@Override
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status,
			String statusDate, String managerId) throws RemoteException {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		// checking if the key already exists in hash map
		if (ddoDB.containsKey(lastName.substring(0, 1))) {
			// Synchronizing array list for particular key of hashmap
			synchronized (ddoDB.get(lastName).subList(0, 1)) {
				ddoDB.get(lastName.substring(0, 1)).add(objRecord);
			}
		} else {
			ArrayList<Record> alRecord = new ArrayList<Record>();
			alRecord.add(objRecord);
			ddoDB.put(lastName.substring(0, 1), alRecord);
		}

		idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

		count++;

		// adding the operation to the log file
		ddoLogger.mLogger.info(managerId + " created Student record with values: " + objRecord + '\n');

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
			ddoLogger.mLogger.info(managerId + " sent request for total record count" + '\n');
			socket1 = new DatagramSocket();
			socket2 = new DatagramSocket();
			InetAddress address = InetAddress.getByName("localhost");

			DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, LVLServer.LVLport);
			socket1.send(request1);
			ddoLogger.mLogger.info(location + " sever sending request to laval sever for total record count" + '\n');

			byte[] receive1 = new byte[1000];
			DatagramPacket reply1 = new DatagramPacket(receive1, receive1.length);
			socket1.receive(reply1);
			ddoLogger.mLogger
					.info("laval server sent response to " + location + " sever for total record count " + '\n');

			str = str.concat(new String(reply1.getData()));
			str = str.trim();
			str = str.concat("\n");

			DatagramPacket request2 = new DatagramPacket(message2, message2.length, address, MTLServer.MTLport);
			socket2.send(request2);
			ddoLogger.mLogger.info(location + " sever sending request to mtl sever for total record count" + '\n');

			byte[] receive2 = new byte[1000];
			DatagramPacket reply2 = new DatagramPacket(receive2, receive2.length);
			socket2.receive(reply2);
			ddoLogger.mLogger.info("mtl server sent response to " + location + " sever for total record count " + '\n');

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
		System.out.println(str);
		ddoLogger.mLogger.info("Total record count is : \n" + str + '\n');
		return str;
	}

	// Method to edit status,statusdate(Student) && Address,phone,specialization(Teacher)
	@Override
	public String editRecord(String recordId, String fieldName, String newValue, String managerId)
			throws RemoteException {
		ddoLogger.mLogger.info(
				managerId + " sent request to edit Record with ID: " + recordId + " new value is: " + newValue + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			ddoLogger.mLogger.info("Record couldn't be updated as record value: " + recordId + " doesnt exist" + "\n");
			return "The given record id doesn't exist";
		}

		StringBuilder output = new StringBuilder();

		synchronized (ddoDB.get(key)) {

			for (Record temp : ddoDB.get(key)) {
				String id = temp.getRecordId();
				if (id.equalsIgnoreCase(recordId)) {
					if (recordId.startsWith("ST")) {
						if (fieldName.equalsIgnoreCase("status")) {
							output.append(printMessage(((Student) temp).getStatus(), newValue));
							((Student) temp).setStatus(newValue);
							ddoLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("statusDate")) {
							output.append(printMessage(((Student) temp).getStatusDate(), newValue));
							((Student) temp).setStatusDate(newValue);
							ddoLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
						} else {
							return "The given field name is invalid for student record";
						}
					} else if (recordId.startsWith("TR")) {
						if (fieldName.equalsIgnoreCase("address")) {
							output.append(printMessage(((Teacher) temp).getAddress(), newValue));
							((Teacher) temp).setAddress(newValue);
							ddoLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("phone")) {
							output.append(printMessage(((Teacher) temp).getPhone(), newValue));
							((Teacher) temp).setPhone(newValue);
							ddoLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
						} else if (fieldName.equalsIgnoreCase("specialization")) {
							output.append(printMessage(((Teacher) temp).getSpecialization(), newValue));
							((Teacher) temp).setSpecialization(newValue);
							ddoLogger.mLogger.info("Record Updated, new value: " + ((Teacher) temp) + '\n');
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
		ddoLogger.mLogger.info(
				managerId + " sent request to edit Record with ID: " + recordId + " new value is: " + newValue + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else
			return "The given record id doesn't exist";
		StringBuilder output = new StringBuilder();
		for (Record temp : ddoDB.get(key)) {
			if (temp.getRecordId() == recordId && recordId.startsWith("ST")
					&& fieldName.equalsIgnoreCase("courseRegistered")) {
				output.append(printMessage(((Student) temp).getCourseRegistered().toString(), newValue.toString()));
				((Student) temp).setCourseRegistered(newValue);
				ddoLogger.mLogger.info("Record Updated, new value: " + ((Student) temp) + '\n');
			} else {
				return "The given field name is invalid for student record";
			}
		}

		return output.toString();
	}
	
	public void printHashMap() throws RemoteException {

		for (Map.Entry<String, ArrayList<Record>> map : ddoDB.entrySet()) {

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
	 * @param args
	 *            (No arguments are needed to launch)
	 */
	public static void main(String[] args) {

		try {
			Registry registry = LocateRegistry.createRegistry(DDOport);
			DDOServer dollard = new DDOServer();
			registry.bind(location, dollard);
			System.out.println("Dollard Server is started");

			DatagramSocket socket = null;
			try {

				socket = new DatagramSocket(DDOport);
				byte[] get = new byte[256];
				byte[] send = new byte[1000];

				while (true) {
					DatagramPacket request = new DatagramPacket(get, get.length);
					socket.receive(request);

					String requestContent = new String(request.getData()).trim();
					String[] requestChunks = requestContent.split("\\|");
					
					if (requestContent.startsWith("ST") || requestContent.startsWith("TR")) {
						
						String recordId;
						String firstName;
						String lastName;
						Record objRecord;
						switch (requestContent.substring(0, 2)) {
						case "ST":
							recordId = requestChunks[0];
							firstName = requestChunks[1];
							lastName = requestChunks[2];
							ArrayList<String> courseRegistered = new ArrayList<String>();
							for (int i = 0; i < requestChunks[3].split(",").length; i++) {
								courseRegistered.add(requestChunks[3].split(",")[i]);
							}
							String status = requestChunks[4];
							String statusDate = requestChunks[5];
							objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);
							
							if (ddoDB.containsKey(lastName.substring(0, 1))) {
								// Synchronizing array list for particular key of hashmap
								synchronized (ddoDB.get(lastName).subList(0, 1)) {
									ddoDB.get(lastName.substring(0, 1)).add(objRecord);
								}
							} else {
								ArrayList<Record> alRecord = new ArrayList<Record>();
								alRecord.add(objRecord);
								ddoDB.put(lastName.substring(0, 1), alRecord);
							}

							// use the old recordID
							objRecord.setRecordId(recordId);
							idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

							count++;
							// adding the operation to the log file
							dollard.ddoLogger.mLogger.info(" New Student record transfered with values: " + objRecord + '\n');
							
							break;

						case "TR":
							recordId = requestChunks[0];
							firstName = requestChunks[1];
							lastName = requestChunks[2];
							String address = requestChunks[3];
							String phone = requestChunks[4];
							String specialization = requestChunks[5];
							String location = requestChunks[6];
							objRecord = new Teacher(firstName, lastName, address, phone, specialization, location);
							
							if (ddoDB.containsKey(lastName.substring(0, 1))) {
								// Synchronizing array list for particular key of hashmap
								synchronized (ddoDB.get(lastName).subList(0, 1)) {
									ddoDB.get(lastName.substring(0, 1)).add(objRecord);
								}
							} else {
								ArrayList<Record> alRecord = new ArrayList<Record>();
								alRecord.add(objRecord);
								ddoDB.put(lastName.substring(0, 1), alRecord);
							}

							// use the old recordID
							objRecord.setRecordId(recordId);
							idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

							count++;
							// adding the operation to the log file
							dollard.ddoLogger.mLogger.info(" New Teacher record transfered with values: " + objRecord + '\n');
							break;
						}
						
						send = ("success").getBytes();
						DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(),
								request.getPort());
						socket.send(reply);
					} else {
						send = (location + " " + count).getBytes();
						DatagramPacket reply = new DatagramPacket(send, send.length, request.getAddress(),
								request.getPort());
						socket.send(reply);
					}
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

	@Override
	public boolean transferRecord(String managerId, String recordId, String targetCenterName) throws RemoteException {
		
		ddoLogger.mLogger.info(managerId + " sent request to transfer Record with ID: " + recordId + " to center: " + targetCenterName + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			ddoLogger.mLogger.info("Record couldn't be updated as record value: " + recordId + " doesnt exist" + "\n");
			return false;
		}

		synchronized (ddoDB.get(key)) {

			for (int i = 0; i < ddoDB.get(key).size(); i++) {
				
				Record temp = ddoDB.get(key).get(i);
				String id = temp.getRecordId();
				
				if (id.equalsIgnoreCase(recordId)) {

					DatagramSocket socket = null;
					String transferContent = "";
			
					if (recordId.startsWith("ST")) {
						String courses = "";
						for (String course : ((Student)temp).getCourseRegistered()) {
							courses += course + ",";
						}					
						courses = courses.substring(0, courses.length() - 1);
						transferContent = id +"|"+ ((Student)temp).getFirstName() +"|"+ ((Student)temp).getLastName() +"|"+ courses
								+"|"+ ((Student)temp).getStatus() +"|"+ ((Student)temp).getStatusDate();
					} else if (recordId.startsWith("TR")) {
						transferContent = id +"|"+ ((Teacher)temp).getFirstName() +"|"+ ((Teacher)temp).getLastName() +"|"+ ((Teacher)temp).getAddress()
								 +"|"+ ((Teacher)temp).getPhone() +"|"+ ((Teacher)temp).getSpecialization() +"|"+ ((Teacher)temp).getLocation();
					}

					byte[] message = transferContent.getBytes();
					
					try {
						ddoLogger.mLogger.info(managerId + " sent request for record transfer" + '\n');
						socket = new DatagramSocket();
						InetAddress address = InetAddress.getByName("localhost");

						DatagramPacket request = new DatagramPacket(message, message.length, address, targetCenterName.equalsIgnoreCase("lvl") ? LVLServer.LVLport : MTLServer.MTLport);
						socket.send(request);
						ddoLogger.mLogger.info(location + " sever sending request to "+targetCenterName+" sever for record transfer" + '\n');

						byte[] receive = new byte[1000];
						DatagramPacket reply = new DatagramPacket(receive, receive.length);
						socket.receive(reply);
						ddoLogger.mLogger
								.info(targetCenterName+" server sent response to " + location + " sever regarding the record transfer " + '\n');

						String replyStr = new String(reply.getData()).trim();
						
						if (replyStr.equals("success")) {
							ddoDB.get(key).remove(i);
							count--;
							idToLastName.remove(recordId);
						} else {
							return false;
						}

					} catch (SocketException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						socket.close();
					}					
				}
			}
		}
		
		return true;
	}

}
