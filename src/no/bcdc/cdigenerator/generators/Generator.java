package no.bcdc.cdigenerator.generators;

import java.util.List;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.Importer;

/**
 * Abstract generator class
 * @author Steve Jones
 *
 */
public abstract class Generator {

	/**
	 * The application's configuration
	 */
	protected Config config;
	
	/**
	 * The importer being used
	 */
	protected Importer importer;
	
	/**
	 * The maximum value for the progress monitor
	 */
	protected int progressMax = 1;
	
	/**
	 * The current value of the progress monitor
	 */
	protected int progress = 0;
	
	/**
	 * The data set ID currently being processed
	 */
	protected String currentDataSetId = null;
	
	/**
	 * The text shown with the progress monitor
	 */
	protected String progressMessage = "Processing...";
	
	/**
	 * Base constructor - stores the configuration
	 * @param config The configuration
	 */
	public Generator(Config config) {
		this.config = config;
	}

	/**
	 * Starts the generator. This is the main program.
	 * @throws Exception Any errors are passed up to be handled by the main method. Ideally there shouldn't be any, obviously
	 */
	protected abstract void start() throws Exception;
	
	/**
	 * Get the list of IDs for the data sets that are to be imported
	 * @param dataSetIdsDescriptor The descriptive name for the data set IDs (e.g. "DOIs")
	 * @return The list of data set IDs, or {@code null} if we are aborting the process
	 */
	public abstract List<String> getDataSetIds(String dataSetIdsDescriptor);
	
	/**
	 * Log a message for viewing after the processing is completed.
	 * @param message The message to be logged
	 */
	public abstract void logMessage(String dataSetId, String message);
	
	/**
	 * Set the maximum value for the progress monitor
	 * @param progressMax The maximum value for the progress monitor
	 */
	public void setProgressMax(int progressMax) {
		this.progressMax = progressMax;
	}
	
	/**
	 * Set the current value of the progress monitor
	 * @param progress The progress value
	 */
	public void setProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * Set the progress message
	 * @param progressMessage The progress message
	 */
	public void setProgressMessage(String progressMessage) {
		this.progressMessage = progressMessage;
	}
	
	/**
	 * Set the name of the data set ID current being processed
	 * @param dataSetId The ID of the data set currently being processed
	 */
	public void setCurrentDataSetId(String dataSetId) {
		this.currentDataSetId = dataSetId;
	}
	
	/**
	 * Update the progress display 
	 */
	public abstract void updateProgressDisplay();
}