package no.bcdc.cdigenerator.importers.concrete;

import java.util.HashMap;
import java.util.Iterator;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.ColumnPaddingSpec;
import no.bcdc.cdigenerator.importers.ImporterException;
import no.bcdc.cdigenerator.importers.ValueLookupException;
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
	 * The number of columns in the file
	 */
	private int columnCount = -1;
	
	/**
	 * The list of column padding specs for this importer
	 */
	private static HashMap<String, ColumnPaddingSpec> columnPaddingSpecs = null;
	
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
	 * Get the EXPO Code from the metadata.
	 * 
	 * PANGAEA appends '-track' to all EXPO Codes, so we remove it.
	 * 
	 * @return The EXPO Code
	 * @throws Exception If the XPath lookup fails
	 */
	private String getExpoCode() {
		
		String result = null;
		
		String eventLabel = evaluateXPath("EXPOCODE", XPATH_EXPOCODE);
		if (null != eventLabel) {
			result = eventLabel.replaceAll("(.*)-track$", "$1");
		}
		
		return result;
	}
	
	/**
	 * Get the Ship Code.
	 * The EXPO code is of the form <Ship Code>YYYYMMDD[-...] so we can regex it
	 * @return
	 * @throws ValueLookupException
	 */
	private String getShipCode() {
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
	protected String lookupTemplateTagValue(String tag) throws ImporterException, ValueLookupException {
		
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
			tagValue = evaluateXPath("SENSOR_DEPTH", XPATH_SENSOR_DEPTH);
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
			tagValue = super.lookupTemplateTagValue(tag);
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
	protected ColumnPaddingSpec getColumnPaddingSpec(String columnName) throws PaddingException {
		if (null == columnPaddingSpecs) {
			columnPaddingSpecs = new HashMap<String, ColumnPaddingSpec>();
			
			// Non-reformatted fields. They must be added so we know that the column
			// is known. If you see what I mean. If you don't the logic below will help you.
			columnPaddingSpecs.put("Date/Time", null);
			columnPaddingSpecs.put("Algorithm", null);
			columnPaddingSpecs.put("Flag [#]", null);
			
			columnPaddingSpecs.put("Latitude", new ColumnPaddingSpec(9, 5));

			columnPaddingSpecs.put("Longitude", new ColumnPaddingSpec(10, 5));
			
			columnPaddingSpecs.put("Depth water [m]", new ColumnPaddingSpec(3, 0));

			ColumnPaddingSpec tempAndSalPadding = new ColumnPaddingSpec(7, 3);
			columnPaddingSpecs.put("Sal", tempAndSalPadding);
			columnPaddingSpecs.put("Sal interp", tempAndSalPadding);
			columnPaddingSpecs.put("Temp [°C]", tempAndSalPadding);
			columnPaddingSpecs.put("Tequ [°C]", tempAndSalPadding);
			
			
			ColumnPaddingSpec pressurePadding = new ColumnPaddingSpec(9, 3);
			columnPaddingSpecs.put("Pequ [hPa]", pressurePadding);
			columnPaddingSpecs.put("PPPP [hPa]", pressurePadding);
			columnPaddingSpecs.put("PPPP interp [hPa]", pressurePadding);
			
			ColumnPaddingSpec distanceAndDepthPadding = new ColumnPaddingSpec(5, 0);
			columnPaddingSpecs.put("Bathy depth interp/grid [m]", distanceAndDepthPadding);
			columnPaddingSpecs.put("Distance [km]", distanceAndDepthPadding);
			
			ColumnPaddingSpec co2Padding = new ColumnPaddingSpec(8, 3);
			columnPaddingSpecs.put("fCO2water_equ_wet [µatm]", co2Padding);
			columnPaddingSpecs.put("fCO2water_SST_wet [µatm]", co2Padding);
			columnPaddingSpecs.put("fCO2water_SST_wet [µatm] (Recomputed after SOCAT (Pfeil...)", co2Padding);
			columnPaddingSpecs.put("pCO2water_equ_wet [µatm]", co2Padding);
			columnPaddingSpecs.put("pCO2water_SST_wet [µatm]", co2Padding);
			columnPaddingSpecs.put("xCO2air_interp [µmol/mol]", co2Padding);
			columnPaddingSpecs.put("xCO2water_equ_dry [µmol/mol]", co2Padding);
			columnPaddingSpecs.put("xCO2water_SST_dry [µmol/mol]", co2Padding);
		}
		
		if (!columnPaddingSpecs.containsKey(columnName)) {
			throw new PaddingException("Unrecognised column name " + columnName);
		}
		
		return columnPaddingSpecs.get(columnName);
	}
	
	@Override
	protected String[] copyHeader(Iterator<String> iterator, StringBuilder output) throws ImporterException {
		
		boolean headerFinished = false;
		String[] columnHeadings = null;
		
		while (iterator.hasNext() && !headerFinished) {
			String headerLine = iterator.next();
			output.append(headerLine);
			output.append('\n');
			if (headerLine.startsWith(DATA_HEADER_START)) {
				columnHeadings = headerLine.split("\t");
				headerFinished = true;
			}
		}
		
		if (!headerFinished) {
			throw new ImporterException("EOF before end of header");
		}
		
		return columnHeadings;
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
	
	@Override
 	protected void preprocessData() throws ImporterException {
		
		// Find the first data line
		String[] lines = data.split("\n");
		
		// Search for the header
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(DATA_HEADER_START)) {
				firstLineNumber = i + 2; // i is zero-based!
				columnCount = lines[i].split("\t").length;
				break;
			}
		}
	}

}
