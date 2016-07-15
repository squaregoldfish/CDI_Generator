package no.bcdc.cdigenerator;

import java.util.List;
import java.util.Scanner;

public class CommandLineGenerator {
	
	private static final String QUIT_OPTION = "q";
	 
	private Config config;
	
	private Scanner inputScanner;
	
	protected CommandLineGenerator(Config config) {
		this.config = config;
		inputScanner = new Scanner(System.in);
	}
	
	protected void start() throws Exception {
		
		boolean quit = false;
		
		while (!quit) {
			String chosenImporter = getImporterChoice();
			if (null == chosenImporter) {
				quit = true;
			} else {
				System.out.println(chosenImporter);
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
}
