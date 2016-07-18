package no.bcdc.cdigenerator.importers;

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
	public void init() {
		// Default initialiser does nothing. It can be overridden if needed.
	}
	
	public abstract void doAThing();
	
}
