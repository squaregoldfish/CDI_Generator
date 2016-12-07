package no.bcdc.cdigenerator.importers;

public class ValueLookupException extends Exception {
	
	private static final long serialVersionUID = -8184505709001633446L;

	public ValueLookupException(String valueName, String message) {
		super(valueName + ": " + message);
	}

	public ValueLookupException(String valueName, String message, Throwable cause) {
		super(valueName + ": " + message, cause);
	}
}
