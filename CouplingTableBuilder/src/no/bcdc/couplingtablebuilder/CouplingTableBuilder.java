package no.bcdc.couplingtablebuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CouplingTableBuilder {

	/**
	 * The application configuration
	 */
	private static Config configuration = null;
	
	/**
	 * The database connection
	 */
	private static CouplingDB db = null;
	
	/**
	 * Main method
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		
		try {
			
			String configFile = null;
			
			// Check the command line arguments
			for (String arg : args) {
				if (!arg.startsWith("-D")) {
					configFile = arg;
				}
			}
			
			if (null == configFile) {
				System.out.println("Usage: java -jar CouplingTableBuilder.jar [JVM options] <config file>");
				System.exit(0);
			}
			
			// Load the configuration
			if (!loadConfig(configFile)) {
				System.exit(0);
			}
			
			db = new CouplingDB(configuration);
			
			start();
			
			db.close();
			
		} catch (Exception e) {
			System.out.println("A terrible thing has occurred, and it shouldn't have done.");
			System.out.println("Please copy and paste the stuff below, and send it to someone");
			System.out.println("who knows what they're doing along with a description of what happened");
			System.out.println("--------");
			e.printStackTrace();
		}

		
	}
	
	/**
	 * Loads and checks the configuration
	 * @param configFile The name of the configuration file
	 * @return {@code true} if the configuration file is loaded successfully. {@code false} if the configuration is invalid.
	 * @throws IOException If a system error occurs while reading the file
	 */
	private static boolean loadConfig(String configFilename) throws IOException {
		boolean ok = true;
		
		// Check that the file exists, is a file, and is readable.
		File configFile= new File(configFilename);
		if (!configFile.exists()) {
			System.out.println("Specified configuration file does not exist");
			ok = false;
		}
		
		if (ok && !configFile.isFile()) {
			System.out.println("Specified configuration file is not a file");
			ok = false;
		}
		
		if (ok && !configFile.canRead()) {
			System.out.println("Do not have permission to read specified config file");
			ok = false;
		}
		
		if (ok) {
			try {
				configuration = new Config(new FileReader(configFile));
			} catch (ConfigException e) {
				System.out.println("Error in configuration file: " + e.getMessage());
				ok = false;
			} catch (IllegalAccessException|InstantiationException e) {
				System.out.println("Error setting up configuration: " + e.getMessage());
				ok = false;
			}
		}
		
		return ok;
	}

	/**
	 * Do the stuff
	 * @throws Exception If something nasty happens
	 */
	private static void start() throws Exception {
		
		// Empty the coupling table
		db.emptyCouplingTable();
		
		// Find the list of files to process
		DataFileFilter filter = new DataFileFilter(configuration);
		
		File[] fileList = configuration.getFilesDir().listFiles(filter);
		
		for (File dataFile : fileList) {
			db.addFile(dataFile.getName());
		}
	}
	
	
}
