package no.bcdc.cdigenerator.importers;

public class UnrecognisedNemoTagException extends NemoTemplateException {

	private static final long serialVersionUID = -8201941208511275620L;

	public UnrecognisedNemoTagException(String tag) {
		super("Unrecognised or empty NEMO template tag '" + tag + "'");
	}
	
}
