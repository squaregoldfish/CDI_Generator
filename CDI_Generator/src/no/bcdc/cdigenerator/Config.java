package no.bcdc.cdigenerator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import no.bcdc.cdigenerator.importers.Importer;

/**
 * Configuration for the CDI Generator
 * @author Steve Jones
 *
 */
public class Config extends Properties {
	
	private static final long serialVersionUID = -9088488147547028352L;

	/**
	 * The key for importers
	 */
	private static final String IMPORTERS_PROPERTY = "importers";
	
	/**
	 * The default package for importer classes. Used if the package isn't set in the config file.
	 */
	private static final String DEFAULT_IMPORTER_PACKAGE = "no.bcdc.cdigenerator.importers.concrete";
	
	/**
	 * The key for the temporary/cache directory
	 */
	private static final String TEMP_DIR_PROPERTY = "dir.temp";
	
	/**
	 * The key for the NEMO models directory
	 */
	private static final String NEMO_TEMPLATES_DIR_PROPERTY = "dir.nemoTemplates";
	
	/**
	 * The key for the NEMO output directory
	 */
	private static final String NEMO_OUTPUT_DIR_PROPERTY = "dir.nemoOutput";
	
	/**
	 * The key for the NEMO working directory
	 */
	private static final String NEMO_WORKING_DIR_PROPERTY = "dir.nemoWorking";
	
	/**
	 * The key for the number of network retries
	 */
	private static final String NETWORK_RETRIES_PROPERTY = "network.retryCount";
	
	/**
	 * The key for the retry wait time
	 */
	private static final String RETRY_WAIT_TIME_PROPERTY = "network.retryWaitTime";
	
	/**
	 * The key for the database server
	 */
	private static final String DB_SERVER_PROPERTY = "db.server";
	
	/**
	 * The key for the database port
	 */
	private static final String DB_PORT_PROPERTY = "db.port";
	
	/**
	 * The key for the database name
	 */
	private static final String DB_NAME_PROPERTY = "db.database";
	
	/**
	 * The key for the database user
	 */
	private static final String DB_USER_PROPERTY = "db.user";
	
	/**
	 * The key for the database password
	 */
	private static final String DB_PASSWORD_PROPERTY = "db.password";
	
	/**
	 * The key for the CSR lookup file
	 */
	private static final String CSR_URL_PROPERTY = "url.csrs";
	
	/**
	 * Lookup table of importers
	 */
	private TreeMap<String, Importer> importers = null;
	
	/**
	 * Temp directory
	 */
	private File tempDir;
	
	/**
	 * NEMO Models directory
	 */
	private File nemoTemplatesDir;
	
	/**
	 * NEMO Output directory
	 */
	private File nemoOutputDir;
	
	/**
	 * NEMO Working directory
	 */
	private File nemoWorkingDir;

	/**
	 * The number of attempts to make when retrieving data across the net
	 */
	private int networkRetries;
	
	/**
	 * The number of seconds to wait between network retries
	 */
	private int retryWaitTime;
	
	/**
	 * The database server
	 */
	private String dbServer;
	
	/**
	 * The database port
	 */
	private int dbPort;
	
	/**
	 * The database name
	 */
	private String dbName;
	
	/**
	 * The database user
	 */
	private String dbUser;
	
	/**
	 * The database password
	 */
	private String dbPassword;
	
	/**
	 * The URL for downloading CSR data 
	 */
	private URL csrDownloadUrl;
	
	/**
	 * Initialise and load the configuration
	 * @param configReader A reader for the config file
	 * @throws IOException If an error occurs while reading the file data
	 * @throws ConfigException If there are errors in the configuration
	 */
	public Config(Reader configReader) throws IOException, ConfigException, IllegalAccessException, InstantiationException {
		super();
		load(configReader);
		checkTempDir();
		checkNemoTemplatesDir();
		checkNemoOutputDir();
		checkNemoWorkingDir();
		extractImporters();
		setupCSRUrl();
		
		networkRetries = extractZeroPositiveInteger(NETWORK_RETRIES_PROPERTY);
		retryWaitTime = extractZeroPositiveInteger(RETRY_WAIT_TIME_PROPERTY);
		
		dbServer = getProperty(DB_SERVER_PROPERTY);
		dbPort = extractPositiveInteger(DB_PORT_PROPERTY);
		dbName = getProperty(DB_NAME_PROPERTY);
		dbUser = getProperty(DB_USER_PROPERTY);
		dbPassword = getProperty(DB_PASSWORD_PROPERTY);
	}
	
