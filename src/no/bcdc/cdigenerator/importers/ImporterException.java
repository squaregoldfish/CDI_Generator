package no.bcdc.cdigenerator.importers;

public class ImporterException extends Exception {
	
	private static final long serialVersionUID = -3792809123094028710L;

	public ImporterException(String message) {
		super(message);
	}
	
	public ImporterException(String message, Throwable cause) {
		super(message + ": " + cause.getMessage(), cause);
	}
}
