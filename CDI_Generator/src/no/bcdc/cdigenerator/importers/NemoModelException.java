package no.bcdc.cdigenerator.importers;

import java.io.File;

/**
 * Exception for errors in NEMO model files
 * @author Steve Jones
 *
 */
public class NemoModelException extends ImporterException {

	private static final long serialVersionUID = 1601241092793521943L;

	/**
	 * Basic constructor
	 * @param modelFile The model file where the error occurred
	 * @param message The error message
	 */
	public NemoModelException(File modelFile, String message) {
		super("Error in NEMO Model file '" + modelFile.getAbsolutePath() + "': " + message);
	}
}
