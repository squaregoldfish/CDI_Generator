package no.bcdc.cdigenerator.lookups;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
	
	/**
	 * The column containing the platform code
	 */
	private static final int COL_PLATFORM_CODE = 4;
	
	/**
	 * The column containing the start date
	 */
	private static final int COL_START_DATE = 5;
	
	/**
	 * The column containing the end date
	 */
	private static final int COL_END_DATE = 6;
	
	/**
	 * The column containing the CSR reference
	 */
	private static final int COL_CSR_REFERENCE = 0;
	
	/**
	 * The parsed data structure
	 */
	private Map<String, TreeSet<CSREntry>> csrLookups = null;
	
	private DateTimeFormatter dateFormatter = null;
	
	/**
	 * Constructor - downloads and extracts data
	 * @param config The configuration
	 * @throws IOException If the data can't be downloaded
	 * @throws CSRLookupException If the downloaded data is invalid
	 */
	public CSRReferenceLookup(Config config) throws IOException, CSRLookupException {
		System.out.println("Downloading CSR data...");
		
		dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
		csrLookups = new HashMap<String, TreeSet<CSREntry>>();
		
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
		
		for (CSREntry entry : csrLookups.get(platformCode)) {
			if (entry.encompassesDate(startDate)) {
				result = entry.getCsrReference();
				break;
			}
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
			try {
				String line = lines.get(i);
				
				String[] fields = new String[COLUMN_COUNT];
				
				int currentIndex = 0; // We know the first character is a quote
				int currentField = 0;
				
				while (currentField < COLUMN_COUNT) {
					int startQuote = line.indexOf('"', currentIndex);
					int endQuote = line.indexOf('"', startQuote + 1);
					fields[currentField] = line.substring(startQuote + 1, endQuote);
					currentIndex = endQuote + 1;
					currentField++;
				}
				
				String csrReference = stripQuotes(fields[COL_CSR_REFERENCE]);
				String platformCode = stripQuotes(fields[COL_PLATFORM_CODE]);
				LocalDate startDate = LocalDate.parse(stripQuotes(fields[COL_START_DATE]), dateFormatter);
				LocalDate endDate = LocalDate.parse(stripQuotes(fields[COL_END_DATE]), dateFormatter);
				
				if (!csrLookups.containsKey(platformCode)) {
					csrLookups.put(platformCode, new TreeSet<CSREntry>());
				}
				
				TreeSet<CSREntry> platformLookups = csrLookups.get(platformCode);
				CSREntry entry = new CSREntry(startDate, endDate, csrReference);
				platformLookups.add(entry);
			} catch (Exception e) {
				throw new CSRLookupException(i, e.getMessage());
			}
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
