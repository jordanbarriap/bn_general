
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import smile.Network;

public class UpdateStudentModel extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private boolean verbose = false;

	/*---------------------------------------------------------------------
	 * (STATIC DATA)
	 * -------------------------------------------------------------------- */
	private static StudentData stdData; // singleton instance of class with students data
	private static HashSet<String> kcList = null;
	private static HashSet<String> itemList = null;
	private static Map<String, Network> stdNetworkMap = null; // key is usrgrp, value is the latest network of student

	private static String bnStr = null; //string representing the network in the BN File
	private static Network mainNet = null; //a network object representing the network in the BN file

	/* -------------------------------------------------------------------- */
	
	// private String inputDir = "C:\\java\\Tomcat\\webapps\\bn\\WEB-INF\\input\\";
    private String inputDir = "/var/lib/tomcat/webapps/bn_general/WEB-INF/input/";
//	private String inputDir = "/Users/roya/Documents/eclipseWS/bn_general/WebContent/WEB-INF/input/";

	private String newlineSymbol = "\r\n";
	private double MIN_BN_KNOWLEDGE = 0.0001; // doesn't allow knowledge to be 0
												// or 1, because once the level
												// reaches 0/1, it could never
												// be changed again.

	private DecimalFormat df4 = new DecimalFormat("#.####"); 
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // for logging
    

	static {
		// License issued by BayesFusion Licensing Server
		// This code must be executed before any other jSMILE object is created
		new smile.License("SMILE LICENSE bc5521ad e4a84d0a c1673b41 " + "THIS IS AN ACADEMIC LICENSE AND CAN BE USED  "
				+ "SOLELY FOR ACADEMIC RESEARCH AND TEACHING, " + "AS DEFINED IN THE BAYESFUSION ACADEMIC  "
				+ "SOFTWARE LICENSING AGREEMENT. " + "Serial #: az1cg5xxh8hcfmlvrg6n3ks9v "
				+ "Issued for: Roya Hosseini (roh38@pitt.edu) " + "Academic institution: University of Pittsburgh "
				+ "Valid until: 2018-12-11 " + "License issued for Ph.D. thesis in Intelligent Systems Program",
				new byte[] { 10, 97, 25, 44, 1, 58, -122, -49, -56, -32, 68, 8, 17, 72, 120, 96, -68, -19, 121, 18, 84,
						-120, -23, 101, 45, -89, -81, -19, 15, -60, -12, 77, -61, 102, 23, 41, 80, 89, 32, -32, -61, 34,
						-48, -33, 119, 32, -63, -124, -22, 22, -87, -50, 41, -81, -128, -46, -9, 53, 80, 102, 47, -46,
						89, -25 });
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Step 0: logging
		//System.out.println("-------------------------------------------------------");
		//long startTime = System.currentTimeMillis();
		//System.out.println("start processing: " + dateFormat.format(new Date()));
	
		String usr = null;
		String grp = null;
		String lastAct = null;
		Double lastActResult = null; 
		String[] contentList = null;
		String event = null;
		try {
	
			// Step 1: parse the json in the request
			InputStreamReader is = new InputStreamReader(request.getInputStream());
			JSONParser jsonParser = new JSONParser();
			org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(is);
			usr = (String) jsonObject.get("usr");
			grp = (String)jsonObject.get("grp");
			lastAct = (String)jsonObject.get("lastContentId");
			String lastActResTxt = (String)jsonObject.get("lastContentResult");
			if (lastActResTxt != null) {
				try {
					lastActResult = Double.parseDouble((String)jsonObject.get("lastContentResult"));
				} catch (Exception e) {
					System.out.println(
							"bn_general/UpdateStudentModel: Last activity result in the parameter could not be parsed to double. ");
				}
			}

			String contentsTxt = (String)jsonObject.get("contents");
			if (contentsTxt != null) {
				//add pretest items to the contentList;
				contentsTxt += ("pretest_qj_if,pretest_qj_while,pretest_qj_for," +
					       "pretest_qj_array,pretest_qj_2darray,pretest_boolean," +
					       "pretest_nested_for,pretest_array,pretest_ifelse," +
					       "pretest_method");
				contentList = contentsTxt.split(","); // contents are separated by ,
			}

			event = (String)jsonObject.get("event");
			
			
			// Step 2: fill static data
			if (UpdateStudentModel.kcList == null) 
				kcList = this.getKCList(contentList); 
			
			if (UpdateStudentModel.itemList == null)
				itemList = this.getItemList(contentList); 
															
			// Step 3: dynamically define which bn file should be used for the grp
			//String bnFile = inputDir + grp + "_net_learned_BN.xdsl";
			String bnFile = inputDir + "java_net_learned_BN.xdsl";
			// Step 4: Create the main network from the original bn file
			if (UpdateStudentModel.bnStr == null || UpdateStudentModel.bnStr.isEmpty()) {
				BufferedReader reader = new BufferedReader(new FileReader(bnFile));
				bnStr = "";
				String line = "";
				while ((line = reader.readLine()) != null)
					bnStr += line + "\n";
				reader.close();
			}
			
			if (UpdateStudentModel.mainNet == null) {
				mainNet = new Network();	
				mainNet.readString(bnStr);
				mainNet.updateBeliefs();  //after reading bn string, we need to update the beliefs
			}
			
			// Step 5: create the student network map
			if (UpdateStudentModel.stdNetworkMap == null) {
				stdNetworkMap = new HashMap<String, Network>();
			}

			//System.out.println("finished params init:" + dateFormat.format(new Date()));

			// Step 6: get the student data instance (singleton)
			stdData = StudentData.getInstance();

			// Step 7: get the latest student model
			String lastStdModelTxt = getLastStudentModel(usr, grp);
			HashMap<String, Double> lastStdModel = getMapLastStdModel(lastStdModelTxt);
			
			//System.out.println("got last student model map:" + dateFormat.format(new Date()));

			// Step 8: if lastActivity is provided and result is valid, get the new estimates of student model
			HashMap<String, Double> newStdModel = null;
			if (lastAct != null && lastAct.isEmpty() == false && lastActResult != null) {
				ArrayList<Attempt> lastAttempts = getLastAttempts(lastAct, lastActResult);
				newStdModel = getNewStudentModel(usr, grp, lastStdModel, lastAttempts, bnFile);
			}

			//System.out.println("got new student model map:" + dateFormat.format(new Date()));

			// Step 9: store new student model in the tables
			// if no previous model, first insert the null model
			String jsonStdModel = null;
			if (lastStdModelTxt == null) {
				jsonStdModel = getJSONStdModel(lastStdModel);
				// put this model in the map
				stdData.setStdModel(usr, grp, jsonStdModel);
				storeNullStdModel(usr, grp, jsonStdModel, event);
			}

			//System.out.println("stored null models:" + dateFormat.format(new Date()));
		
			if (newStdModel != null) {
				jsonStdModel = getJSONStdModel(newStdModel);
				// put this model in the map
				stdData.setStdModel(usr, grp, jsonStdModel);
				storeUpdatedStdModel(usr, grp, jsonStdModel, event);
			} 
			
			if (jsonStdModel == null) { //this occurs when last student model is not null but new model is null
				jsonStdModel = getJSONStdModel(lastStdModel); //in this case, json should represent the last student model
			}

			//System.out.println("stored updated models:" + dateFormat.format(new Date()));

			// Step 10: return student model JSON, if new student model is null
			// (i.e., last student model is not updated), return last student model
			String output = jsonStdModel.toString();
			setResponse(response, output);

		
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
			
		//double endTime = (System.currentTimeMillis() - startTime) / 1000.0;
		//System.out.println("BNG STudent Model Response Time: " + endTime + " (sec)");
		//System.out.println("------------------------------------------------------");
	}

	private String getJSONStdModel(HashMap<String, Double> lastStdModel) {
		JSONArray ja = new JSONArray();
		JSONObject jo;
		// adding kc estimates
		for (String kc : kcList) {
			jo = new JSONObject();
			jo.put("name", kc);
			jo.put("p", lastStdModel.get(kc));
			ja.put(jo);
		}
		// adding item estimates
		for (String item : itemList) {
			jo = new JSONObject();
			jo.put("name", item);
			jo.put("p", lastStdModel.get(item));
			ja.put(jo);
		}

		JSONObject jsonStdModel = new JSONObject();
		jsonStdModel.put("item-kc-estimates", ja);
		return jsonStdModel.toString();
	}

	private void storeUpdatedStdModel(String usr, String grp, String jsonStdModel, String event) {
		BNGeneralConfigManager cm = new BNGeneralConfigManager(this);
		BNGeneralDB bnDB = new BNGeneralDB(cm.bn_dbstring, cm.bn_dbuser, cm.bn_dbpass);
		bnDB.openConnection();
		bnDB.storeUpdatedStdModel(usr, grp, jsonStdModel, event);
		bnDB.closeConnection();
	}

	private void storeNullStdModel(String usr, String grp, String jsonStdModel, String event) {
		BNGeneralConfigManager cm = new BNGeneralConfigManager(this);
		BNGeneralDB bnDB = new BNGeneralDB(cm.bn_dbstring, cm.bn_dbuser, cm.bn_dbpass);
		bnDB.openConnection();
		bnDB.storeNullStdModel(usr, grp, jsonStdModel, event);
		bnDB.closeConnection();
	}

	/**
	 * This method processes the JSON string and creates a map of items and KCs
	 * and their estimates. JSON format is: { "item-kc-estimates" : [ { "name" :
	 * "kc1", "p" : 0.79, "seq" : "0111" }, { "name" : "item1", "p" : 0.3, "seq"
	 * : "01" } ] }
	 */
	private HashMap<String, Double> getMapLastStdModel(String lastStdModel) {
		HashMap<String, Double> stdModelMap = new HashMap<String, Double>();

		// when latest model is null, initialize all concepts with empty
		if (lastStdModel == null) {
			// adding estimates of kcs
			for (String kc : kcList) {
				stdModelMap.put(kc, 0.0);
			}
			// adding estimates of items
			for (String item : itemList) {
				stdModelMap.put(item, 0.0);
			}
		} else {
			JSONObject json = new JSONObject(lastStdModel);
			JSONArray itemKCEstimates = json.getJSONArray("item-kc-estimates");
			// iterating through estimates of kcs and items
			for (int i = 0; i < itemKCEstimates.length(); i++) {
				JSONObject jsonobj = itemKCEstimates.getJSONObject(i);
				stdModelMap.put(jsonobj.getString("name"), jsonobj.getDouble("p"));
			}
		}
		if (verbose) {
			System.out.println("stdModelMap size: " + stdModelMap.size());
		}
		return stdModelMap;

	}

	/**
	 * The method to get kcList
	 */
	private HashSet<String> getKCList(String[] contentList) {
		HashSet<String> set = new HashSet<String>();
		BNGeneralConfigManager cm = new BNGeneralConfigManager(this);
		BNGeneralDB bnDB = new BNGeneralDB(cm.bn_dbstring, cm.bn_dbuser, cm.bn_dbpass);
		bnDB.openConnection();
		set = bnDB.getKCList(contentList);
		bnDB.closeConnection();
		return set;

	}

	/**
	 * The method to get item list
	 * 
	 * @param contentList
	 */
	private HashSet<String> getItemList(String[] contentList) {
		HashSet<String> set = new HashSet<String>();
		BNGeneralConfigManager cm = new BNGeneralConfigManager(this);
		BNGeneralDB bnDB = new BNGeneralDB(cm.um2_dbstring, cm.um2_dbuser, cm.um2_dbpass);
		bnDB.openConnection();
		set = bnDB.getItemList(contentList);
		bnDB.closeConnection();
		return set;
	}

	private String getLastStudentModel(String usr, String grp) {

		// check student model map, if model exists for usrgrp, return it,
		// otherwise get it from db.
		String stdModel = stdData.getLastStudentModel(usr, grp);
		if (stdModel != null)
			return stdModel;
		
		BNGeneralConfigManager cm = new BNGeneralConfigManager(this);
		BNGeneralDB bnDB = new BNGeneralDB(cm.bn_dbstring, cm.bn_dbuser, cm.bn_dbpass);
		bnDB.openConnection();
		stdModel = bnDB.getLastStudentModel(usr, grp);
		bnDB.closeConnection();
		return stdModel;
	}

	public ArrayList<Attempt> getLastAttempts(String activityName, double result) {
		ArrayList<Attempt> lastAttempts = new ArrayList<Attempt>();
		lastAttempts.add(new Attempt(activityName, result));
		if (verbose) {
			String outStr = "Last attempts: ";
			for (Attempt a : lastAttempts)
				outStr += a.getContentName() + ":" + a.getResult() + ", ";
			System.out.println(outStr);
		}
		return lastAttempts;
	}

	public HashMap<String, Double> getNewStudentModel(String usr, String grp, HashMap<String, Double> lastStudentModel,
			ArrayList<Attempt> lastAttempts, String bnFile) throws IOException {

		//System.out.println("in new student model:" + dateFormat.format(new Date()));

		
		//get the latest user network from the map if exists; otherwise, initialize a network for the user based on user model
		Network net = stdNetworkMap.get(usr + grp) ;
		if (net == null) {
			net = mainNet;
			//initialize the network using the latest student model 
			Set<String> bnNodes = new HashSet<String>(Arrays.asList(net.getAllNodeIds()));
			double prob;
			for (String bnKCId : lastStudentModel.keySet()) {
				prob = lastStudentModel.get(bnKCId); 
				if (kcList.contains(bnKCId)) {
					bnKCId = bnKCId.replaceAll(".", "_"); //mapping from KC name from parser to BN node name
					if (!bnNodes.contains(bnKCId))
						continue;
					if (net.getParentIds(bnKCId).length > 0){
						System.err.println("ERROR (bn_general/UpdateStudentModel): net.getParentIds(bnKCId).length > 0, but we don't consider skills that have parents!)");
						continue;
					}
					prob = Math.min(Math.max(prob, MIN_BN_KNOWLEDGE), 1 - MIN_BN_KNOWLEDGE);
					double[] aDef = {prob, 1 - prob};
					net.setNodeDefinition(bnKCId, aDef);
				}
			}
			net.updateBeliefs(); // we need to update beliefs after we initialized the value of KCs nodes with student model data
			
			//System.out.println("in new student model (finished initializing BN with student last model):" + dateFormat.format(new Date()));
		}		
		
		HashMap<String, Double> newStudentModel = null;	    
		if (lastAttempts == null || lastAttempts.size() == 0) {
			if (verbose)
				System.out.println("\tNo new attempts. Assign current probabilities from bn to student model.");
		} else {
			if (verbose)
				System.out.println("\tEnter new attempts...");
			for (int i = 0; i < lastAttempts.size(); i++) {
				String itemName = lastAttempts.get(i).getContentName();
				double stepValue = lastAttempts.get(i).getResult();
				String itemValue = (stepValue > 0.9999) ? "Correct" : "Wrong";
				HashSet<String> itemOldestAncesterKCIds = getOldestAncesters(net, itemName, verbose);
				newStudentModel = updateByOneEvidence(usr, grp, net, itemName, itemValue, itemOldestAncesterKCIds);
			}
		}
		
		//System.out.println("in new student model (finished creating new student model):" + dateFormat.format(new Date()));

		return newStudentModel;
	}

	public HashSet<String> getOldestAncesters(Network net, String curNodeId, boolean verbose) {
		HashSet<String> oldestAncesters = new HashSet<String>();
		if (net == null) {
			if (verbose)
				System.err.println("ERROR  (bn_general/UpdateStudentModel): net==null!");
			return null;
		}
		HashSet<String> bnNodes = new HashSet<String>(Arrays.asList(net.getAllNodeIds()));
		if (!bnNodes.contains(curNodeId)) {
			if (verbose)
				System.err.println("ERROR  (bn_general/UpdateStudentModel): !bnNodes.contains(" + curNodeId + ")");
			return null;
		}

		String[] parents = net.getParentIds(curNodeId);
		if (parents != null & parents.length > 0) {
			for (String r : parents) {// it may contain some KCs not in
										// allKCIdList!
				String[] nr = net.getParentIds(r);// indirect parents
				if (nr != null & nr.length > 0)
					for (String n : nr)
						oldestAncesters.addAll(getOldestAncesters(net, n, verbose));
				else
					oldestAncesters.add(r);
			}
		} else
			oldestAncesters.add(curNodeId);
		return oldestAncesters;
	}

	public 	HashMap<String, Double> updateByOneEvidence(String usr, String grp, Network net, String itemId, String itemValue, Set<String> KCIds) {
		/*
		 * "item": observes variables, can be problem, step, attempt, depending
		 * on the context "KC": hidden variables, can be skills, concepts KCIds:
		 * these should be the KCs in bn that are the oldest ancestors of the
		 * current item, i.e., these KCs shouldn't have parents. Only these kind
		 * of KCs can be updated direclty. Others that have parents will be
		 * determined by their parents.
		 * 
		 * Here it assumes that all KCIds are already in the bn.
		 */
		try {
			Set<String> bnNodes = new HashSet<String>(Arrays.asList(net.getAllNodeIds()));
			if (!bnNodes.contains(itemId)) {
				System.err.println("(bn_general/UpdateStudentModel): " + itemId + " is not in bn, no KC will be updated by BN!");
				return null;
			}

			if (verbose) {
				String outStr = "\tEvidence: " + itemId + "=" + itemValue + newlineSymbol + "\tBefore: ";
				for (String kcId : KCIds)
					outStr += kcId + ":" + net.getNodeValue(kcId)[0] + ", ";
				System.out.println(outStr);
			}

			net.setEvidence(itemId, itemValue); //we set the evidence
			net.updateBeliefs();  //we update the beliefs
			
			String outStr = "";
			if (verbose)
				outStr += "\tAfter: ";
	
			double pLearnedPost, validProb;
			boolean kcDefChanged = false; //we set it to true when a kc node gets a negative value after belief update. If true, we validate the
										  //definition for that kc node, and then we need to update beliefs again.
			for (String bnKCId : KCIds) {
				int nbParents = net.getParentIds(bnKCId).length;
				if (nbParents != 0) {
					System.err.println("(bn_general/UpdateStudentModel): bnKCId (to be updated in the bn) has parents! bnKCId:" + bnKCId
							+ ", #parents:" + nbParents);
					continue;
				}
				pLearnedPost = net.getNodeValue(bnKCId)[0];
				// // NOTE: This is for forcing knowledge and result to be
				// consistent, i.e, when there is a correct attempt, knowledge
				// should increase; when there is a wrong attempt, knowledge
				// shouldn't increase.
				validProb = Math.min(Math.max(pLearnedPost, MIN_BN_KNOWLEDGE), 1 - MIN_BN_KNOWLEDGE);
				if (pLearnedPost != validProb) {
					pLearnedPost = validProb;
					double[] aDef = { pLearnedPost, 1 - pLearnedPost };
					net.setNodeDefinition(bnKCId, aDef);
					kcDefChanged = true;
				}
				if (verbose) {
					outStr += bnKCId + ":" + pLearnedPost + ", ";
				}		
			}
			if (verbose)
				System.out.println(outStr + newlineSymbol);
			
			if (kcDefChanged) {
				net.updateBeliefs(); //since we changed values of one/more kc nodes (because they were negative), we need to update beliefs again
			}
			
			//System.out.println("in new student model (finished updated by one evidence):" + dateFormat.format(new Date()));

			HashMap<String, Double> newStudentModel = new HashMap<String, Double>();	    

			for (String bnId : bnNodes) {
				double knowledge = net.getNodeValue(bnId)[0];
				// check if bn node maps to KCs, if yes, replace all _ by .
				if (kcList.contains(bnId) || kcList.contains(bnId.replaceAll("_", "."))) {
					bnId = bnId.replaceAll("_", ".");
				}
				newStudentModel.put(bnId, Double.parseDouble(df4.format(knowledge)));
			}
			if (verbose)
				System.out.println("\tNew student model (same as skills from bn): " + newlineSymbol + "\t\t" + newStudentModel);
	
			
			//remove the evidence so that the evidence does not influence next update of the beliefs when next evidence is received
			net.clearEvidence(itemId); //note that we need to do this only after we got updated values. Otherwise, item node values will be invalidated

			//add the latest network of the user in the map
			stdNetworkMap.put(usr + grp, net);
	
			return newStudentModel;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setResponse(HttpServletResponse response, String output) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Cache-Control",
				"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");
		out.print(output);
	}

}
