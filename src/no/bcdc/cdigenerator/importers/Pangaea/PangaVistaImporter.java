package no.bcdc.cdigenerator.importers.Pangaea;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
	 * XPath for the DOI
	 */
	private static final String XPATH_DOI = "/MetaData/citation/URI";
	
	/**
	 * XPath for the abstract
	 */
	private static final String XPATH_ABSTRACT = "/MetaData/citation/title";
	
	/**
	 * XPath for west longitude
	 */
	private static final String XPATH_WEST_LONGITUDE = "/MetaData/extent/geographic/westBoundLongitude";
	
	/**
	 * XPath for east longitude
	 */
	private static final String XPATH_EAST_LONGITUDE = "/MetaData/extent/geographic/eastBoundLongitude";
	
	/**
	 * XPath for south latitude
	 */
	private static final String XPATH_SOUTH_LATITUDE = "/MetaData/extent/geographic/southBoundLatitude";
	
	/**
	 * XPath for north latitude
	 */
	private static final String XPATH_NORTH_LATITUDE = "/MetaData/extent/geographic/northBoundLatitude";
	
	/**
	 * XPath for the start time
	 */
	private static final String XPATH_START_TIME = "/MetaData/extent/temporal/minDateTime";
	
	/**
	 * XPath for the end time
	 */
	private static final String XPATH_END_TIME = "/MetaData/extent/temporal/maxDateTime";
	
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
	protected String getDataSetMetadata(String dataSetId) throws ImporterException, DataSetNotFoundException {
		
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
		
		// Have a short sleep - trying to access the session too quickly causes problems
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Meh
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
		
		int retriesLeft = config.getNetworkRetries();
		
		while (null == xml && retriesLeft > 0) {
			
			boolean sessionOK = false;
			
			while (!sessionOK) {
				
				// Start by assuming we have a valid session
				sessionOK = true;
				
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
							sessionOK = false;
						} catch (ImporterException e2) {
							// If that fails, throw the resulting exception
							throw e2;
						}
					} else if (e.getMessage().startsWith(DATASET_NOT_FOUND_ERROR)) {
						throw new DataSetNotFoundException(dataSetId);
					} else {
						// Otherwise we log the error and retry
						getLogger().warning("Metadata retrieval attempt failed\n");
						getLogger().throwing(this.getClass().getName(), "getMetadataXML", e);
					}
				}
			}
			
			if (null == xml) {
				retriesLeft--;
				
				int waitCount = config.getRetryWaitTime();
				while (waitCount > 0) {
					generator.setProgressMessage("Metadata retrieval failed. Retrying in " + waitCount + " seconds (" + retriesLeft + " attempts remaining)");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// We don't care
					}
					waitCount--;
				}
			}
		}
		
		return xml;
	}
	
	@Override
	protected String getDataSetData(String dataSetId) throws ImporterException, DataSetNotFoundException {
		String result = null;
		
		int retriesLeft = config.getNetworkRetries();
		
		while (null == result && retriesLeft > 0) {
		
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
				getLogger().warning("Data retrieval attempt failed\n");
				getLogger().throwing(this.getClass().getName(), "getDataSetData", e);
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
			
			if (null == result) {
				retriesLeft--;
				
				int waitCount = config.getRetryWaitTime();
				while (waitCount > 0) {
					generator.setProgressMessage("Data retrieval failed. Retrying in " + waitCount + " seconds (" + retriesLeft + " attempts remaining)");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// We don't care
					}
					waitCount--;
				}
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
	
	/**
	 * Evaluate an XPath in the metadata, and convert it to a double value
	 * @param xPath The XPath
	 * @return The matching value
	 * @throws ImporterException If the XPath fails or the value is not numeric
	 */
	protected double evaluateXPathDouble(String xPath) throws ImporterException {
		try {
			return Double.parseDouble(evaluateXPath(xPath));
		} catch (NumberFormatException e) {
			throw new ImporterException("Metadata value is not numeric");
		}
	}
	
	@Override
	protected String getTemplateTagValue(String tag) throws NemoTemplateException {
		String tagValue = null;
		
		switch (tag) {
		case "SHIP_NAME": {
			tagValue = evaluateXPath(XPATH_SHIP_NAME);
			break;
		}
		case "FIRST_AUTHOR": {
			tagValue = getFirstAuthor();
			break;
		}
		case "START_DATE_MS": {
			tagValue = String.valueOf(getStartDateMilliseconds());
			break;
		}
		case "END_DATE_MS": {
			tagValue = String.valueOf(getEndDateMilliseconds());
			break;
		}
		}
		
		return tagValue;
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
	
	@Override
	public String getDoi() throws ImporterException {
		String xPathValue = evaluateXPath(XPATH_DOI);
		if (xPathValue.startsWith("doi:")) {
			xPathValue = xPathValue.substring(4);
		}
		
		return xPathValue;
	}
	
	@Override
	public String getDoiUrl() throws ImporterException {
		return "https://doi.pangaea.de/" + getDoi();
	}
	
	@Override
	public String getAbstract() throws ImporterException {
		return evaluateXPath(XPATH_ABSTRACT);
	}
	
	@Override
	public double getWestLongitude() throws ImporterException {
		return evaluateXPathDouble(XPATH_WEST_LONGITUDE);
	}
	
	@Override
	public double getEastLongitude() throws ImporterException {
		return evaluateXPathDouble(XPATH_EAST_LONGITUDE);
	}
	
	@Override
	public double getSouthLatitude() throws ImporterException {
		return evaluateXPathDouble(XPATH_SOUTH_LATITUDE);
	}
	
	@Override
	public double getNorthLatitude() throws ImporterException {
		return evaluateXPathDouble(XPATH_NORTH_LATITUDE);
	}

	@Override
	public Date getStartDate() throws ImporterException {
		Instant dateTime = Instant.ofEpochMilli(getStartDateMilliseconds());
		Instant dateOnly = dateTime.truncatedTo(ChronoUnit.DAYS);
		return new Date(dateOnly.toEpochMilli());
	}
	
	@Override
	public Date getStartDateTime() throws ImporterException {
		return new Date(getStartDateMilliseconds());
	}
	
	@Override
	public Date getEndDateTime() throws ImporterException {
		return new Date(getEndDateMilliseconds());
	}

	/**
	 * Get the time of the first data record in milliseconds since the epoch
	 * @return The time of the first data record in milliseconds since the epoch
	 */
	private long getStartDateMilliseconds() throws NemoTemplateException {
		return timeToMilliseconds(evaluateXPath(XPATH_START_TIME));
	}
	
	/**
	 * Get the time of the last data record in milliseconds since the epoch
	 * @return The time of the last data record in milliseconds since the epoch
	 */
	private long getEndDateMilliseconds() throws NemoTemplateException {
		return timeToMilliseconds(evaluateXPath(XPATH_END_TIME));
	}

	/**
	 * Take a time string from the SOCAT file and convert it to milliseconds since the epoch.
	 * The times in these files are of the form "YYYY-MM-DDThh:mm", so we add the seconds and timezone (UTC).
	 * @param timeString The time string from the file
	 * @return The time string as milliseconds since the epoch.
	 */
	private long timeToMilliseconds(String timeString) {
		String isoTimeString = timeString + "+00:00";
		ZonedDateTime parsedTime = ZonedDateTime.parse(isoTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		return parsedTime.toInstant().toEpochMilli();
	}
}
