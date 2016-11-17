package no.bcdc.cdigenerator.importers.Pangaea;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ParameterMode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.DataSetNotFoundException;
import no.bcdc.cdigenerator.importers.Importer;
import no.bcdc.cdigenerator.importers.ImporterException;
import no.bcdc.cdigenerator.importers.NemoTemplateException;

public abstract class PangaVistaImporter extends Importer {

	/**
	 * The URI for the PangaVista web service
	 */
	protected static final String END_POINT = "https://ws.pangaea.de/ws/services/PangaVista";
	
	/**
	 * XPath for ship name
	 */
	private static final String XPATH_SHIP_NAME = "/MetaData/event/basis";
	
	/**
	 * XPath for the first author's last name
	 */
	private static final String XPATH_AUTHOR_LAST_NAME = "/Metadata/citation/author/lastName";
	
	/**
	 * XPath for the first author's first name
	 */
	private static final String XPATH_AUTHOR_FIRST_NAME = "/Metadata/citation/author/firstName";
	
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
	 * The error string returned when the data set cannot be found
	 */
	private static final String DATASET_NOT_FOUND_ERROR = "This is not a valid PANGAEA DOI or DATASETID";
	
	/**
	 * The SOAP service object
	 */
	private Service service = new Service();
	
	/**
	 * The current session ID
	 */
	private String sessionId = null;
	
	/**
	 * The parsed metadata XML
	 */
	protected Document metadataXML = null;
	
	/**
	 * XPath resolver for metadata files
	 */
	protected XPath xPathResolver = null;
	
	/**
	 * Default constructor - invokes the parent constructor
	 * @param config The configuration
	 */
	public PangaVistaImporter(Config config) {
		super(config);
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		xPathResolver = xPathFactory.newXPath();
		xPathResolver.setNamespaceContext(new PangaeaMetadataNamespaceContext());
	}

	@Override
	protected String getDataSetMetaData(String dataSetId) throws ImporterException, DataSetNotFoundException {
		
		// Start a new session if required
		if (null == sessionId) {
			getNewSession();
		}
		
		return getMetadataXML(dataSetId);
	}
	
	@Override
	public String getDataSetIdFormat() {
		return "<number>";
	}
	
	@Override
	public boolean validateIdFormat(String id) {
		boolean result = false;
		
		if (null != id) {
			result= id.matches("[0-9]+");
		}
		
		return result;
	}
	
	@Override
	public String getDataSetIdsDescriptor() {
		return "PANGAEA IDs";
	}
	
	@Override
	public String getDataSetIdDescriptor() {
		return "PANGAEA ID";
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
	 * @throws DataSetNotFoundException 
	 */
	private String getMetadataXML(String dataSetId) throws ImporterException, DataSetNotFoundException {
		
		String xml = null;
		
		boolean retry = true;
		
		while (retry) {
			retry = false;
			try {
				Call call = (Call) service.createCall();
				call.setTargetEndpointAddress(new java.net.URL(END_POINT));
				call.setOperationName(new QName(OPERATION_NAME_URI, OPERATION_METADATA));
				call.setReturnType(org.apache.axis.Constants.XSD_STRING);
				
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
				} else if (e.getMessage().startsWith(DATASET_NOT_FOUND_ERROR)) {
					throw new DataSetNotFoundException(dataSetId);
				} else {
					// Otherwise we just throw the exception
					throw new ImporterException("Error while retrieving metadata", e);
				}
			}
		}
		
		return xml;
	}
	
	@Override
	protected String getDataSetData(String dataSetId) throws ImporterException, DataSetNotFoundException {
		String result = null;
		
		HttpsURLConnection conn = null;
		InputStream stream = null;
		StringWriter writer = null;
		
		try {
			URL url = makeUrl(dataSetId);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			
			stream = conn.getInputStream();
			writer = new StringWriter();
			IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
			result = writer.toString();
		} catch (FileNotFoundException e) {
			throw new DataSetNotFoundException(dataSetId);
		} catch (Exception e) {
			throw new ImporterException("Error while retrieving data set", e);
		} finally {
			try {
				if (null != writer) {
					writer.close();
				}
				
				if (null != stream) {
					stream.close();
				}
				
				if (null != conn) {
					conn.disconnect();
				}
			} catch (IOException e) {
				// Do nothing - we can say that we tried.
			}
		}
		
		return result;
	}
	
	/**
	 * Make a URL for a data set
	 * @param dataSetId The data set ID
	 * @return The data set URL
	 * @throws MalformedURLException If the generated URL is invalid
	 */
	private URL makeUrl(String dataSetId) throws MalformedURLException {
		StringBuilder url = new StringBuilder("https://doi.pangaea.de/10.1594/PANGAEA.");
		url.append(dataSetId);
		url.append("?format=textfile");
		
		return new URL(url.toString());
	}
	
	@Override
	protected void preprocessMetadata() throws ImporterException {
		// Create the XML document
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			metadataXML = builder.parse(IOUtils.toInputStream(metadata, StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new ImporterException("Error while parsing metadata XML", e);
		}
	}

	/**
	 * Evaluate an XPath in the metadata
	 * @param xPath The XPath to evaluate
	 * @return The matching string
	 * @throws Exception If the XPath fails
	 */
	protected String evaluateXPath(String xPath) throws NemoTemplateException {
		String result = null;
		
		try {
			result = xPathResolver.evaluate(xPath, metadataXML).trim();
		} catch (XPathExpressionException e) {
			throw new NemoTemplateException("Error extracting XPath from metadata", e);
		}
		
		return result;
	}
	
	@Override
	protected String getTemplateTagValue(String tag) throws NemoTemplateException {
		String tagValue = null;
		
		switch (tag) {
		case "SHIP_NAME": {
			tagValue = getShipName();
			break;
		}
		case "FIRST_AUTHOR": {
			tagValue = getFirstAuthor();
			break;
		}
		}
		
		return tagValue;
	}
	
	/**
	 * Get the ship name
	 * @return The name of the ship
	 * @throws NemoTemplateException If the XPath lookup fails
	 */
	private String getShipName() throws NemoTemplateException {
		return evaluateXPath(XPATH_SHIP_NAME); 
	}
	
	/**
	 * Get the first author of this data set, in the form <Last Name>, <First Name>
	 * The author's last name and first name(s) are stored in two elements of the XML
	 * @return The first author's name
	 * @throws NemoTemplateException If the XPath lookups fail
	 */
	private String getFirstAuthor() throws NemoTemplateException {
		StringBuilder output = new StringBuilder();
		
		output.append(evaluateXPath(XPATH_AUTHOR_LAST_NAME));
		output.append(", ");
		output.append(evaluateXPath(XPATH_AUTHOR_FIRST_NAME));
		
		return output.toString();
	}
}
