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
	 * The NEMO model file
	 */
	private File modelFile;
	
	/**
	 * The output format for this model
	 */
	private String outputFormat;
	
	/**
	 * Construct the NEMO model information
	 * @param config The application configuration
	 * @param modelIdentifier The model indentifier
	 * @param outputFormat The output format of the model
	 * @throws NemoModelException If the model file is not valid
	 */
	public NemoModel(Config config, String modelIdentifier, String outputFormat) throws NemoModelException {
		this.config = config;
		this.outputFormat = outputFormat;
		this.modelFile = new File(config.getNemoTemplatesDir(), getModelFilename(modelIdentifier));
		validateModelFile();
	}
	
	/**
	 * Construct the model template's filename
	 * @param identifer The model identifier
	 * @return The model template's filename
	 */
	private String getModelFilename(String identifer) {
		return identifer + '_' + outputFormat + ".xml";
	}
	
	/**
	 * Validate the model template file
	 * @throws NemoModelException If the file is not valid
	 */
	private void validateModelFile() throws NemoModelException {
		if (!modelFile.exists()) {
			throw new NemoModelException(modelFile, "Does not exist");
		}
		if (!modelFile.isFile()) {
			throw new NemoModelException(modelFile, "Is not a file");
		}
		if (!modelFile.canRead()) {
			throw new NemoModelException(modelFile, "Cannot be accessed");
		}
	}

	/**
	 * Returns the File object representing the NEMO model template
	 * @return The model file
	 */
	public File getModelFile() {
		return modelFile;
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
		return new File(config.getTempDir(), dataSetId + '_' + outputFormat + "_nemoModel.xml");
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
