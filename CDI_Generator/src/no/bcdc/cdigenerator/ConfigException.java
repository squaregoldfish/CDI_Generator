package no.bcdc.cdigenerator;

import java.io.File;

/**
 * Exception for the CDI Generator configuration
 * @author Steve Jones
 *
 */
public class ConfigException extends Exception {
	
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 8613470085736098536L;

	/**
	 * Simple constructor with just a message
	 * @param message The message
	 */
	public ConfigException(String message) {
		super(message);
	}
	
	/**
	 * Simple constructor with a message and a cause
	 * @param message The message
	 * @param cause The underlying cause
	 */
	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * A configuration exception related to a file or directory.
	 * @param file The file or directory where the problem was found
	 * @param message The description of the problem
	 */
	public ConfigException(File file, String message) {
		super(file.getAbsolutePath() + ": " + message);
	}
}
