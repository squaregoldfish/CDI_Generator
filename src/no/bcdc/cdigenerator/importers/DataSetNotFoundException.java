package no.bcdc.cdigenerator.importers;

public class DataSetNotFoundException extends Exception {
	
	public DataSetNotFoundException(String dataSetId) {
		super("The data set '" + dataSetId + "' could not be found");
	}
	
}
