
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

public class HttpAsyncClientInterface {

	private final String bnServiceURL = "http://adapt2.sis.pitt.edu/bn_general/UpdateStudentModel";
	private CloseableHttpAsyncClient client;
	private static HttpAsyncClientInterface instance;

	private HttpAsyncClientInterface() {
		client = HttpAsyncClients.createDefault();
		client.start();
	}

	public static HttpAsyncClientInterface getInstance() {
		if (instance == null) {
			instance = new HttpAsyncClientInterface();
		}
		return instance;
	}

	public void sendHttpAsynchPostRequest(String params) {
		try {
			if (client.isRunning() == false) {
				client.close();
				client = HttpAsyncClients.createDefault();
				client.start();
			}
			
			StringEntity entity = new StringEntity(params,
					         ContentType.create("application/json", Consts.UTF_8));
			HttpPost request = new HttpPost(bnServiceURL);
			request.setEntity(entity);

			client.execute(request, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {
					System.out.println(request.getRequestLine() + "->" + response.getStatusLine());
				}

				@Override
				public void failed(final Exception ex) {
					System.out.println(request.getRequestLine() + "->" + ex);
				}

				@Override
				public void cancelled() {
					System.out.println(request.getRequestLine() + " cancelled");
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
