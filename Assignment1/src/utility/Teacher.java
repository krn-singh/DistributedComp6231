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
	private String firstName;
	private String lastName;
	private String address;
	private String phone;
	private String specialization;
	private String location;
	static int id = 100;

	public Teacher(String firstName, String lastName, String address, String phone, String specialization,
			String location) {

		super();
		this.recordId = "TR" + Teacher.id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
		this.specialization = specialization;
		this.location = location;
		Teacher.id++;
		System.out.println("Visited Teacher Constructor");
	}

	public String getRecordId() {
		return recordId;
	}

	// public void setRecordId(String recordId) {
	// this.recordId =
	// "TR"+"1"+Integer.toString((int)(Math.random()*10+30))+Integer.toString((int)(Math.random()*10+50));
	// }

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
