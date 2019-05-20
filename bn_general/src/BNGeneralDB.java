
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BNGeneralDB extends DBInterface {
	public boolean verbose;

	public BNGeneralDB(String connurl, String user, String pass) {
		super(connurl, user, pass);
		verbose = false;
	}

	public HashSet<String> getKCList(String[] contentList) {

		HashSet<String> set = null;
		try {
			String availableContentText = getContents(contentList);
			stmt = conn.createStatement();

			String query = "SELECT distinct concept FROM bn_general.ent_content_concepts " + 
			" where content_name in ("+ availableContentText + ");";

			if (verbose) {
				System.out.println(query);
			}
			rs = stmt.executeQuery(query);
			set = new HashSet<String>();
			while (rs.next()) {
				set.add(rs.getString(1));
			}
			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			this.releaseStatement(stmt, rs);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			this.releaseStatement(stmt, rs);
		}
		return set;

	}

	public HashSet<String> getItemList(String[] contentList) {

		try {
			String availableContentText = getContents(contentList);
			stmt = conn.createStatement(); // create a statement

			String query = " SELECT distinct activity from um2.ent_activity " + " where appid in (25,44,47) "
					+ " and activity in (" + availableContentText + ");";

			if (verbose) {
				System.out.println(query);
			}
			rs = stmt.executeQuery(query);
			HashSet<String> activityList = new HashSet<String>();
			while (rs.next()) {
				activityList.add(rs.getString("activity"));
			}
			this.releaseStatement(stmt, rs);
			return activityList;
		} catch (SQLException ex) {
			this.releaseStatement(stmt, rs);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		} finally {
			this.releaseStatement(stmt, rs);
		}
	}

	private static String getContents(String[] contentList) {
		if (contentList == null)
			return "";
		String contents = "";
		for (String c : contentList)
			contents += "'" + c + "',";
		if (contents.length() > 0)
			contents = contents.substring(0, contents.length() - 1); // this is
																		// for
																		// ignoring
																		// the
																		// last
																		// ,
		return contents;
	}

	public String getLastStudentModel(String std, String grp) {
		String stdModel = null;
		try {
			stmt = conn.createStatement();
			String query = "select std_model from ent_student_models_latest where user_id = '" + std
					+ "' and group_id = '" + grp + "';";

			if (verbose) {
				System.out.println(query);
			}
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				stdModel = getLargerString(rs, 1);				
			}
			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			this.releaseStatement(stmt, rs);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			this.releaseStatement(stmt, rs);
		}
		return stdModel;
	}

	public void storeUpdatedStdModel(String usr, String grp, String stdModel, String event) {
		try {
			String query = "";

			query = "UPDATE ent_student_models_latest SET std_model='" + stdModel +
					"', `event`='" + event + "', datentime=now() " +
					" WHERE user_id = '" + usr + "' and group_id='" + grp + "';";

			stmt = conn.createStatement();
			if (verbose) {
				System.out.println("storeUpdatedStdModel:" + query);
			}
			int rowCount = stmt.executeUpdate(query);
			if (rowCount == 0) { // if student has no records in
									// ent_student_models_latest, insert a row

				query = "INSERT INTO ent_student_models_latest (user_id,group_id,datentime,std_model,`event`) VALUES "
						+ "('" + usr + "','" + grp + "',now(),'" + stdModel + "','" + event + "');";
			}

			query = "INSERT INTO ent_student_models_history (user_id,group_id,datentime,std_model,`event`) VALUES " + "('"
					+ usr + "','" + grp + "',now(),'" + stdModel + "','" + event + "');";
			if (verbose) {
				System.out.println("storeUpdatedStdModel:" + query);
			}
			stmt.execute(query);

			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
		} finally {
			this.releaseStatement(stmt, rs);
		}

	}

	public void storeNullStdModel(String usr, String grp, String stdModel, String event) {

		try {
			String query = "";

			query = "INSERT INTO ent_student_models_latest (user_id,group_id,datentime,std_model,`event`) VALUES " + "('"
					+ usr + "','" + grp + "',now(),'" + stdModel + "','" + event + "');";

			stmt = conn.createStatement();
			if (verbose) {
				System.out.println("storeNullStdModel:" + query);
			}
			stmt.execute(query);
			query = "INSERT INTO ent_student_models_history (user_id,group_id,datentime,std_model,`event`) VALUES " + "('"
					+ usr + "','" + grp + "',now(),'" + stdModel + "','" + event + "');";
			if (verbose) {
				System.out.println("storeNullStdModel:" + query);
			}
			stmt.execute(query);

			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
		} finally {
			this.releaseStatement(stmt, rs);
		}

	}

	// Sees if a user has a model for a specific course id
	public boolean existComputedModel(String usr, String grp) {
		int n = 0;
		try {
			stmt = conn.createStatement();
			String query = "SELECT count(*) as npm " + "FROM ent_student_models_latest  " + "WHERE user_id='" + usr
					+ "' and group_id='" + grp + "';";
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				n = rs.getInt("npm");
			}
			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
		} finally {
			this.releaseStatement(stmt, rs);
		}
		return n > 0;
	}

	public String getLargerString(ResultSet rs, int columnIndex) throws SQLException {

		InputStream in = null;
		int BUFFER_SIZE = 1024;
		try {
			
			in = rs.getAsciiStream(columnIndex);
			if (in == null) {
				return null;
			}

			byte[] arr = new byte[BUFFER_SIZE];
			StringBuffer buffer = new StringBuffer();
			int numRead = in.read(arr);
			while (numRead != -1) {
				buffer.append(new String(arr, 0, numRead));
				numRead = in.read(arr);
			}
			if (verbose) {
				System.out.println(buffer.toString());
			}
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		}
	}
	
	public Map<String,String> getLatestStudentModels(){
		Map<String,String> stdModelMap = new HashMap<String, String>();
		try {
			stmt = conn.createStatement();
			String query = "SELECT * FROM bn_general.ent_student_models_latest";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String user = rs.getString("user_id");
				String grp = rs.getString("group_id");
				String stdModel = rs.getString("std_model");
				String userKey = user+grp;
				stdModelMap.put(userKey, stdModel);
			}
			this.releaseStatement(stmt, rs);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
		} finally {
			this.releaseStatement(stmt, rs);
		}
		return stdModelMap;
	}
}
