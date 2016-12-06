package no.bcdc.cdigenerator.importers;

import java.io.File;

import no.bcdc.cdigenerator.Config;

/**
 * A class to represent details of a NEMO model template and its output
 * @author Steve Jones
 *
 */
public class NemoModel {

	/**
	 * The application configuration
	 */
	private Config config;

	/**
	 * The name of the current importer
	 */
	private String importerName;
	
	/**
	 * The identifier for the current NEMO model
	 */
	private String modelIdentifier;
	
	/**
	 * The File object for the model template file
	 */
	private File modelTemplateFile;
	
	/**
	 * The output format for this model
	 */
	private String outputFormat;
	
	/**
	 * Construct the NEMO model information
	 * @param config The application configuration
	 * @param modelIdentifier The model identifier
	 * @param outputFormat The output format of the model
	 * @throws NemoModelException If the model file is not valid
	 */
	public NemoModel(Config config, String importerName, String modelIdentifier, String outputFormat) throws NemoModelException {
		this.config = config;
		this.importerName = importerName;
		this.modelIdentifier = modelIdentifier;
		this.outputFormat = outputFormat;
		
		buildModelTemplateFile();
	}
	
	/**
	 * Construct the File object for the NEMO model template
	 * @param templatesDir The application's NEMO templates directory
	 * @param importerName The name of the current importer
	 * @param identifer The template identifier
	 * @return The NEMO model template file
	 * @throws NemoModelException If there is a problem with the model template file 
	 */
	private void buildModelTemplateFile() throws NemoModelException {
		File importerTemplatesDir = new File(config.getNemoTemplatesDir(), importerName);
		String templateFilename = modelIdentifier + '_' + outputFormat + ".xml";
		modelTemplateFile = new File(importerTemplatesDir, templateFilename);
	
		if (!modelTemplateFile.exists()) {
			throw new NemoModelException(modelTemplateFile, "Does not exist");
		}
		if (!modelTemplateFile.isFile()) {
			throw new NemoModelException(modelTemplateFile, "Is not a file");
		}
		if (!modelTemplateFile.canRead()) {
			throw new NemoModelException(modelTemplateFile, "Cannot be accessed");
		}
	}
	
	/**
	 * Returns the File object representing the NEMO model template
	 * @return The model file
	 */
	public File getModelTemplateFile() {
		return modelTemplateFile;
	}
	
	/**
	 * Get the output format of this model template
	 * @return The output format
	 */
	public String getOutputFormat() {
		return outputFormat;
	}
	
	/**
	 * Get the location where the populated model template is to be stored
	 * @param dataSetId The data set ID
	 * @return The location of the populated model template file
	 */
	public File getPopulatedTemplateFile(String dataSetId) {
		
		return new File(config.getTempDir(), dataSetId + '_' + modelIdentifier + '_' + outputFormat + "_nemoModel.xml");
	}

	/**
	 * Get the name of the NEMO output file
	 * @return The filename
	 */
	public File getOutputFile(String localCdiId) throws ImporterException {
		StringBuilder filename = new StringBuilder();
		
		filename.append(localCdiId);
		filename.append('_');
		filename.append(outputFormat.toLowerCase());
		filename.append(".txt");
		
		return new File(config.getNemoOutputDir(), filename.toString());
	}
	
	/**
	 * Get the name of the NEMO summary file
	 * @return The filename
	 */
	public File getSummaryFile(String localCdiId) throws ImporterException {
		StringBuilder filename = new StringBuilder();
		
		filename.append(localCdiId);
		filename.append('_');
		filename.append(outputFormat.toLowerCase());
		filename.append("_summary.txt");
		
		return new File(config.getNemoOutputDir(), filename.toString());
	}
	
}
