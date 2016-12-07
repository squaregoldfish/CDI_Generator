package no.bcdc.cdigenerator.importers;

public class InvalidLookupValueException extends ValueLookupException {

	private static final long serialVersionUID = 6934450459587853920L;

	/**
	 * Constructor takes the name of the value being looked up,
	 * and the underlying parsing error (e.g. a NumberFormatException).
	 * @param valueName The name of the value being looked up
	 * @param cause The parsing error 
	 */
	public InvalidLookupValueException(String valueName, Throwable cause) {
		super(valueName, "Invalid value", cause);
	}
	
}
