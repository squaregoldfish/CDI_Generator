package no.bcdc.cdigenerator.importers;

import no.bcdc.cdigenerator.importers.Pangaea.PangaVistaImporter;

public class SocatPangaea extends PangaVistaImporter {

	/**
	 * The constructor does nothing.
	 */
	public SocatPangaea() {
		super();
	}

	@Override
	public String getName() {
		return "SOCATv3 from PANGAEA";
	}
}
