
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;


public class StudentModelCache extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		// Step 1: get the url params
		String usr = request.getParameter("usr");
		String grp = request.getParameter("grp");
		String lastAct = request.getParameter("lastContentId");
		String lastActResult = request.getParameter("lastContentResult");
		String contents = request.getParameter("contents");
		String event = request.getParameter("event");

		// Step 3: get the last student model
		String jsonLastStdModel = StudentData.getInstance().getLastStudentModel(usr, grp);

		String params = createParamJSON(usr, grp, lastAct, lastActResult, contents, event);
		
		// Step 4: call the student model asynchronously to update its belief
		HttpAsyncClientInterface.getInstance().sendHttpAsynchPostRequest(params);

		// Step 5: return student model JSON, if new student model is null
		// return an empty string
		System.out.println("Sending cache data to recommendation...");

		String output = jsonLastStdModel == null ? "" : jsonLastStdModel;
		setResponse(response, output);

	}

	private String createParamJSON(String usr, String grp, String lastAct, 
			String lastActResult, String contents,
			String event) {
		JSONObject json = new JSONObject();
		json.put("usr", usr);
		json.put("grp", grp);
		json.put("lastContentId", lastAct);
		json.put("lastContentResult", lastActResult);
		json.put("contents", contents);
		json.put("event", event);
		return json.toString();
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
