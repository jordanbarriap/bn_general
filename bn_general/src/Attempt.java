
public class Attempt {
	private String contentName;
	private double result;

	public Attempt(String contentName, double result) {
		// super();
		this.contentName = contentName;
		this.result = result;
	}

	public String getContentName() {
		return contentName;
	}

	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}

}
