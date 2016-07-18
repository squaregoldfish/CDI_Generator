package no.bcdc.cdigenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import no.bcdc.cdigenerator.importers.Importer;

/**
 * CDI Generator that runs on the command line
 * @author Steve Jones
 *
 */
public class CommandLineGenerator extends Generator {
	
	/**
	 * String indicating that the user wants to quit
	 */
	private static final String QUIT_OPTION = "q";
	
	/**
	 * Indicates that the user wants to enter a dataset ID by hand
	 */
	private static final int SOURCE_OPTION_TYPED = 1;
	
	/**
	 * Indicates that the user wants to supply a file full of dataset IDs
	 */
	private static final int SOURCE_OPTION_FILE = 2;
	
	/**
	 * Integer value for the option indicating that the user wants to go back a step
	 */
	private static final int OPTION_BACK = -1;
	
	/**
	 * The option that the user can type in to go back a step
	 */
	private static final String OPTION_BACK_STRING = "b";
	 
	/**
	 * Command line processor
	 */
	private Scanner inputScanner;
	
	/**
	 * Initialises the configuration and command line scanner
	 * @param config The configuration
	 */
	protected CommandLineGenerator(Config config) {
		super(config);
		inputScanner = new Scanner(System.in);
	}
	
	@Override
	protected void start() throws Exception {
		
		boolean quit = false;
		
		// Main loop asks for an importer, then runs it.
		// We do that until the user decides to quit.
		while (!quit) {
			String chosenImporter = getImporterChoice();
			if (null == chosenImporter) {
				quit = true;
			} else {
				Importer importer= config.getImporter(chosenImporter);
				importer.start(this);
			}
		}
	}
	
	/**
	 * Ask the user to select an importer to use.
	 * If the user says 'q' this will return {@code null},
	 * indicating that the program should quit.
	 * @return The name of the importer to use, or {@code null} if the program should quit.
	 */
	private String getImporterChoice() {
		
		// Show a list of importers
		List<String> importerNames = config.getImporterNames();
		
		boolean inputOK = false;
		String result = null;
		
		while (!inputOK) {
			
			System.out.println("\nSelect the type of data you want to process:");
			int importerCount = 0;
			for (String name : importerNames) {
				importerCount++;
				System.out.println(importerCount + ". " + name);
			}
			
			System.out.print("Enter 1-" + importerNames.size() + " or q to quit : ");
		
			String userInput = inputScanner.next();
			
			if (userInput.equalsIgnoreCase(QUIT_OPTION)) {
				inputOK = true;
			} else {
				try {
					int choice = Integer.parseInt(userInput);
					if (choice >= 1 && choice <= importerNames.size()) {
						result = importerNames.get(choice - 1);
						inputOK = true;
					}
				} catch (NumberFormatException e) {
					// Do nothing - we'll go round the loop again.
				}
			}
		}
		
		return result;
	}
	
	@Override
	public List<String> getDataSetIds(String dataSetIdsDescriptor) {
		
		List<String> ids = new ArrayList<String>();
		
		int sourceOption = getIdSourceOption(dataSetIdsDescriptor);
		
		switch (sourceOption) {
		case OPTION_BACK: {
			ids = null;
			break;
		}
		default: {
			System.out.println("Next up: Do whatever the choice required");
		}
		}
		
		
		return ids;
		
	}
	
	/**
	 * Get the user to say whether they want to input a dataset ID or a file full of them
	 * @param dataSetIdsDescriptor The descriptive name for the data set IDs (e.g. "DOIs")
	 * @return The user's choice
	 */
	private int getIdSourceOption(String dataSetIdsDescriptor) {
		
		boolean inputOK = false;
		int result = 0;

		while (!inputOK) {
			System.out.println("\n\nHow do you want to supply the " + dataSetIdsDescriptor + "?");
			System.out.println("1. Type one in");
			System.out.println("2. Name a file containing the " + dataSetIdsDescriptor);
			System.out.println("B. Go back");
			
			System.out.print("Make your choice: ");
			
			String userInput = inputScanner.next();
			
			if (userInput.equalsIgnoreCase(OPTION_BACK_STRING)) {
				result = OPTION_BACK;
				inputOK = true;
			} else {
				try {
					int choice = Integer.parseInt(userInput);
					if (choice == SOURCE_OPTION_TYPED || choice == SOURCE_OPTION_FILE) {
						result = choice;
						inputOK = true;
					}
				} catch (NumberFormatException e) {
					// Do nothing - we'll go round the loop again.
				}
			}
		}
		
		return result;
	}
}
