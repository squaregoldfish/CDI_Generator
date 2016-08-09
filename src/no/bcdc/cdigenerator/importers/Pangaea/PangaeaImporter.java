package no.bcdc.cdigenerator.importers.Pangaea;

import no.bcdc.cdigenerator.importers.Importer;
import no.bcdc.cdigenerator.output.Metadata;

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
