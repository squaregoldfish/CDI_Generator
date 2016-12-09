package no.bcdc.cdigenerator.generators;

public class ExternalProcessFailedException extends Exception {

	private static final long serialVersionUID = 1527100984304942600L;

	/**
	 * The name of the external process
	 */
	private String processName;
	
	/**
	 * Basic constructor
	 * @param processName The process name
	 */
	public ExternalProcessFailedException(String processName) {
		super();
		this.processName = processName;
	}
	
	/**
	 * Constructor with a cause
	 * @param processName The process name
	 * @param cause The cause of the failure
	 */
	public ExternalProcessFailedException(String processName, Throwable cause) {
		super(cause);
	}
	
	@Override
	public String getMessage() {
		return "The external process '" + processName + "' failed";
		
	}
}
