package no.bcdc.cdigenerator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import no.bcdc.cdigenerator.importers.Importer;
import no.bcdc.cdigenerator.importers.ModelFilenameFilter;

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
	private static final String TEMP_DIR_PROPERTY = "config.tempDir";
	
	/**
	 * The key for the models directory
	 */
	private static final String NEMO_TEMPLATES_DIR_PROPERTY = "config.nemoTemplatesDir";
	
	/**
	 * Lookup table of importers
	 */
	private TreeMap<String, Importer> importers = null;
	
	/**
	 * Temp directory
	 */
	private File tempDir = null;
	
	/**
	 * Models directory
	 */
	private File nemoTemplatesDir = null;
	
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
		extractImporters();
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
				
				ModelFilenameFilter modelFilenameFilter = new ModelFilenameFilter(importer.getName());
				List<File> models = Arrays.asList(getNemoTemplatesDir().listFiles(modelFilenameFilter));
				if (models.size() == 0) {
					throw new ConfigException("There are no NEMO models for importer " + importer.getName());
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
		checkDir(tempDir);
	}
	
	/**
	 * Set up and check the models directory
	 * @throws ConfigException If the models directory is incorrectly configured
	 */
	private void checkNemoTemplatesDir() throws ConfigException {
		String tempDirString = getProperty(NEMO_TEMPLATES_DIR_PROPERTY);
		if (null == tempDirString) {
			throw new ConfigException(NEMO_TEMPLATES_DIR_PROPERTY + " not specified");
		}

		nemoTemplatesDir = new File(tempDirString);
		checkDir(nemoTemplatesDir);
	}
	
	/**
	 * Check a directory for existence, directoryness, and writeability
	 * @throws ConfigException If the directory has none of those things.
	 */
	private void checkDir(File directory) throws ConfigException {
		if (!directory.exists()) {
			throw new ConfigException(directory.getAbsolutePath() + " does not exist");
		} else if (!directory.isDirectory()) {
			throw new ConfigException(directory.getAbsolutePath() + " is not a directory");
		} else if (!directory.canWrite()) {
			throw new ConfigException(directory.getAbsolutePath() + " is not writeable");
		}
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
}
