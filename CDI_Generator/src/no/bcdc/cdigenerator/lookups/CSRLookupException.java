package no.bcdc.cdigenerator.lookups;

/**
 * Class for errors in CSR lookups
 * @author Steve Jones
 *
 */
public class CSRLookupException extends Exception {
	
	/**
	 * The Serial Version UID
	 */
	private static final long serialVersionUID = 7852382218843753018L;

	/**
	 * Constructor for an error found on a specific line in the CSR file
	 * @param line The line number
	 * @param message The error message
	 */
	public CSRLookupException(int line, String message) {
		super("Error on CSR file line " + line + ": " + message);
	}

}
