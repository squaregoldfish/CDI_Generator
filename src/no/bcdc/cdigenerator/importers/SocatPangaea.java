package no.bcdc.cdigenerator.importers;

public class SocatPangaea extends Importer {

	/**
	 * The constructor does nothing.
	 */
	public SocatPangaea() {
		super();
	}

	@Override
	public String getDataSetIdsDescriptor() {
		return "DOIs";
	}
	
	@Override
	public String getDataSetIdDescriptor() {
		return "DOI";
	}
	
	@Override
	public boolean validateIdFormat(String id) {
		boolean result = false;
		
		if (null != id) {
			result= id.matches("10\\.1594/PANGAEA\\.[0-9]+");
		}
		
		return result;
	}
	
	@Override
	public String getDataSetIdFormat() {
		return "10.1594/PANGAEA.<number>";
	}
}
