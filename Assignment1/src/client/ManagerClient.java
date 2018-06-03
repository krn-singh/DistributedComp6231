/**
 * The package contains the all the clients which have an access to the server
 */
package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import logger.LogManager;

import server.CenterServer;

/**
 * The class represents the manager client. Depending upon the region to which
 * manager belongs, the data access is provided to the manager to perform the
 * required operations after successful validation.
 * 
 * @author KVM2
 */
public class ManagerClient extends Thread {

	private static int MTL_SERVER_ID = 1234;
	private static int LVL_SERVER_ID = 2345;
	private static int DDO_SERVER_ID = 3456;
	public int serverId;
	private String firstName;
	private String lastName;
	private String address;
	private String phone;
	private String specialization;
	private String location;
	private String status;
	private String statusDate;
	private ArrayList<String> courseRegistered;
	private String recordId;
	private String fieldName;
	private String newValue;
	private LogManager clientLogger = null;
	private CenterServer serverObj = null;

	public ManagerClient(String managerId) {
		this.recordId = managerId;
		this.clientLogger = new LogManager(managerId);
		String serverName = managerId.substring(0, 3);
		fetchServer(serverName);

	}

	public void fetchServer(String serverName) {

		serverName = serverName.toLowerCase();
		if (serverName.equals("mtl")) {
			serverId = MTL_SERVER_ID;
		} else if (serverName.equals("lvl")) {
			serverId = LVL_SERVER_ID;
		} else if (serverName.equals("ddo")) {
			serverId = DDO_SERVER_ID;
		} else {
			System.out.println("Incorrect server name");
			clientLogger.mLogger.info("Incorrect server name");
			System.exit(0);
			return;
		}

		try {
			Registry registry = LocateRegistry.getRegistry(serverId);
			this.serverObj = (CenterServer) registry.lookup(serverName);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Validates the manager Id
	 * 
	 * @param managerId
	 *            Manager Login ID
	 * @return True/False whether the validation was successfully or not
	 */
	public static boolean validateManager(String managerId) {

		try {
			if (managerId.length() != 7) {
				return false;
			}

			for (int i = 3; i < managerId.length(); i++) {
				if (!Character.isDigit(managerId.charAt(i))) {
					return false;
				}
			}

			String center = managerId.substring(0, 3);

			if (center.equalsIgnoreCase("MTL") || center.equalsIgnoreCase("LVL") || center.equalsIgnoreCase("DDO")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean validatePhoneNumber(String phoneNumber) {
		try {
			if (phoneNumber.length() != 10) {
				System.out.println("Phone number can't be less than 10 digits");
				return false;
			}
			if (!phoneNumber.matches("^[0-9]*$")) {
				System.out.println("Phone number can't have any characters");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static boolean validateSpecialization(String specializationType) {
		try {
			specializationType = specializationType.toLowerCase();
			if (specializationType.equals("french") || specializationType.equals("maths")
					|| specializationType.equals("science")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Specialization should be from maths, french or science only");
		return false;
	}

	/**
	 * Prompts the user for Teacher attributes.
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 */
	public void createTRecord(Scanner scan) {

		try {
			System.out.println("Enter First Name:");
			firstName = scan.nextLine();
			System.out.println("Enter Last name");
			lastName = scan.nextLine();
			System.out.println("Enter address");
			address = scan.nextLine();
			System.out.println("Enter Phone Number");
			phone = scan.nextLine();
			while (!validatePhoneNumber(phone)) {
				System.out.println("Enter Phone Number");
				phone = scan.nextLine();
			}
			System.out.println("Enter Specialization");
			specialization = scan.nextLine();
			while (!validateSpecialization(specialization)) {
				System.out.println("Enter Specialization");
				specialization = scan.nextLine();
			}
			location = locationMenu(scan);
			clientLogger.mLogger.info(
					"Creating Teacher Record with First Name: " + firstName + " Last name: " + lastName + " Address: "
							+ address + " Phone number: " + phone + " Specialization: " + specialization + '\n');
			// location = location.toLowerCase();
			// if(location.equals(recordId.substring(0,3))){
			serverObj.createTRecord(firstName, lastName, address, phone, specialization, location);
			// }
			// else {
			// CenterServer tempServer = null;
			// fetchServer(location, clientLogger, tempServer);
			// tempServer.createTRecord(firstName, lastName, address, phone, specialization,
			// location);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prompts the user for Student attributes.
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 */
	public void createSRecord(Scanner scan) {

		try {
			System.out.println("Enter First Name:");
			firstName = scan.nextLine();
			System.out.println("Enter Last name");
			lastName = scan.nextLine();
			courseRegistered = courseMenu(scan, new ArrayList<String>());
			status = statusMenu(scan);
			System.out.println("Enter Status Date (dd/mm/yyyy)");
			statusDate = scan.nextLine();
			clientLogger.mLogger.info(
					"Creating Student Record with First Name: " + firstName + " Last name: " + lastName + " Course: "
							+ courseRegistered + " Status: " + status + " Status Date: " + statusDate + '\n');
			serverObj.createSRecord(firstName, lastName, courseRegistered, status, statusDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prompts the user for Teacher location.
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 * @return Location of the teacher
	 */
	public String locationMenu(Scanner scan) {

		String location = "";
		try {
			System.out.println("Enter Location\n" + "1. Laval(lvl)\n" + "2. Dollard(ddo)\n" + "3. Montreal(mtl)\n");

			String option = scan.nextLine();

			switch (option) {
			case "1":
				location = "lvl";
				break;

			case "2":
				location = "ddo";
				break;

			case "3":
				location = "mtl";
				break;

			default:
				System.out.println("Invalid option. Try again");
				locationMenu(scan);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Prompts the user for Student status.
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 * @return Status of the student
	 */
	public String statusMenu(Scanner scan) {

		String status = "";
		try {
			System.out.println("Enter Status\n" + "1. Active\n" + "2. Inactive\n");

			String option = scan.nextLine();

			switch (option) {
			case "1":
				status = "active";
				break;

			case "2":
				status = "inactive";
				break;

			default:
				System.out.println("Invalid option. Try again");
				statusMenu(scan);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return status;
	}

	/**
	 * Prompts the user for courses.
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 * @return Registered Courses
	 */
	public ArrayList<String> courseMenu(Scanner scan, ArrayList<String> courseList) {

		try {
			System.out.println("Select Course\n" + "1. Maths\n" + "2. French\n" + "3. Science\n" + "4. Exit");

			String option = scan.nextLine();
			do {
				switch (option) {
				case "1":
					if (!courseList.contains("Maths")) {
						courseList.add("Maths");
					}
					break;

				case "2":
					if (!courseList.contains("French")) {
						courseList.add("French");
					}
					break;

				case "3":
					if (!courseList.contains("Science")) {
						courseList.add("Science");
					}
					break;

				case "4":
					if (courseList.isEmpty()) {
						System.out.println("Select atleast one course");
					} else {
						return courseList;
					}
					break;

				default:
					System.out.println("Invalid option. Try again");
					break;
				}
				System.out
						.println("Select another Course\n" + "1. Maths\n" + "2. French\n" + "3. Science\n" + "4. Exit");
				option = scan.nextLine();
			} while ((Integer.parseInt(option) < 4 && Integer.parseInt(option) > 0) || courseList.isEmpty());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return courseList;
	}

	public String editRecord(Scanner scan) {

		try {
			System.out.println("Enter the Record Id");
			recordId = scan.nextLine();
			System.out.println("Enter the field name which is ('address', 'phone' and 'location' for TeacherRecord)\n"
					+ "and ('course registered', 'status' and 'status date' for StudentRecord)");
			fieldName = scan.nextLine();
			System.out.println("Enter the new value");
			newValue = scan.nextLine();
			clientLogger.mLogger.info("Editing Record with ID:" + recordId + '\n');
			return serverObj.editRecord(recordId, fieldName, newValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "return";
	}

	/**
	 * System Main menu Select the following options: 1>Create Teacher Record
	 * 2>Create Student Record 3>Edit Record 4>Get record count 5>Exit
	 * 
	 * @param scan
	 *            Simple text scanner for user input
	 * @param menu
	 */
	public void mainMenu(Scanner scan, StringBuffer menu) {

		try {
			System.out.println(menu);

			String option = scan.nextLine();

			do {
				switch (option) {
				case "1":
					createTRecord(scan);
					break;

				case "2":
					createSRecord(scan);
					break;

				case "3":
					System.out.println(editRecord(scan));
					break;

				case "4":
					System.out.println(serverObj.getRecordCounts());
					clientLogger.mLogger
							.info("Client requested to get the number of records in each server i.e. option " + option
									+ '\n');
					break;

				case "5":
					clientLogger.mLogger.info("Logged Out" + '\n');
					System.out.println("Good Bye");
					System.exit(0);
					break;

				default:
					clientLogger.mLogger.info("Client entered Invalid Option for main menu: " + option + '\n');
					System.out.println("Invalid option. Try again");
					break;
				}
				mainMenu(scan, menu);
			} while (option != "5");
		} catch (Exception e) {
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
		Scanner scan = new Scanner(System.in);

		ManagerClient objClient1 = new ManagerClient("MTL1430");
		ManagerClient objClient2 = new ManagerClient("MTL2222");
		ManagerClient objClient3 = new ManagerClient("DDO1212");

		objClient1.start();
		objClient2.start();
		objClient3.start();

		// StringBuffer menu = new StringBuffer("Select the following options:\n" + "1>
		// Create Teacher Record\n"
		// + "2> Create Student Record\n" + "3> Edit Record\n" + "4> Get record count\n"
		// + "5> Exit\n");
		//
		// System.out.println("Enter the Manager Id");
		// String managerId = scan.nextLine();
		// if (validateManager(managerId)) {
		// ManagerClient client = new ManagerClient(managerId);
		// client.clientLogger.mLogger.info("Manager: " + managerId + " logged in." +
		// '\n');
		// client.mainMenu(scan, menu);
		// } else {
		// System.out.println("Invalid Login Id..... Terminating the system");
	}

	public void run() {

		
		if (recordId.equals("MTL1430")) {
			
			try {
				serverObj.createSRecord("firstName", "lastName", new ArrayList<String>(), "status", "statusDate");
				System.out.println("fsrsrt");
				serverObj.editRecord("SR100", "statusDate", "active1213");
				
//				serverObj.createSRecord("firstName", "lastName", new ArrayList<String>(), "status", "statusDate");
				System.out.println("1 created");
//				System.out.println(serverObj.getRecordCounts());
//				serverObj.createSRecord("fir", "lastName", new ArrayList<String>(), "status", "statusDate");
//				System.out.println("2 created");
//				System.out.println(serverObj.getRecordCounts());
//				serverObj.createSRecord("firstName5ygy", "lastName", new ArrayList<String>(), "status", "statusDate");
//				System.out.println("3 created");
//				System.out.println(serverObj.getRecordCounts());
//				
				serverObj.printHashMap();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else if(recordId.equals("MTL2222")){
			
			try {
				
				serverObj.editRecord("SR100", "statusDate", "inactive323");
//				serverObj.createSRecord("firstName5ygy", "Name", new ArrayList<String>(), "status", "statusDate");
				System.out.println("4 created");
//				
//				System.out.println(serverObj.getRecordCounts());
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
//		else if(serverId == 2345)
//		{
//			try {
//				serverObj.createSRecord("firstNam", "lastNam", new ArrayList<String>(), "status", "statusDate");
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else if(serverId == 3456)
//		{
//			try {
//				serverObj.createSRecord("first", "last", new ArrayList<String>(), "status", "statusDate");
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}
}