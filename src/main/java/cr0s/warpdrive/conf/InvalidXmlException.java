package cr0s.warpdrive.conf;

public class InvalidXmlException extends Exception {

	public InvalidXmlException() {
		super("An unknown xml error occured");
	}

	public InvalidXmlException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public InvalidXmlException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public InvalidXmlException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public InvalidXmlException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