	/**
	 * Checks that the configuration is valid.
	 * @throws ConfigException If there are any errors in the configuration
	 */
	private void extractImporters() throws ConfigException, IllegalAccessException, InstantiationException {
		
		importers = new TreeMap<String, Importer>();
		
		// Loop through all the keys, looking for those with the right prefix
		String[] importerClasses = getProperty(IMPORTERS_PROPERTY).split(";");
		
		for (String className : importerClasses) {

			// Add the default prefix if required
			if (className.indexOf('.') == -1) {
				className = DEFAULT_IMPORTER_PACKAGE + "." + className;
			}
			
			// Check the class exists and extends the correct class
			try {
				Class<?> clazz = Class.forName(className);
				if (!Importer.class.isAssignableFrom(clazz)) {
					throw new ConfigException("Importer class '" + className + "' is not a subclass of no.bcdc.cdigenerator.Importer");
				}
				
				// Create an instance of the class, and check that there are
				// NEMO models for it
				Constructor<?> constructor = clazz.getConstructor(Config.class);
				Importer importer = (Importer) constructor.newInstance(this);
				
				File modelsDir = importer.getModelsDir();
				if (!modelsDir.exists()) {
					throw new ConfigException("The NEMO models directory for " + importer.getName() + " is missing");
				}
				if (!modelsDir.isDirectory()) {
					throw new ConfigException("The NEMO models directory for " + importer.getName() + " is not a directory");
				}
				if (!modelsDir.canRead()) {
					throw new ConfigException("Cannot access the NEMO models directory for " + importer.getName());
				}
				if (modelsDir.listFiles().length == 0) {
					throw new ConfigException("The NEMO models directory for " + importer.getName() + " is empty");
				}
				
				// Store the importer
				importers.put(importer.getName(), importer);
				
			} catch (ClassNotFoundException e) {
				throw new ConfigException("Importer class '" + className + "' does not exist");
			} catch (NoSuchMethodException e) {
				throw new ConfigException("Importer class '" + className + "' does not have a Config constructor");
			} catch (IllegalArgumentException|InvocationTargetException e) {
				throw new ConfigException("Could not instatiate importer class '" + className, e);
			}
		}
		
		if (importers.keySet().size() == 0) {
			throw new ConfigException("No importers specified in config file");
		}
	}
	
	/**
	 * Returns a sorted list of all the importer names
	 * @return The list of importer names
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public List<String> getImporterNames() {
		return new ArrayList<String>(importers.keySet());		
	}
	
	/**
	 * Retrieve the importer identified by the specified name
	 * @param importerName The name of the importer
	 * @return The Importer object
	 * @throws ConfigException If the importer cannot be found
	 */
	public Importer getImporter(String name) throws ConfigException {
		Importer importer = importers.get(name);
		return importer;
	}
	
	/**
	 * Set up and check the temp directory
	 * @throws ConfigException If the models directory is incorrectly configured
	 */
	private void checkTempDir() throws ConfigException {
		String tempDirString = getProperty(TEMP_DIR_PROPERTY);
		if (null == tempDirString) {
			throw new ConfigException(TEMP_DIR_PROPERTY + " not specified");
		}

		tempDir = new File(tempDirString);
		checkDir(tempDir, true);
	}
	
	/**
	 * Set up and check the NEMO models directory
	 * @throws ConfigException If the directory is incorrectly configured
	 */
	private void checkNemoTemplatesDir() throws ConfigException {
		String tempDirString = getProperty(NEMO_TEMPLATES_DIR_PROPERTY);
		if (null == tempDirString) {
			throw new ConfigException(NEMO_TEMPLATES_DIR_PROPERTY + " not specified");
		}

		nemoTemplatesDir = new File(tempDirString);
		checkDir(nemoTemplatesDir, false);
	}
	
	/**
	 * Set up and check the NEMO output directory
	 * @throws ConfigException If the directory is incorrectly configured
	 */
	private void checkNemoOutputDir() throws ConfigException {
		String tempDirString = getProperty(NEMO_OUTPUT_DIR_PROPERTY);
		if (null == tempDirString) {
			throw new ConfigException(NEMO_OUTPUT_DIR_PROPERTY + " not specified");
		}

		nemoOutputDir = new File(tempDirString);
		checkDir(nemoOutputDir, true);
	}
	
	/**
	 * Set up and check the NEMO working directory
	 * @throws ConfigException If the directory is incorrectly configured
	 */
	private void checkNemoWorkingDir() throws ConfigException {
		String tempDirString = getProperty(NEMO_WORKING_DIR_PROPERTY);
		if (null == tempDirString) {
			throw new ConfigException(NEMO_WORKING_DIR_PROPERTY + " not specified");
		}

		nemoWorkingDir = new File(tempDirString);
		checkDir(nemoWorkingDir, false);
	}
	
