package no.bcdc.cdigenerator.generators;

/**
 * An exception triggered when required data cannot be found in the database
 * @author Steve Jones
 *
 */
public class MissingDatabaseDataException extends Exception {
	
	private static final long serialVersionUID = -7365362738023738208L;

	public MissingDatabaseDataException(String message) {
		super(message);
	}

}
