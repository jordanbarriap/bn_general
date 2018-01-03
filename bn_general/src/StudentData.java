import java.util.HashMap;
import java.util.Map;

public class StudentData {

	private static StudentData instance = null;
	private Map<String, String> stdModelMap = null; // key is usrgrp
															// (concatenated),
															// value is string
															// representation of
															// the JSON

	private StudentData() {
		stdModelMap = new HashMap<String, String>();
	}

	public static StudentData getInstance() {
		if (instance == null) {
			instance = new StudentData();
			
		}
		return instance;
	}
	public Map<String, String> getStdModelMap() {
		return stdModelMap;
	}
	
	public void setStdModel(String usr, String grp, String jsonStdModel) {
		stdModelMap.put(usr + grp, jsonStdModel);
	}

	public String getLastStudentModel(String usr, String grp) {
		return stdModelMap.get(usr + grp);
	}
	
}
