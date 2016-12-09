package no.bcdc.couplingtablebuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Configuration for the CDI Generator
 * @author Steve Jones
 *
 */
public class Config extends Properties {
	
	private static final long serialVersionUID = -855818585035627883L;

	/**
	 * The key for importers
	 */
	private static final String FILE_DIR_PROPERTY = "dir.files";
	
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
	 * Files directory
	 */
	private File filesDir;
	
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
	 * Initialise and load the configuration
	 * @param configReader A reader for the config file
	 * @throws IOException If an error occurs while reading the file data
	 * @throws ConfigException If there are errors in the configuration
	 */
	public Config(Reader configReader) throws IOException, ConfigException, IllegalAccessException, InstantiationException {
		super();
		load(configReader);
		checkFilesDir();
		
		dbServer = getProperty(DB_SERVER_PROPERTY);
		dbPort = extractPositiveInteger(DB_PORT_PROPERTY);
		dbName = getProperty(DB_NAME_PROPERTY);
		dbUser = getProperty(DB_USER_PROPERTY);
		dbPassword = getProperty(DB_PASSWORD_PROPERTY);
	}
	
	/**
	 * Set up and check the temp directory
	 * @throws ConfigException If the models directory is incorrectly configured
	 */
	private void checkFilesDir() throws ConfigException {
		String filesDirString = getProperty(FILE_DIR_PROPERTY);
		if (null == filesDirString) {
			throw new ConfigException(FILE_DIR_PROPERTY + " not specified");
		}

		filesDir = new File(filesDirString);
		checkDir(filesDir, false);
	}
	
	/**
	 * Check a directory for existence, directoryness, and readability and writeability
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
	 * Get the directory containing the data files
	 * @return The files directory
	 */
	public File getFilesDir() {
		return filesDir;
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
}
