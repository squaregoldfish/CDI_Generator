package no.bcdc.cdigenerator.importers.concrete;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.ColumnPaddingSpec;
import no.bcdc.cdigenerator.importers.ImporterException;
import no.bcdc.cdigenerator.importers.NemoTemplateException;
import no.bcdc.cdigenerator.importers.PaddingException;
import no.bcdc.cdigenerator.importers.Pangaea.PangaVistaImporter;

public class SocatV3Pangaea extends PangaVistaImporter {

	/**
	 * The XPath for the EXPO Code
	 */
	private static final String XPATH_EXPOCODE = "/MetaData/event/label";
	
	/**
	 * The XPath for the sensor depth
	 */
	private static final String XPATH_SENSOR_DEPTH = "/MetaData/extent/elevation/min";
	
	/**
	 * The text used to identify the start of the column header line
	 */
	private static final String DATA_HEADER_START = "Date/Time";
	
	/**
	 * Position of the start of the latitude field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String LATITUDE_FIRST_CHAR = "19";
	
	/**
	 * Position of the end of the latitude field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String LATITUDE_LAST_CHAR = "27";
	
	/**
	 * Position of the start of the longitude field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String LONGITUDE_FIRST_CHAR = "29";
	
	/**
	 * Position of the end of the longitude field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String LONGITUDE_LAST_CHAR = "38";
	
	/**
	 * Position of the start of the salinity field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SALINITY_FIRST_CHAR = "44";
	
	/**
	 * Position of the end of the salinity field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SALINITY_LAST_CHAR = "50";
	
	/**
	 * Position of the start of the SST field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SST_FIRST_CHAR = "52";
	
	/**
	 * Position of the end of the SST field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SST_LAST_CHAR = "58";
	
	/**
	 * Position of the start of the Atmospheric Pressure field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String PRESSURE_FIRST_CHAR = "68";
	
	/**
	 * Position of the end of the Atmospheric Pressure field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String PRESSURE_LAST_CHAR = "76";
	
	/**
	 * Position of the start of the Bathymetry field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String BATHYMETRY_FIRST_CHAR = "96";
	
	/**
	 * Position of the end of the Bathymetry field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String BATHYMETRY_LAST_CHAR = "100";
	
	/**
	 * Position of the start of the fCO2 field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String FCO2_FIRST_CHAR = "126";
	
	/**
	 * Position of the end of the fCO2 field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String FCO2_LAST_CHAR = "133";
	
	/**
	 * The line containing the first data record
	 */
	private int firstLineNumber = -1;
	
	/**
	 * The first data line
	 */
	private String firstLine = null;
	
	/**
	 * The last data line
	 */
	private String lastLine = null;
	
	/**
	 * The list of column padding specs for this importer
	 */
	private static HashMap<Integer, ColumnPaddingSpec> columnPaddingSpecs = null;
	
	/**
	 * Invoke the parent constructor.
	 */
	public SocatV3Pangaea(Config config) {
		super(config);
	}

	@Override
	public String getName() {
		return "SOCATv3 from PANGAEA";
	}
	
