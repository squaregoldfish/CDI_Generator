package no.bcdc.cdigenerator.generators;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An exception triggered when required data cannot be found in the database
 * @author Steve Jones
 *
 */
public class MissingDatabaseDataException extends Exception {
	
	private static final long serialVersionUID = -3446368309446572503L;

	/**
	 * A custom error message, used by the specialist constructors
	 */
	private String customMessage = null;
	
	/**
	 * Basic constructor with a simple message
	 * @param message
	 */
	public MissingDatabaseDataException(String message) {
		super(message);
	}
	
	/**
	 * Constructor for missing data in the platform lookup.
	 * 
	 * The dataset ID can be null if it's not relevant for the current search
	 * 
	 * @param platformCode The platform code
	 * @param startDate The start date
	 * @param datasetId The dataset ID (optionnal)
	 */
	public MissingDatabaseDataException(String platformCode, Date startDate, String datasetId) {
		super();
		
		StringBuilder message = new StringBuilder("The platform ID for platform ");
		message.append(platformCode);
		message.append(" and start date ");
		message.append(new SimpleDateFormat("yyy-MM-dd").format(startDate));
		
		if (null != datasetId) {
			message.append(" for dataset ID ");
			message.append(datasetId);
		}
		
		message.append(" is not in the database");

		customMessage = message.toString();
	}
	
	@Override
	public String getMessage() {
		// If we've set a custom message, use that.
		return (null == customMessage ? super.getMessage() : customMessage);
	}
}
