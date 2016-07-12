package no.bcdc.cdigenerator.importers;

public abstract class Importer {

	public Importer() {
		// Do nothing!
	}
	
	public void init() {
		// Default initialiser does nothing. It can be overridden if needed.
	}
	
	public abstract void doAThing();
	
}
