package no.bcdc.cdigenerator;

import java.io.File;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import no.bcdc.cdigenerator.importers.PaddingException;

/**
 * Look through all SOCAT files to find files with different
 * numbers of columns. Reformat them as per the CDI Generator
 * so we can configure the columns for NEMOs
 * @author Steve Jones
 *
 */
public class ColumnsFinder {

	private static final String IN_DIR = "/Users/zuj007/Documents/SOCAT/v4Zip/SOCAT_v4/datasets";
	
	private static final String OUT_DIR = "/Users/zuj007/Documents/SeaDataNet/SOCATv4Columns";
	
	private static final String COLUMN_HEADER_START = "Date/Time";
	
	private File inDir;
	
	private File outDir;
	
	public static void main(String[] args) {
		try {
			new ColumnsFinder().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ColumnsFinder() throws ConfigException {
		inDir = new File(IN_DIR);
		checkDir(inDir, false);
		
		outDir= new File(OUT_DIR);
		checkDir(outDir, true);
	}
	
	/**
	 * Check a directory for existence, directoryness, and readability and writeability
	 * @throws ConfigException If the directory has none of those things.
	 */
	private static void checkDir(File directory, boolean mustBeWritable) throws ConfigException {
		if (!directory.exists()) {
			throw new ConfigException(directory, "does not exist");
		} else if (!directory.isDirectory()) {
			throw new ConfigException(directory, "not a directory");
		} else if (!directory.canRead()) {
			throw new ConfigException(directory, "not readable");
		} else if (mustBeWritable && !directory.canWrite()) {
			throw new ConfigException(directory, "not writeable");
		}
	}

	private void start() throws Exception {
		
		List<Integer> columnCountsFound = new ArrayList<Integer>();
		
		File[] inFiles = inDir.listFiles();
		for (int i = 0; i < inFiles.length; i++) {			
			String fileData = FileUtils.readFileToString(inFiles[i], StandardCharsets.UTF_8);
			String[] headerAndFirstDataLine = getHeaderAndFirstDataLine(fileData);

			String[] columns = headerAndFirstDataLine[0].split("\t");
			
			if (!columnCountsFound.contains(columns.length)) {
				columnCountsFound.add(columns.length);

				String[] fields = headerAndFirstDataLine[1].split("\t");
				
				StringBuilder reformattedHeader = new StringBuilder();
				StringBuilder reformattedLine = new StringBuilder();
				for (int j = 0; j < columns.length; j++) {
					reformattedHeader.append(columns[j]);
					reformattedLine.append(padField(columns[j], fields[j]));
					if (j < columns.length - 1) {
						reformattedHeader.append(';');
						reformattedLine.append(';');
					}
				}
				
				File outputFile = new File(outDir, columns.length + ".txt");
				FileUtils.writeStringToFile(outputFile, inFiles[i].getName() + "\n" + reformattedHeader.toString() + "\n" + reformattedLine.toString(), StandardCharsets.UTF_8);
			}
		}
	}
	
	private String[] getHeaderAndFirstDataLine(String fileData) {
		String[] result = new String[2];
		
		String[] lines = fileData.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(COLUMN_HEADER_START)) {
				result[0] = lines[i];
				result[1] = lines[i + 1];
				break;
			}
		}
		
		return result;
	}
	
	private String padField(String columnName, String value) throws PaddingException {
		String result = null;
		
		switch (columnName) {
		case "Algorithm":
		case "Flag [#]":
		case "Date/Time": {
			result = value;
			break;
		}
		case "Latitude": {
			result = pad(value, 9, 5);
			break;
		}
		case "Longitude": {
			result = pad(value, 10, 5);
			break;
		}
		case "Depth water [m]": {
			result = pad(value, 3, 0);
			break;
		}
		case "Temp [°C]":
		case "Tequ [°C]":
		case "Sal":
		case "Sal interp": {
			result = pad(value, 7, 3);
			break;
		}
		case "PPPP [hPa]":
		case "Pequ [hPa]":
		case "PPPP interp [hPa]": {
			result = pad(value, 9, 3);
			break;
		}
		case "Bathy depth interp/grid [m]": {
			result = pad(value, 5, 0);
			break;
		}
		case "Distance [km]": {
			result = pad(value, 5, 0);
			break;
		}
		case "xCO2water_SST_dry [µmol/mol]":
		case "fCO2water_SST_wet [µatm]":
		case "fCO2water_equ_wet [µatm]":
		case "fCO2water_SST_wet [µatm] (Recomputed after SOCAT (Pfeil...)":
		case "pCO2water_SST_wet [µatm]":
		case "pCO2water_equ_wet [µatm]":
		case "xCO2water_equ_dry [µmol/mol]":
		case "xCO2air_interp [µmol/mol]": {
			result = pad(value, 8, 3);
			break;
		}
		default: {
			throw new PaddingException("Unrecognised column header " + columnName);
		}
		}
		
		return result;
	}


	private String pad(String value, int length, int precision) throws PaddingException {
		
		String result;
		
		boolean isNumeric = true;
		double numericValue = 0.0;
		
		try {
			numericValue = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			isNumeric = false;
		}
		
		if (!isNumeric) {
			result = padString(value, length);
		} else {
			result = padNumber(numericValue, length, precision);
		}
		
		return result;
	}
	
	private String padNumber(double value, int requiredLength, int precision) {
		StringBuilder formatString = new StringBuilder();
		
		int precisionDigits = precision;
		if (precisionDigits > 0) {
			precisionDigits++;
		}
		
		// The extra 1 is for the sign, which is explicitly added below
		int digitsBeforePoint = requiredLength - precisionDigits - 1; 
		
		for (int i = 0; i < digitsBeforePoint; i++) {
			formatString.append('0');
		}
		
		if (precisionDigits > 0) {
			formatString.append('.');
			for (int i = 1; i < precisionDigits; i++) {
				formatString.append('0');
			}
		}
				
		DecimalFormat formatter = new DecimalFormat(formatString.toString());
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setPositivePrefix("+");
		formatter.setNegativePrefix("-");
		
		return formatter.format(value);
	}

	private String padString(String value, int length) throws PaddingException {
		
		StringBuilder output = new StringBuilder();
		
		int paddingRequired = length - value.length();
		if (paddingRequired < 0) {
			throw new PaddingException("Value length is larger than available length");
		} else if (paddingRequired > 0) {
			for (int i = 0; i < paddingRequired; i++) {
				output.append(' ');
			}
		}
		
		output.append(value);
		
		return output.toString();
	}

}
