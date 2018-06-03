package utility;

public class Record {
	
	private String recordId;
	private String firstName;
	private String lastName;
	static int id = 100;
	
	public Record(String firstName, String lastName, String prefix)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		setRecordId(prefix + id);
		id++;
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


	public String getRecordId() {
		return recordId;
	}


	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}	
}
