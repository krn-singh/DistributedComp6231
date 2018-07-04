package server;

import logger.LogManager;
import utility.Record;
import utility.Student;
import utility.Teacher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import server.CenterServer;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import client.ManagerClient;

@WebService(endpointInterface = "server.CenterServer")

public class MTLServer extends Thread implements CenterServer {
	public static HashMap<String, ArrayList<Record>> mtlDB = new HashMap<String, ArrayList<Record>>();
	private static HashMap<String, String> idToLastName = new HashMap<String, String>();
	private static int count = 0;
	private LogManager mtlLogger;
	static String location = "mtl";
	public static int MTLport = 1234;
	public static boolean MTLFlag;

	public MTLServer() throws Exception {
		super();
		mtlLogger = new LogManager("mtl");
		mtlLogger.mLogger.setUseParentHandlers(true);

	}

	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization,
			String location, String managerId) {
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

	public boolean createSRecord(String firstName, String lastName, String courseRegistered, String status,
			String statusDate, String managerId) {

		Record objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

		// checking if the key already exists in hash map
		synchronized (mtlDB) {

			if (mtlDB.containsKey(lastName.substring(0, 1))) {
				// Synchronizing array list for particular key of hashmap
				// synchronized (mtlDB.get(lastName).subList(0, 1)) {
				mtlDB.get(lastName.substring(0, 1)).add(objRecord);

			} else {
				ArrayList<Record> alRecord = new ArrayList<Record>();
				alRecord.add(objRecord);
				mtlDB.put(lastName.substring(0, 1), alRecord);
			}

			idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

			count++;
		}
		// adding the operation to the log file
		mtlLogger.mLogger.info(managerId + " created Student record with values: " + objRecord + '\n');

		return true;
	}

	public String getRecordCounts(String managerId) {

		String str = location + " " + count + "\n";

		DatagramSocket socket1 = null;
		DatagramSocket socket2 = null;
		byte[] message1 = location.getBytes();
		byte[] message2 = location.getBytes();

		try {
			synchronized (mtlDB) {
				mtlLogger.mLogger.info(managerId + " sent request for total record count" + '\n');

				socket1 = new DatagramSocket();
				socket2 = new DatagramSocket();
				InetAddress address = InetAddress.getByName("localhost");

				DatagramPacket request1 = new DatagramPacket(message1, message1.length, address, LVLServer.LVLport);
				socket1.send(request1);
				mtlLogger.mLogger
						.info(location + " sever sending request to laval sever for total record count" + '\n');

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
				mtlLogger.mLogger
						.info("ddo server sent response to " + location + " sever for total record count " + '\n');

				str = str.concat(new String(reply2.getData()));
				str = str.trim();
				str = str.concat("\n");
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			socket1.close();
			socket2.close();
		}

		mtlLogger.mLogger.info("Total record count is : \n" + str + '\n');
		return str;
	}

	public String editRecord(String recordId, String fieldName, String newValue, String managerId) {

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

	public String editListRecord(String recordId, String fieldName, String newValue, String managerId) {

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

	public String printMessage(String str1, String str2) {
		return "Old Value:" + str1 + " " + " New value updated:" + str2;

	}

	public void printHashMap() {
	}

	public boolean transferRecord(String managerId, String recordId, String targetCenterName) {

		mtlLogger.mLogger.info(managerId + " sent request to transfer Record with ID: " + recordId + " to center: "
				+ targetCenterName + '\n');
		String key;
		if (idToLastName.containsKey(recordId))
			key = idToLastName.get(recordId);
		else {
			mtlLogger.mLogger.info("Record cannot be transfered as record value: " + recordId + " doesnt exist" + "\n");
			return false;
		}

		synchronized (mtlDB.get(key)) {

			for (int i = 0; i < mtlDB.get(key).size(); i++) {

				Record temp = mtlDB.get(key).get(i);
				String id = temp.getRecordId();

				if (id.equalsIgnoreCase(recordId)) {

					DatagramSocket socket = null;
					String transferContent = "";

					if (recordId.startsWith("ST")) {
						String courses = ((Student) temp).getCourseRegistered();
						transferContent = id + "|" + ((Student) temp).getFirstName() + "|"
								+ ((Student) temp).getLastName() + "|" + courses + "|" + ((Student) temp).getStatus()
								+ "|" + ((Student) temp).getStatusDate();
					} else if (recordId.startsWith("TR")) {
						transferContent = id + "|" + ((Teacher) temp).getFirstName() + "|"
								+ ((Teacher) temp).getLastName() + "|" + ((Teacher) temp).getAddress() + "|"
								+ ((Teacher) temp).getPhone() + "|" + ((Teacher) temp).getSpecialization() + "|"
								+ ((Teacher) temp).getLocation();
					}

					byte[] message = transferContent.getBytes();

					try {
						mtlLogger.mLogger.info(managerId + " sent request for record transfer" + '\n');
						socket = new DatagramSocket();
						InetAddress address = InetAddress.getByName("localhost");

						DatagramPacket request = new DatagramPacket(message, message.length, address,
								targetCenterName.equalsIgnoreCase("lvl") ? LVLServer.LVLport : DDOServer.DDOport);
						socket.send(request);
						mtlLogger.mLogger.info(location + " sever sending request to " + targetCenterName
								+ " sever for record transfer" + '\n');

						byte[] receive = new byte[1000];
						DatagramPacket reply = new DatagramPacket(receive, receive.length);
						socket.receive(reply);
						mtlLogger.mLogger.info(targetCenterName + " server sent response to " + location
								+ " sever regarding the record transfer " + '\n');

						String replyStr = new String(reply.getData()).trim();

						if (replyStr.equals("success")) {
							mtlDB.get(key).remove(i);
							count--;
							idToLastName.remove(recordId);
							if (mtlDB.get(key).isEmpty()) {
								mtlDB.remove(key);
								break;
							}

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

	public static void main(String[] args) throws Exception {

		MTLFlag = true;
		ArrayList<Record> alRecordInitialMTL = new ArrayList<Record>();
		alRecordInitialMTL.add(new Student("mtlFirstName", "mtlLastName", "maths", "active", "11/11/2015"));
		mtlDB.put("m", alRecordInitialMTL);
		idToLastName.put("ST100", "m");
		count++;
		MTLFlag = false;
		System.out.println("Montreal Server is started");
		System.out.println("Dummy Student record with id - ST100 is created for testing");
		Thread threadSocket = new Thread(new MTLServer());
		threadSocket.start();

		try {
			Endpoint.publish("http://localhost:1234/ws/" + ManagerClient.MTL_SERVER_NAME, new MTLServer());
			System.out.println("Montreal Server ready and waiting ...");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		DatagramSocket socket = null;
		try {
			MTLServer montreal = new MTLServer();
			socket = new DatagramSocket(MTLport);
			byte[] get = new byte[1000];
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
						String courseRegistered = requestChunks[3];
						// ArrayList<String> courseRegistered = new ArrayList<String>();
						// for (int i = 0; i < requestChunks[3].split(",").length; i++) {
						// courseRegistered.add(requestChunks[3].split(",")[i]);
						// }
						String status = requestChunks[4];
						String statusDate = requestChunks[5];
						objRecord = new Student(firstName, lastName, courseRegistered, status, statusDate);

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

						// use the old recordID
						objRecord.setRecordId(recordId);
						idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

						count++;
						// adding the operation to the log file
						montreal.mtlLogger.mLogger
								.info(" New Student record transfered with values:" + objRecord + '\n');

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

						// use the old recordID
						objRecord.setRecordId(recordId);
						idToLastName.put(objRecord.getRecordId(), lastName.substring(0, 1));

						count++;
						// adding the operation to the log file
						montreal.mtlLogger.mLogger
								.info(" New Teacher record transfered with values: " + objRecord + '\n');
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

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		} finally {
			socket.close();
		}
	}

}
