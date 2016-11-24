package no.bcdc.cdigenerator.importers;

public class MissingLookupValueException extends ValueLookupException {

	private static final long serialVersionUID = 443655177882529970L;

	public MissingLookupValueException(String valueName) {
		super(valueName, "Value empty or missing");
	}
}
