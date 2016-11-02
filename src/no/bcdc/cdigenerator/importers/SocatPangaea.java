package no.bcdc.cdigenerator.importers;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.Pangaea.PangaVistaImporter;

public class SocatPangaea extends PangaVistaImporter {

	/**
	 * Invoke the parent constructor.
	 */
	public SocatPangaea(Config config) {
		super(config);
	}

	@Override
	public String getName() {
		return "SOCATv3 from PANGAEA";
	}
}
