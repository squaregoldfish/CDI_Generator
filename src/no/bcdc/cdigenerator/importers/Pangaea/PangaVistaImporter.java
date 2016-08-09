package no.bcdc.cdigenerator.importers.Pangaea;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import no.bcdc.cdigenerator.importers.Importer;
import no.bcdc.cdigenerator.importers.ImporterException;
import no.bcdc.cdigenerator.output.Metadata;

public abstract class PangaVistaImporter extends Importer {

	/**
	 * The URI for the PangaVista web service
	 */
	protected static final String END_POINT = "https://ws.pangaea.de/ws/services/PangaVista";
	
	/**
	 * The SOAP URI
	 */
	protected static final String OPERATION_NAME_URI = "http://soapinterop.org/";
	
	/**
	 * The command name for registering a new session
	 */
	protected static final String OPERATION_REGISTER_SESSION = "registerSession";
	
	/**
	 * The command name for retrieving a data set's metadata
	 */
	protected static final String OPERATION_METADATA = "metadata";
	
	/**
	 * The error string returned when a session has expired
	 */
	private static final String EXPIRED_SESSION_ERROR = "You must register a valid session first!";
	
	/**
	 * The SOAP service object
	 */
	private Service service = new Service();
	
	/**
	 * The current session ID
	 */
	private String sessionId = null;
	
	
	@Override
	protected Metadata getDataSetMetaData(String dataSetId) throws ImporterException {
		
		// Start a new session if required
		if (null == sessionId) {
			getNewSession();
		}
		
		String metadataXML = getMetadataXML(dataSetId);
		return generateMetadataFromXML(metadataXML);
	}
	
	@Override
	public String getDataSetIdFormat() {
		return "10.1594/PANGAEA.<number>";
	}
	
	/**
	 * Obtain a new Session ID from the PangaVista web service
	 * @throws ImporterException If an error occurs
	 */
	private void getNewSession() throws ImporterException {
		try {
			Call call = (Call) service.createCall();
	        call.setTargetEndpointAddress(new java.net.URL(PangaVistaImporter.END_POINT));
	        call.setOperationName(new QName(OPERATION_NAME_URI, OPERATION_REGISTER_SESSION));
	        sessionId = (String) call.invoke( new Object[] {} );
		} catch (Exception e) {
			throw new ImporterException("Unable to get a session ID", e);
		}
	}
	
	/**
	 * Retrieve the metadata for a given data set ID
	 * @param dataSetid The data set ID
	 * @return The metadata XML
	 * @throws ImporterException If an error occurs during the retrieval
	 */
	private String getMetadataXML(String dataSetId) throws ImporterException {
		
		String xml = null;
		
		boolean retry = true;
		
		while (retry) {
			retry = false;
			try {
				Call call = (Call) service.createCall();
				call.setTargetEndpointAddress(new java.net.URL(END_POINT));
				call.setOperationName(new QName(OPERATION_NAME_URI, OPERATION_METADATA));
		        
				call.addParameter("session", Constants.XSD_STRING, ParameterMode.IN);
				call.addParameter("URI", Constants.XSD_STRING, ParameterMode.IN);
		        
		        xml = (String) call.invoke(new Object[] { sessionId, dataSetId });
			} catch (Exception e) {

				// If the session is invalid, get a new one and try again
				if (e.getMessage().equals(EXPIRED_SESSION_ERROR)) {
					try {
						getNewSession();
						retry = true;
					} catch (ImporterException e2) {
						// If that fails, throw the resulting exception
						throw e2;
					}
				} else {
					// Otherwise we just throw the exception
					throw new ImporterException("Error while retrieving metadata", e);
				}
			}
		}
		
		return xml;
	}
	
	/**
	 * Generates a Metadata object from a PangaVista metadata XML string
	 * @param xml The PangaVista XML
	 * @return The metadata object
	 */
	private Metadata generateMetadataFromXML(String xml) {
		return new Metadata();
	}
}