	@Override
	protected void preprocessData() throws ImporterException {
		String[] lines = data.split("\n");
		
		// Search for the header
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(DATA_HEADER_START)) {
				firstLineNumber = i + 2; // i is zero-based!
				firstLine = lines[i + 1];
			}
		}
		
		// Store the last line
		lastLine = lines[lines.length - 1];
	}
	
	/**
	 * Get the EXPO Code from the metadata
	 * @return The EXPO Code
	 * @throws Exception If the XPath lookup fails
	 */
	private String getExpoCode() throws NemoTemplateException {
		String eventLabel = evaluateXPath(XPATH_EXPOCODE);
		return eventLabel.replaceAll("([^-]*)-.*", "$1");
	}
	
	/**
	 * Get the Ship Code.
	 * The EXPO code is of the form <Ship Code>YYYYMMDD[-...] so we can regex it
	 * @return
	 * @throws NemoTemplateException
	 */
	private String getShipCode() throws NemoTemplateException {
		String expoCode = getExpoCode();
		String shipCode = null;
		
		if (expoCode.indexOf("-") > -1) {
			shipCode = expoCode.replaceAll("(.*)[0-9][0-9][0-9][0-9][0-1][0-9][0-3][0-9]-.*", "$1");
		} else {
			shipCode = expoCode.replaceAll("(.*)[0-9][0-9][0-9][0-9][0-1][0-9][0-3][0-9]$", "$1");
		}
		
		return shipCode;
	}
	
	@Override
	protected String getTemplateTagValue(String tag) throws NemoTemplateException {
		
		String tagValue = null;
		
		switch (tag) {
		case "EXPOCODE": {
			tagValue = getExpoCode();
			break;
		}
		case "SHIP_CODE": {
			tagValue = getShipCode();
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
		case "FIRST_LINE": {
			tagValue = String.valueOf(firstLineNumber);
			break;
		}
		case "LATITUDE_FIRST_CHAR": {
			tagValue = LATITUDE_FIRST_CHAR;
			break;
		}
		case "LATITUDE_LAST_CHAR": {
			tagValue = LATITUDE_LAST_CHAR;
			break;
		}
		case "LONGITUDE_FIRST_CHAR": {
			tagValue = LONGITUDE_FIRST_CHAR;
			break;
		}
		case "LONGITUDE_LAST_CHAR": {
			tagValue = LONGITUDE_LAST_CHAR;
			break;
		}
		case "SENSOR_DEPTH": {
			tagValue = evaluateXPath(XPATH_SENSOR_DEPTH);
			break;
		}
		case "SALINITY_FIRST_CHAR": {
			tagValue = SALINITY_FIRST_CHAR;
			break;
		}
		case "SALINITY_LAST_CHAR": {
			tagValue = SALINITY_LAST_CHAR;
			break;
		}
		case "SST_FIRST_CHAR": {
			tagValue = SST_FIRST_CHAR;
			break;
		}
		case "SST_LAST_CHAR": {
			tagValue = SST_LAST_CHAR;
			break;
		}
		case "PRESSURE_FIRST_CHAR": {
			tagValue = PRESSURE_FIRST_CHAR;
			break;
		}
		case "PRESSURE_LAST_CHAR": {
			tagValue = PRESSURE_LAST_CHAR;
			break;
		}
		case "BATHYMETRY_FIRST_CHAR": {
			tagValue = BATHYMETRY_FIRST_CHAR;
			break;
		}
		case "BATHYMETRY_LAST_CHAR": {
			tagValue = BATHYMETRY_LAST_CHAR;
			break;
		}
		case "FCO2_FIRST_CHAR": {
			tagValue = FCO2_FIRST_CHAR;
			break;
		}
		case "FCO2_LAST_CHAR": {
			tagValue = FCO2_LAST_CHAR;
			break;
		}
		default: {
			tagValue = super.getTemplateTagValue(tag);
			break;
		}
		}
		
		return tagValue;
	}
	
	/**
	 * Get the time of the first data record in milliseconds since the epoch
	 * @return The time of the first data record in milliseconds since the epoch
	 */
	private long getStartDateMilliseconds() {
		return timeToMilliseconds(getDateTimeColumn(firstLine));
	}
	
	/**
	 * Get the time of the last data record in milliseconds since the epoch
	 * @return The time of the last data record in milliseconds since the epoch
	 */
	private long getEndDateMilliseconds() {
		return timeToMilliseconds(getDateTimeColumn(lastLine));
	}
	
	/**
	 * Get the Date/Time column from the given data line
	 * @param line The line
	 * @return The contents of the Date/Time column
	 */
	private String getDateTimeColumn(String line) {
		return line.substring(0, 16);
	}
	
	/**
	 * Take a time string from the SOCAT file and convert it to milliseconds since the epoch.
	 * The times in these files are of the form "YYYY-MM-DDThh:mm", so we add the seconds and timezone (UTC).
	 * @param timeString The time string from the file
	 * @return The time string as milliseconds since the epoch.
	 */
	private long timeToMilliseconds(String timeString) {
		String isoTimeString = timeString + ":00+00:00";
		ZonedDateTime parsedTime = ZonedDateTime.parse(isoTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		return parsedTime.toInstant().toEpochMilli();
	}
	
	@Override
	protected String getSeparator() {
		return "\t";
	}
	
	@Override
	protected ColumnPaddingSpec getColumnPaddingSpec(int columnIndex) throws PaddingException {
		if (null == columnPaddingSpecs) {
			columnPaddingSpecs = new HashMap<Integer, ColumnPaddingSpec>();
			
			// Latitude
			columnPaddingSpecs.put(1, new ColumnPaddingSpec(9, 5));
			
			// Longitude
			columnPaddingSpecs.put(2, new ColumnPaddingSpec( 10, 5));
			
			// Water depth
			columnPaddingSpecs.put(3, new ColumnPaddingSpec(3, 0));
			
			// Salinity
			columnPaddingSpecs.put(4, new ColumnPaddingSpec(7, 3));
			
			// SST
			columnPaddingSpecs.put(5, new ColumnPaddingSpec(7, 3));
			
			// Equilibrator Temp
			columnPaddingSpecs.put(6, new ColumnPaddingSpec(7, 3));
			
			// Atmospheric Pressure
			columnPaddingSpecs.put(7, new ColumnPaddingSpec(9, 3));
			
			// Interpolated Salinity
			columnPaddingSpecs.put(8, new ColumnPaddingSpec(7, 3));
			
			// Interpolated Atmospheric Pressure
			columnPaddingSpecs.put(9, new ColumnPaddingSpec(9, 3));
			
			// Bathymetry Depth
			columnPaddingSpecs.put(10, new ColumnPaddingSpec(5, 0));

			// Distance from land
			columnPaddingSpecs.put(11, new ColumnPaddingSpec(5, 0));
			
			// CO2 values
			columnPaddingSpecs.put(12, new ColumnPaddingSpec(8, 3));
			columnPaddingSpecs.put(13, new ColumnPaddingSpec(8, 3));
			columnPaddingSpecs.put(14, new ColumnPaddingSpec(8, 3));
		}
		
		return columnPaddingSpecs.get(columnIndex);
	}
	
	@Override
	protected void copyHeader(Iterator<String> iterator, StringBuilder output) throws ImporterException {
		
		boolean headerFinished = false;
		
		while (iterator.hasNext() && !headerFinished) {
			String headerLine = iterator.next();
			output.append(headerLine);
			output.append('\n');
			if (headerLine.startsWith(DATA_HEADER_START)) {
				headerFinished = true;
			}
		}
		
		if (!headerFinished) {
			throw new ImporterException("EOF before end of header");
		}
	}
	
	@Override
	public String getNemoOutputFormat() {
		return "ODV";
	}
	
	@Override
	protected int getStationNumber() throws ImporterException {
		return 1;
	}
	
	@Override
	public String getNemoDataType() throws ImporterException {
		return "H71";
	}

	@Override
	protected String getDataSetInternalId() throws ImporterException {
		return getExpoCode();
	}
}
