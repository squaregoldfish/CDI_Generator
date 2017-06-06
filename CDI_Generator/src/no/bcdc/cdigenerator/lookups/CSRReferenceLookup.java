package no.bcdc.cdigenerator.lookups;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import no.bcdc.cdigenerator.Config;

/**
 * Class for looking up CSR references.
 * 
 * <p>
 *   Downloads the semi-colon separated file, and extracts
 *   it into a nice data structure for quick interrogation
 * </p>
 * @author Steve Jones
 *
 */
public class CSRReferenceLookup {

	/**
	 * The number of columns in the CSR file
	 */
	private static final int COLUMN_COUNT = 7;
	
	private static final int COL_PLATFORM_CODE = 4;
	
	private static final int COL_START_DATE = 5;
	
	private static final int COL_CSR_REFERENCE = 0;
	
	/**
	 * The parsed data structure
	 */
	private Map<String, Map<LocalDate, String>> csrLookups = null;
	
	private DateTimeFormatter dateFormatter = null;
	
	/**
	 * Constructor - downloads and extracts data
	 * @param config The configuration
	 * @throws IOException If the data can't be downloaded
	 * @throws CSRLookupException If the downloaded data is invalid
	 */
	public CSRReferenceLookup(Config config) throws IOException, CSRLookupException {
		System.out.println("Downloading CSR data...");
		
		dateFormatter = DateTimeFormatter.ofPattern("YYYYMMDD");
		csrLookups = new HashMap<String, Map<LocalDate, String>>();
		
		URL url = config.getCSRDownloadUrl();
		
		String csrData = new String(IOUtils.toByteArray(url)); 
		
		extractCSRData(csrData);
	}
	
	/**
	 * Look up a CSR reference
	 * @param platformCode The platform code
	 * @param startDate The cruise start date
	 * @return The CSR reference, or {@code null} if none is found
	 */
	public String getCSRReference(String platformCode, LocalDate startDate) {
		String result = null;
		
		Map<LocalDate, String> platformLookups = csrLookups.get(platformCode);
		if (null != platformLookups) {
			result = platformLookups.get(startDate);
		}
		
		return result;
	}

	/**
	 * Extract the downloaded data
	 * @param data The downloaded data
	 * @throws CSRLookupException If the data is invalid
	 */
	private void extractCSRData(String data) throws CSRLookupException {
		
		List<String> lines = Arrays.asList(data.split("\n"));
		
		// Skip the header
		for (int i = 1; i < lines.size(); i++) {
			String[] fields = lines.get(i).split(";");
			if (fields.length != COLUMN_COUNT) {
				throw new CSRLookupException(i + 1, "Incorrect number of columns");
			}
				
			String csrReference = stripQuotes(fields[COL_CSR_REFERENCE]);
			String platformCode = stripQuotes(fields[COL_PLATFORM_CODE]);
			LocalDate startDate = LocalDate.parse(stripQuotes(fields[COL_START_DATE]), dateFormatter);
			
			if (!csrLookups.containsKey(platformCode)) {
				csrLookups.put(platformCode, new HashMap<LocalDate, String>());
			}
			
			Map<LocalDate, String> platformLookups = csrLookups.get(platformCode);
			if (platformLookups.containsKey(startDate)) {
				throw new CSRLookupException(i + 1, "Platform " + platformCode + " already has a CSR for " + startDate.format(dateFormatter));
			}

			platformLookups.put(startDate, csrReference);
		}
	}
	
	/**
	 * Remove the quotes from a string
	 * @param value The string
	 * @return The string with quotes removed
	 */
	private String stripQuotes(String value) {
		return value.replaceAll("\"", "");
	}
}
