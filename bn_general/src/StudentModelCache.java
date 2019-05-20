
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;


public class StudentModelCache extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)  {

		try {
			// Step 1: get the url params
			String usr = request.getParameter("usr");
			String grp = request.getParameter("grp");
			String lastAct = request.getParameter("lastContentId");
			String lastActResult = request.getParameter("lastContentResult");
			String contents = request.getParameter("contents");
			String event = (request.getParameter("event") == null? "" : request.getParameter("event"));
	        String updatesm = request.getParameter("updatesm");

			// Step 3: get the last student model
			String jsonLastStdModel = StudentData.getInstance(this).getLastStudentModel(usr, grp);

			String params = createParamJSON(usr, grp, lastAct, lastActResult, contents, event);
			
			// Step 4: call the student model asynchronously to update its belief
			// call student model only if updatesm is true and the result is in valid range 0-1
			if (updatesm != null && updatesm.equals("true")) {
				try {
					double result = Double.parseDouble(lastActResult);
					if (result >=0 && result <=1) {
						HttpAsyncClientInterface.getInstance().sendHttpAsynchPostRequest(params);	
					}
				} catch (Exception e) {
					System.out.println("bn_general/StudentModelCache: last activity result is not between 0-1. So, UpdateStudentModel is not called.");
				}
			}

			// Step 5: return student model JSON, if new student model is null
			// return an empty string
			String output = jsonLastStdModel == null ? "{\"item-kc-estimates\":[]}" : jsonLastStdModel;
			setResponse(response, output);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
