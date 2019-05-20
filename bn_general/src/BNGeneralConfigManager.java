
import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class BNGeneralConfigManager {

	// database connection parameters
	public String bn_dbstring;
	public String bn_dbuser;
	public String bn_dbpass;
	public String um2_dbstring;
	public String um2_dbuser;
	public String um2_dbpass;

	private static String config_string = "./WEB-INF/bn_general_config.xml";
	private static String config_string_no_servlet = "./WebContent/WEB-INF/bn_general_config.xml";

	public BNGeneralConfigManager(HttpServlet servlet) {
		try {
			ServletContext context = servlet.getServletContext();
			// System.out.println(context.getContextPath());
			InputStream input = context.getResourceAsStream(config_string);
			if (input != null) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(input);
				doc.getDocumentElement().normalize();

				// set database connection parameters
				bn_dbstring = doc.getElementsByTagName("bn_dbstring").item(0).getTextContent().trim();
				bn_dbuser = doc.getElementsByTagName("bn_dbuser").item(0).getTextContent().trim();
				bn_dbpass = doc.getElementsByTagName("bn_dbpass").item(0).getTextContent().trim();
				um2_dbstring = doc.getElementsByTagName("um2_dbstring").item(0).getTextContent().trim();
				um2_dbuser = doc.getElementsByTagName("um2_dbuser").item(0).getTextContent().trim();
				um2_dbpass = doc.getElementsByTagName("um2_dbpass").item(0).getTextContent().trim();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
