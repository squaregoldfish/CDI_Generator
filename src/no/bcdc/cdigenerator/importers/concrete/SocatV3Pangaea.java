package no.bcdc.cdigenerator.importers.concrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.importers.ColumnPaddingSpec;
import no.bcdc.cdigenerator.importers.ImporterException;
import no.bcdc.cdigenerator.importers.NemoModel;
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
	 * The name of the Date/Time column
	 */
	private static final String COL_DATE_TIME = "Date/Time";
	
	/**
	 * The name of the Latitude column
	 */
	private static final String COL_LATITUDE = "Latitude";
	
	/**
	 * The name of the longitude column
	 */
	private static final String COL_LONGITUDE = "Longitude";
	
	/**
	 * The name of the SST Column
	 */
	private static final String COL_SST = "Temp [°C]";
	
	/**
	 * The name of the SST Column
	 */
	private static final String COL_SALINITY = "Sal";
	
	/**
	 * The preferred name of the fCO2 Column
	 */
	private static final String COL_PREFERRED_FCO2 = "fCO2water_SST_wet [µatm] (Recomputed after SOCAT (Pfeil...)";
	
	/**
	 * The fallback fCO2 column name
	 */
	private static final String COL_FALLBACK_FCO2 = "fCO2water_SST_wet [µatm]";
	
	/**
	 * The name of the atmospheric pressure column
	 */
	private static final String COL_ATMOSPHERIC_PRESSURE = "PPPP [hPa]";
	
	/**
	 * The name of the WOCE Flag column
	 */
	private static final String COL_WOCE_FLAG = "Flag [#]";
	
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
	 * Indicates whether or not the data contains a salinity column
	 */
	private boolean hasSalinityColumn = true;
	
	/**
	 * Indicates whether or not the data includes atmospheric pressure measurements
	 */
	private boolean hasAtmosphericPressure = true;
	
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
			columnPaddingSpecs.put(COL_DATE_TIME, null);
			columnPaddingSpecs.put(COL_WOCE_FLAG, null);
			
			columnPaddingSpecs.put(COL_LATITUDE, new ColumnPaddingSpec(9, 5));

			columnPaddingSpecs.put(COL_LONGITUDE, new ColumnPaddingSpec(10, 5));
			
			ColumnPaddingSpec tempAndSalPadding = new ColumnPaddingSpec(7, 3);
			columnPaddingSpecs.put(COL_SALINITY, tempAndSalPadding);
			columnPaddingSpecs.put(COL_SST, tempAndSalPadding);
			
			
			ColumnPaddingSpec pressurePadding = new ColumnPaddingSpec(9, 3);
			columnPaddingSpecs.put(COL_ATMOSPHERIC_PRESSURE, pressurePadding);
			
			ColumnPaddingSpec co2Padding = new ColumnPaddingSpec(8, 3);
			columnPaddingSpecs.put(COL_FALLBACK_FCO2, co2Padding);
			columnPaddingSpecs.put(COL_PREFERRED_FCO2, co2Padding);
		}
		
		if (!columnPaddingSpecs.containsKey(columnName)) {
			throw new PaddingException("Unrecognised column name " + columnName);
		}
		
		return columnPaddingSpecs.get(columnName);
	}
	
	@Override
	public List<String> getNemoOutputFormats() {
		return Arrays.asList(new String[] {"ODV"});
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
			if (lines[i].startsWith(COL_DATE_TIME)) {
				firstLineNumber = i + 2; // i is zero-based!
				break;
			}
		}
	}
	
	@Override
	protected String getColumnHeaderStart() {
		return COL_DATE_TIME;
	}

	@Override
	protected List<Integer> getColumnsToUse(List<String> columnNames) throws ImporterException {

		List<Integer> result = new ArrayList<Integer>();
		
		// The Date/Time, Latitude and Longitude are in fixed positions
		result.add(0);
		result.add(1);
		result.add(2);
		
		int sstCol = columnNames.indexOf(COL_SST);
		if (sstCol == -1) {
			throw new ImporterException("Cannot find SST column");
		}
		result.add(sstCol);
		
		int salCol = columnNames.indexOf(COL_SALINITY);
		if (salCol == -1) {
			hasSalinityColumn = false;
		} else {
			hasSalinityColumn = true;
			result.add(salCol);
		}
		
		int fCo2Col = columnNames.indexOf(COL_PREFERRED_FCO2);
		if (fCo2Col != -1) {
			result.add(fCo2Col);
		} else {
			fCo2Col = columnNames.indexOf(COL_FALLBACK_FCO2);
			if (fCo2Col == -1) {
				throw new ImporterException("Cannot find fCO2 column");
			}
		}
		result.add(fCo2Col);
		
		int pressureCol = columnNames.indexOf(COL_ATMOSPHERIC_PRESSURE);
		if (pressureCol == -1) {
			hasAtmosphericPressure = false;
		} else {
			hasAtmosphericPressure = true;
			result.add(pressureCol);
		}
		
		int flagCol = columnNames.indexOf(COL_WOCE_FLAG);
		if (flagCol == -1) {
			throw new ImporterException("Cannot find WOCE Flag column");
		}
		result.add(flagCol);
		
		return result;
	}
	
	@Override
	public List<NemoModel> getModelsToRun() throws ImporterException {
		
		List<NemoModel> result = new ArrayList<NemoModel>();
		
		for (String outputFormat : getNemoOutputFormats()) {
			String identifier;
			
			if (hasSalinityColumn) {
				identifier = "Sal-";
				
				if (hasAtmosphericPressure) {
					identifier += "Atm";
				} else {
					identifier += "NoAtm";
				}
			} else {
				identifier = "NoSal-";
				
				if (hasAtmosphericPressure) {
					identifier = "Atm";
				} else {
					identifier = "NoAtm";
				}
			}
			
			result.add(new NemoModel(config, identifier, outputFormat));
		}
		
		return result;
	}
}
