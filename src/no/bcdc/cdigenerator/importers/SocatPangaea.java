package no.bcdc.cdigenerator.importers;

public class SocatPangaea extends Importer {

	/**
	 * The constructor does nothing.
	 */
	public SocatPangaea() {
		super();
	}

	@Override
	protected String getDataSetIdsDescriptor() {
		return "DOIs";
	}
}