	/**
	 * Set up and check the CSR lookups file
	 * @throws ConfigException If the file is incorrectly configured
	 */
	private void setupCSRUrl() throws ConfigException {
		String csrDownloadUrlString = getProperty(CSR_URL_PROPERTY);
		if (null == csrDownloadUrlString) {
			throw new ConfigException(CSR_URL_PROPERTY + "not specified");
		}
		
		try {
			csrDownloadUrl = new URL(csrDownloadUrlString);
		} catch (MalformedURLException e) {
			throw new ConfigException("CSR Download URL is invalid", e);
		}
	}
	
	/**
	 * Check a directory for existence, directoryness, readability and (if required) writeability
	 * @throws ConfigException If the directory has none of those things.
	 */
	private void checkDir(File directory, boolean mustBeWritable) throws ConfigException {
		if (!directory.exists()) {
			throw new ConfigException(directory, "does not exist");
		} else if (!directory.isDirectory()) {
			throw new ConfigException(directory, "not a directory");
		} else if (!directory.canRead()) {
			throw new ConfigException(directory, "not readable");
		} else if (mustBeWritable && !directory.canWrite()) {
			throw new ConfigException(directory, "not writeable");
		}
	}
	
	/**
	 * Parse a number from a named property. The number must be an integer,
	 * and must be either zero or positive
	 * @param propertyKey The property
	 * @return The parsed number
	 * @throws ConfigException If the number cannot be parsed, or is negative
	 */
	private int extractZeroPositiveInteger(String propertyKey) throws ConfigException {
		int result;
		
		String propertyValue = getProperty(propertyKey);
		if (null == propertyValue) {
			throw new ConfigException(propertyKey + " is missing");
		} else {
			try {
				result = Integer.parseInt(propertyValue);
				if (result < 0) {
					throw new ConfigException(propertyKey + " must be zero or positive");
				}
			} catch (NumberFormatException e) {
				throw new ConfigException(propertyKey + " must be an integer");
			}
		}
		
		return result;
	}
	
	/**
	 * Parse a number from a named property. The number must be a positive integer,
	 * @param propertyKey The property
	 * @return The parsed number
	 * @throws ConfigException If the number cannot be parsed, or is not positive
	 */
	private int extractPositiveInteger(String propertyKey) throws ConfigException {
		int result;
		
		String propertyValue = getProperty(propertyKey);
		if (null == propertyValue) {
			throw new ConfigException(propertyKey + " is missing");
		} else {
			try {
				result = Integer.parseInt(propertyValue);
				if (result <= 0) {
					throw new ConfigException(propertyKey + " must be positive");
				}
			} catch (NumberFormatException e) {
				throw new ConfigException(propertyKey + " must be an integer");
			}
		}
		
		return result;
	}
	
	/**
	 * Get the temporary directory
	 * @return The temporary directory
	 */
	public File getTempDir() {
		return tempDir;
	}
	
	/**
	 * Get the NEMO templates directory
	 * @return The NEMO templates directory
	 */
	public File getNemoTemplatesDir() {
		return nemoTemplatesDir;
	}
	
	/**
	 * Get the NEMO output directory
	 * @return The NEMO output directory
	 */
	public File getNemoOutputDir() {
		return nemoOutputDir;
	}
	
	/**
	 * Get the NEMO working directory
	 * @return The NEMO working directory
	 */
	public File getNemoWorkingDir() {
		return nemoWorkingDir;
	}
	
	/**
	 * Get the number of times a network operation should be retried
	 * @return The retry count
	 */
	public int getNetworkRetries() {
		return networkRetries;
	}
	
	/**
	 * Get the number of seconds to wait before retrying a network operation
	 * @return The wait time
	 */
	public int getRetryWaitTime() {
		return retryWaitTime;
	}
	
	/**
	 * Get the database server
	 * @return The database server
	 */
	public String getDBServer() {
		return dbServer;
	}
	
	/**
	 * Get the database port
	 * @return The database port
	 */
	public int getDBPort() {
		return dbPort;
	}
	
	/**
	 * Get the database name
	 * @return The database name
	 */
	public String getDBName() {
		return dbName;
	}
	
	/**
	 * Get the database username
	 * @return The database username
	 */
	public String getDBUser() {
		return dbUser;
	}
	
	/**
	 * Get the database password
	 * @return The database password
	 */
	public String getDBPassword() {
		return dbPassword;
	}
	
	/**
	 * Get the CSR lookups file
	 * @return The CSR lookups file
	 */
	public URL getCSRDownloadUrl() {
		return csrDownloadUrl;
	}
}
