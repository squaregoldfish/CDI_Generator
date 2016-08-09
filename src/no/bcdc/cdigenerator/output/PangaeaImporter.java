package no.bcdc.cdigenerator.output;

import no.bcdc.cdigenerator.importers.Importer;

public abstract class PangaeaImporter extends Importer {

	private String sessionID = null;
	
	@Override
	protected Metadata getDataSetMetaData(String dataSetId) {
		return new Metadata();
	}
	
	@Override
	public String getDataSetIdFormat() {
		return "10.1594/PANGAEA.<number>";
	}
	
	
}
