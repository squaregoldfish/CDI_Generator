package no.bcdc.cdigenerator.importers;

public class DataSetNotFoundException extends Exception {
	
	private static final long serialVersionUID = -1840094203995438055L;

	public DataSetNotFoundException(String dataSetId) {
		super("The data set '" + dataSetId + "' could not be found");
	}
	
}
