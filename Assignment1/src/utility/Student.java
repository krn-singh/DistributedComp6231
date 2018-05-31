/**
 * The package contains the utilities such as Teacher and Student attributes along with classes that
 * performs I/O operations.
 */
package utility;

import java.util.ArrayList;

/**
 * The class provides attributes for the student.
 * 
 * @author karan
 */
public class Student extends Record {

	private String recordId;
	private String firstName;
	private String lastName;
	private ArrayList<String> courseRegistered;
	private String status;
	private String statusDate;
	static int id = 100;

	public Student(String firstName, String lastName, ArrayList<String> courseRegistered, String status, String statusDate) {

		super();
		this.recordId = "ST" + Student.id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.courseRegistered = courseRegistered;
		this.status = status;
		this.statusDate = statusDate;
		Student.id++;
	}
	
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = "SR"+"1"+Integer.toString((int)(Math.random()*10+30))+Integer.toString((int)(Math.random()*10+50));
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public ArrayList<String> getCourseRegistered() {
		return courseRegistered;
	}
	public void setCourseRegistered(ArrayList<String> courseRegistered) {
		this.courseRegistered = courseRegistered;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusDate() {
		return statusDate;
	}
	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}
}
