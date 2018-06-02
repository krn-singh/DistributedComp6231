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

	private ArrayList<String> courseRegistered;
	private String status;
	private String statusDate;
	private String recordId;
	static int Sid = 100;

	public Student(String firstName, String lastName, ArrayList<String> courseRegistered, String status, String statusDate) {

		super(firstName, lastName);
		this.recordId = "ST" + Student.Sid;
		this.courseRegistered = courseRegistered;
		this.status = status;
		this.statusDate = statusDate;
		Student.Sid++;
	}
	
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = "SR"+"1"+Integer.toString((int)(Math.random()*10+30))+Integer.toString((int)(Math.random()*10+50));
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
