package no.bcdc.cdigenerator;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import no.bcdc.cdigenerator.importers.Importer;

/**
 * Configuration for the CDI Generator
 * @author Steve Jones
 *
 */
public class Config extends Properties {
	
	/**
	 * Key prefix for importers
	 */
	private static final String IMPORTER_PREFIX = "importer.";
	
	/**
	 * The default package for importer classes. Used if the package isn't set in the config file.
	 */
	private static final String DEFAULT_IMPORTER_PACKAGE = "no.bcdc.cdigenerator.importers";
	
	/**
	 * Lookup table of importers
	 */
	private Map<String, Class<? extends Importer>> importers = null;
	
	/**
	 * Initialise and load the configuration
	 * @param configReader A reader for the config file
	 * @throws IOException If an error occurs while reading the file data
	 * @throws ConfigException If there are errors in the configuration
	 */
	public Config(Reader configReader) throws IOException, ConfigException {
		super();
		load(configReader);
		extractImporters();
	}
	
	/**
	 * Checks that the configuration is valid.
	 * @throws ConfigException If there are any errors in the configuration
	 */
	@SuppressWarnings("unchecked")
	private void extractImporters() throws ConfigException {
		
		importers = new HashMap<String, Class<? extends Importer>>();
		
		// Loop through all the keys, looking for those with the right prefix
		for (String key : stringPropertyNames()) {
			if (key.startsWith(IMPORTER_PREFIX)) {
				
				String importerName = key.substring(IMPORTER_PREFIX.length());
				String className = getProperty(key);
				
				// Add the default prefix if required
				if (className.indexOf('.') == -1) {
					className = DEFAULT_IMPORTER_PACKAGE + "." + className;
				}
				
				// Check the class exists and extends the correct class
				try {
					Class<?> clazz = Class.forName(className);
					if (!Importer.class.isAssignableFrom(clazz)) {
						throw new ConfigException("Importer '" + importerName + "': Importer class '" + className + "' is not a subclass of no.bcdc.cdigenerator.Importer");
					}
					
					importers.put(importerName, (Class<? extends Importer>) clazz);
					
					
				} catch (ClassNotFoundException e) {
					throw new ConfigException("Importer '" + importerName + "': Importer class '" + className + "' does not exist");
				}
			}
		}
		
		if (importers.size() == 0) {
			throw new ConfigException("No importers specified in config file");
		}
	}
	
	/**
	 * Returns a sorted list of all the importer names
	 * @return The list of importer names
	 */
	public List<String> getImporterNames() {
		List<String> importerNames = new ArrayList<String>(importers.size());
		importerNames.addAll(importers.keySet());
		Collections.sort(importerNames);
		return importerNames;
	}
	
	/**
	 * Retrieve the importer identified by the specified name
	 * @param importerName The name of the importer
	 * @return The Importer object
	 * @throws ConfigException If the importer cannot be found
	 */
	public Importer getImporter(String importerName) throws ConfigException {
		
		Importer importer = null;
		
		Class<? extends Importer> importerClass = importers.get(importerName);
		if (null == importerClass) {
			throw new ConfigException("Cannot find importer with name '" + importerName + "'");
		}
		
		try {
			importer = importerClass.newInstance();
			importer.init(this);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ConfigException("Error while retrieving constructing importer: " + e.getMessage(), e);
		}
		
		return importer;
		
	}
}
