/**
 * The package contains the utilities such as Teacher and Student attributes along with classes that
 * performs I/O operations.
 */
package utility;

/**
 * The class provides attributes for the teacher.
 * 
 * @author karan
 */
public class Teacher extends Record {

	private String recordId;
	private String address;
	private String phone;
	private String specialization;
	private String location;
	static int Tid = 100;

	public Teacher(String firstName, String lastName, String address, String phone, String specialization,
			String location) {

		super(firstName, lastName);
		this.recordId = "TR" + Teacher.Tid;
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
		Teacher.Tid++;
		System.out.println("Visited Teacher Constructor");
	}

	public String getRecordId() {
		return recordId;
	}

	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
