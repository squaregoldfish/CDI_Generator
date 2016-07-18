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
		List<String> dataSetIds = generator.getDataSetIds();
		
		System.out.println("I am the Importer, and I have the list of data set IDs. There are " + dataSetIds.size() + ".");
	}
}
