/**
 * The package contains the server classes 
 */
package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author karan
 *
 */
public interface CenterServer extends Remote {

	/**
	 * Creates a Teacher record with the information passed and returns True/False 
	 * whether the operation was successful or not.
	 * 
	 * @param firstName First Name of the teacher
	 * @param lastName Last Name of the teacher
	 * @param address Teachers' address
	 * @param phone Contact number
	 * @param specialization Subject being taught by the teacher e.g. french, maths, etc
	 * @param location One of the three locations i.e. mtl, lvl, ddo
	 * @param managerId ID of currently logged manager
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	public boolean createTRecord(String firstName, String lastName, String address, String phone, String specialization, String location, String managerId) throws RemoteException;
	
	/**
	 * Creates a Student record with the information passed and returns True/False 
	 * whether the operation was successful or not.
	 * 
	 * @param firstName First Name of the student
	 * @param lastName Last Name of the student
	 * @param courseRegistered List of courses registered by the student i.e. maths/french/science
	 * @param status Specifies whether the student status is active/inactive
	 * @param managerId ID of currently logged manager
	 * @param statusDate Latest date when student became active/inactive
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	public boolean createSRecord(String firstName, String lastName, ArrayList<String> courseRegistered, String status, String statusDate, String managerId) throws RemoteException;
	
	/**
	 * Evaluates the total number of records(teacher and student) at all the centers
	 * 
	 * @param managerId ID of currently logged manager
	 * @return total number of records
	 * @throws RemoteException
	 */
	public String getRecordCounts(String managerId) throws RemoteException;
	
	/**
	 * Method to edit status, statusDate(Student) && Address,phone,specialization(Teacher)
	 * 
	 * @param recordId Record Id of the teacher/student
	 * @param fieldName older field value
	 * @param newValue new value
	 * @param managerId ID of currently logged manager
	 * @return Number of records
	 * @throws RemoteException
	 */
	public String editRecord(String recordId, String fieldName, String newValue, String managerId) throws RemoteException;
	
	/**
	 * Method to edit registered courses (Student)
	 * 
	 * @param recordId Record Id of the teacher/student
	 * @param fieldName older field value
	 * @param newValue set of new values
	 * @param managerId ID of currently logged manager
	 * @return response from the server regarding the record updation 
	 * @throws RemoteException
	 */
	public String editRecord(String recordId, String fieldName, ArrayList<String> newValue, String managerId) throws RemoteException;

	/**
	 * Prints the set of records.
	 * 
	 * @throws RemoteException
	 */
	public void printHashMap() throws RemoteException;
	
	/**
	 * Transfers the record to the target server.
	 * 
	 * @param managerId ID of currently logged manager
	 * @param recordId Record Id of the teacher/student
	 * @param targetCenterName Name of the Target server to which the record is to be transfered
	 * @return True/False whether the operation was successful or not
	 * @throws RemoteException
	 */
	public boolean transferRecord(String managerId, String recordId, String targetCenterName) throws RemoteException;
}
