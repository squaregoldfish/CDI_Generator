package no.bcdc.cdigenerator;

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
}
