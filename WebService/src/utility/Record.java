package utility;

import server.DDOServer;
import server.LVLServer;
import server.MTLServer;

public class Record {
	
	protected String recordId;
	protected String firstName;
	protected String lastName;
	static int id = 103;
	
	public Record(String firstName, String lastName, String prefix)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		if(MTLServer.MTLFlag == true) setRecordId(prefix + 100);
		else if(LVLServer.LVLFlag == true) setRecordId(prefix + 101);
		else if(DDOServer.DDOFlag == true) setRecordId(prefix + 102);
		else 
			{setRecordId(prefix + id);
		id++;}
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