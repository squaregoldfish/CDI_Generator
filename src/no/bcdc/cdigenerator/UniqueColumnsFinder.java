package no.bcdc.cdigenerator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * Look through all SOCAT files to find files with different
 * numbers of columns. Reformat them as per the CDI Generator
 * so we can configure the columns for NEMOs
 * @author Steve Jones
 *
 */
public class UniqueColumnsFinder {

	private static final String IN_DIR = "/Users/zuj007/Documents/SOCAT/v4Zip/SOCAT_v4/datasets";
	
	private static final String OUT_DIR = "/Users/zuj007/Documents/SeaDataNet/SOCATv4Columns";
	
	private static final String COLUMN_HEADER_START = "Date/Time";
	
	private File inDir;
	
	private File outDir;
	
	private Map<String, String> uniqueHeaders;
	
	public static void main(String[] args) {
		try {
			new UniqueColumnsFinder().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public UniqueColumnsFinder() throws ConfigException {
		inDir = new File(IN_DIR);
		checkDir(inDir, false);
		
		outDir= new File(OUT_DIR);
		checkDir(outDir, true);
		
		uniqueHeaders = new HashMap<String, String>();
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
		
		File[] inFiles = inDir.listFiles();
		for (int i = 0; i < inFiles.length; i++) {			
			String fileData = FileUtils.readFileToString(inFiles[i], StandardCharsets.UTF_8);
			String headerLine = getHeaderLine(fileData);

			if (headerLine.contains("fCO2water_SST_wet")) {
				if (!uniqueHeaders.containsKey(headerLine)) {
					uniqueHeaders.put(headerLine, inFiles[i].getName());
				}
			}
		}

		StringBuilder output = new StringBuilder();
		for (String header : uniqueHeaders.keySet()) {
			output.append(uniqueHeaders.get(header));
			output.append(':');
			output.append(header);
			output.append('\n');
		}
		
		File outputFile = new File(outDir, "unique_columns.txt");
		FileUtils.writeStringToFile(outputFile, output.toString(), StandardCharsets.UTF_8);
	}
	
	private String getHeaderLine(String fileData) {
		String result = null;
		
		String[] lines = fileData.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(COLUMN_HEADER_START)) {
				result = lines[i];
				break;
			}
		}
		
		return result;
	}
}
