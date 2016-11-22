package no.bcdc.cdigenerator.importers.concrete;

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
	private static final String SALINITY_FIRST_CHAR = "45";
	
	/**
	 * Position of the end of the salinity field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SALINITY_LAST_CHAR = "51";
	
	/**
	 * Position of the start of the SST field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SST_FIRST_CHAR = "53";
	
	/**
	 * Position of the end of the SST field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String SST_LAST_CHAR = "59";
	
	/**
	 * Position of the start of the Atmospheric Pressure field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String PRESSURE_FIRST_CHAR = "69";
	
	/**
	 * Position of the end of the Atmospheric Pressure field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String PRESSURE_LAST_CHAR = "77";
	
	/**
	 * Position of the start of the Bathymetry field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String BATHYMETRY_FIRST_CHAR = "98";
	
	/**
	 * Position of the end of the Bathymetry field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String BATHYMETRY_LAST_CHAR = "102";
	
	/**
	 * Position of the start of the fCO2 field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String FCO2_FIRST_CHAR = "129";
	
	/**
	 * Position of the end of the fCO2 field.
	 * Note we have an offset of one because NEMO inserts a space at the start of each line.
	 */
	private static final String FCO2_LAST_CHAR = "136";
	
	/**
	 * The line containing the first data record
	 */
	private int firstLineNumber = -1;
	
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
		return "SOCATv3";
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
	
	@Override
	public String getPlatformCode() throws ImporterException {
		return getShipCode();
	}
	
	@Override
	public String getDataSetName() throws ImporterException {
		return getName();
	}

	@Override
	public String getDataSetId() throws ImporterException {
		return getExpoCode();
	}
	
	@Override
	public String getCruiseName() throws ImporterException {
		return getExpoCode();
	}
	
	@Override
	public String getCsrReference() throws ImporterException {
		return null;
	}

	@Override
	public String getCurvesDescription() throws ImporterException {
		// Curves are not yet implemented
		return null;
	}

	@Override
	public String getCurvesName() throws ImporterException {
		// Curves are not yet implemented
		return null;
	}

	@Override
	public String getCurvesCoordinates() throws ImporterException {
		// Curves are not yet implemented
		return null;
	}
}
