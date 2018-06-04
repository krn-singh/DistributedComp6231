package test;

import java.util.ArrayList;
import java.util.Random;

import client.ManagerClient;

public class Test {

	public static void main(String[] args) {

		for (int i = 1; i < 3; ++i) {
			Random r = new Random();

			ArrayList<String> locationList = new ArrayList<String>();
			locationList.add("LVL");
			locationList.add("MTL");
			locationList.add("DDO");

			String mId = locationList.get(r.nextInt(locationList.size())) + String.format("%04d", r.nextInt(10000));

			ManagerClient objClient1 = new ManagerClient(mId);
			ArrayList<String> courseList = new ArrayList<String>();
			courseList.add("maths");
			courseList.add("french");
			courseList.add("science");

			ArrayList<String> statusList = new ArrayList<String>();
			statusList.add("active");
			statusList.add("inactive");

			objClient1.setFirstName("firstName");
			objClient1.setLastName("lName");
			objClient1.setAddress("address");
			objClient1.setPhone("1231231231");
			objClient1.setSpecialization(courseList.get(r.nextInt(courseList.size())));
			objClient1.setLocation(locationList.get(r.nextInt(locationList.size())));
			objClient1.setStatus(statusList.get(r.nextInt(statusList.size())));
			objClient1.setStatusDate("12/12/12");

			objClient1.start();
		}

		// ManagerClient objClient2 = new ManagerClient("MTL2222");
		// ManagerClient objClient3 = new ManagerClient("DDO1212");
		// objClient1.start();
		// objClient2.start();
		// objClient3.start();
	}
}
