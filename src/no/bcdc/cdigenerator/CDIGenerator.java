package no.bcdc.cdigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import no.bcdc.cdigenerator.generators.CommandLineGenerator;

public class CDIGenerator {

	/**
	 * The program configuration
	 */
	private static Config configuration = null;
	
	/**
	 * Start method
	 * @param args The command line arguments. One argument required: the name of the configuration file
	 */
	public static void main(String[] args) {

		// We may put a graphical/command line mode switch in here.
		// For now it's command line all the way.
	
		try {
			
			String configFile = null;
			
			// Check the command line arguments
			for (String arg : args) {
				if (!arg.startsWith("-D")) {
					configFile = arg;
				}
			}
			
			if (null == configFile) {
				System.out.println("Usage: java -jar CDIGenerator.jar [JVM options] config file");
				System.exit(0);
			}
			
			// Load the configuration
			if (!loadConfig(configFile)) {
				System.exit(0);
			}
		

			// Initialise the command-line mode generator
			CommandLineGenerator generator = new CommandLineGenerator(configuration);
			generator.start();
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
}
