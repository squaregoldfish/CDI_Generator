package no.bcdc.cdigenerator.importers;

import java.util.List;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.Generator;

/**
 * Parent class of all importers. Lists the required methods
 * @author Steve Jones
 *
 */
public abstract class Importer {

	/**
	 * The basic importer has no constructor activities
	 */
	public Importer() {
		// Do nothing!
	}
	
	/**
	 * Initialise the importer. This usually doesn't need to do anything,
	 * so an empty version is provided here.
	 */
	public void init(Config config) {
		// Default initialiser does nothing. It can be overridden if needed.
	}
	
	/**
	 * Begin the importing process
	 * @param generator The generator, so we can send/receive information to/from it.
	 */
	public void start(Generator generator) {
		
		// Get the list of data set IDs to be imported from the generator
		List<String> dataSetIds = generator.getDataSetIds(getDataSetIdsDescriptor());
		
		// A null set of IDs means we just stop
		if (null != dataSetIds) {
			
			for (String id : dataSetIds) {
				generator.logMessage(id, "is the DOI I have processed");
			}
		}
	}
	
	/**
	 * Returns the descriptive name of the data set IDs, e.g. "DOIs"
	 * @return The descriptive name of the data set IDs
	 */
	public abstract String getDataSetIdsDescriptor();
	
	/**
	 * Returns the descriptive name of a single data set ID, e.g. "DOI"
	 * @return The descriptive name of the data set ID
	 */
	public abstract String getDataSetIdDescriptor();
	
	/**
	 * Returns the format of the data set IDs for this importer, to be displayed to the user
	 * @return The format of the data set IDs
	 */
	public abstract String getDataSetIdFormat();
	
	/**
	 * Validate an ID's format to make sure it looks right.
	 * Note that this doesn't necessarily mean that it *is* right...
	 * 
	 * @param id The id
	 * @return {@code true} if the ID appears to be valid; {@code false} if it does not.
	 */
	public abstract boolean validateIdFormat(String id);
}
