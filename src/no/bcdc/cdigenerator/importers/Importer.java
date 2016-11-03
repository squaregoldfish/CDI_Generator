package no.bcdc.cdigenerator.importers;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.generators.Generator;
import no.bcdc.cdigenerator.output.Metadata;

/**
 * Parent class of all importers. Lists the required methods
 * @author Steve Jones
 *
 */
public abstract class Importer {

	/**
	 * The configuration
	 */
	protected Config config;
	
	/**
	 * The filename filter for NEMO models
	 */
	private ModelFilenameFilter modelFilenameFilter = null;
	
	/**
	 * The basic importer has no constructor activities
	 */
	public Importer(Config config) {
		this.config = config;
		modelFilenameFilter = new ModelFilenameFilter(getName());
	}
	
	/**
	 * Initialise the importer. This usually doesn't need to do anything,
	 * so an empty version is provided here.
	 */
	public void init() {
	}
	
	/**
	 * Begin the importing process
	 * @param generator The generator, so we can send/receive information to/from it.
	 */
	public boolean retrieveData(String dataSetId, Generator generator) {
		
		boolean success = true;
		
		try {
			File dataFile = new File(config.getTempDir(), dataSetId + "_data");
			File metadataFile = new File(config.getTempDir(), dataSetId + "_metadata");
			
			// Retrieve the data
			if (dataFile.exists()) {
				generator.setProgressMessage("Data is already in cache");
			} else {
				generator.setProgressMessage("Retrieving data...");
				String data = getDataSetData(dataSetId);
			
				PrintWriter dataOut = new PrintWriter(dataFile);
				dataOut.print(data);
				dataOut.close();
			}
			
			// Retrieve the metadata
			if (metadataFile.exists()) {
				generator.setProgressMessage("Metadata is already in cache");
			} else {
				generator.setProgressMessage("Retrieving metadata...");
				Metadata metadata = getDataSetMetaData(dataSetId);
				PrintWriter metadataOut = new PrintWriter(metadataFile);
				metadataOut.print(metadata);
				metadataOut.close();
			}
			
		} catch (DataSetNotFoundException e) {
			generator.setProgressMessage(e.getMessage());
			generator.logMessage(dataSetId, "Data set not found");
			success = false;
		} catch (Exception e) {
			generator.setProgressMessage(e.getMessage());
			generator.logMessage(dataSetId, "Error retrieving and storing data");
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Get the list of NEMO model files for this importer
	 * @return The list of NEMO model files
	 */
	public List<File> getNemoModelList() {
		return Arrays.asList(config.getNemoTemplatesDir().listFiles(modelFilenameFilter));
	}
	
	/**
	 * Returns the descriptive name of the data set IDs, e.g. "DOIs"
	 * @return The descriptive name of the data set IDs
	 */
	public abstract String getDataSetIdsDescriptor();
	
	/**
	 * Returns the descriptive name of a single data set ID, e.g. "DOI"
	 * @return The descriptive name of the data set ID
	 */
	public abstract String getDataSetIdDescriptor();
	
	/**
	 * Returns the format of the data set IDs for this importer, to be displayed to the user
	 * @return The format of the data set IDs
	 */
	public abstract String getDataSetIdFormat();
	
	/**
	 * Validate an ID's format to make sure it looks right.
	 * Note that this doesn't necessarily mean that it *is* right...
	 * 
	 * @param id The id
	 * @return {@code true} if the ID appears to be valid; {@code false} if it does not.
	 */
	public abstract boolean validateIdFormat(String id);
	
	/**
	 * Retrieve the data for the specified data set ID
	 * @param dataSetId The data set ID
	 * @return The data
	 */
	protected abstract String getDataSetData(String dataSetId) throws ImporterException, DataSetNotFoundException;
	
	/**
	 * Retrieve the metadata for the specified data set ID
	 * @param dataSetId The data set ID
	 * @return The metadata
	 */
 	protected abstract Metadata getDataSetMetaData(String dataSetId) throws ImporterException, DataSetNotFoundException;
 	
 	/**
 	 * Returns the name of this importer
 	 * @return The name of the importer
 	 */
 	public abstract String getName();
}
