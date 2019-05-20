import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

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
	
	private StudentData(HttpServlet servlet) {
		stdModelMap = new HashMap<String, String>();
		BNGeneralConfigManager bn_cm = new BNGeneralConfigManager(servlet);
		BNGeneralDB bnDB = new BNGeneralDB(bn_cm.bn_dbstring, bn_cm.bn_dbuser, bn_cm.bn_dbpass);
		bnDB.openConnection();		
		stdModelMap = bnDB.getLatestStudentModels();
	}

	public static StudentData getInstance() {
		if (instance == null) {
			instance = new StudentData();	
		}
		return instance;
	}
	
	public static StudentData getInstance(HttpServlet servlet) {
		if (instance == null) {
			instance = new StudentData(servlet);	
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
