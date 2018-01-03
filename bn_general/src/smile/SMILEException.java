package smile;

public class SMILEException extends RuntimeException {
	public SMILEException(String msg, int errorCode) {
        super(msg);
		this.errorCode = errorCode;
    }

    public SMILEException(String msg) {
        super(msg);
    }

	public int getErrorCode() { 
		return errorCode; 
	}

	private int errorCode;
}
