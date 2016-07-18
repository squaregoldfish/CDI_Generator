package no.bcdc.cdigenerator;

import java.util.List;

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
}
