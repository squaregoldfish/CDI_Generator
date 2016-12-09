package no.bcdc.couplingtablebuilder;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Determines whether or not a given file should be included
 * in the coupling table.
 * 
 * The data files directory can contain both data files and CDI summary files,
 * formatted as:
 * 
 * {@code <local-cdi-id>_<format>.txt}
 * 
 * or
 * 
 * {@code <local-cdi-id>_<format>_summary.txt}
 * 
 * The summary files will be ignored
 * 
 * @author Steve Jones
 *
 */
public class DataFileFilter implements FilenameFilter {
	
	/**
	 * The data files directory. All files must be in this directory
	 * to pass the filter.
	 */
	private File filesDir;
	
	/**
	 * Constructor
	 * @param config The application configuration
	 */
	public DataFileFilter(Config config) {
		filesDir = config.getFilesDir();
	}

	@Override
	public boolean accept(File dir, String filename) {
		boolean result = true;
		
		if (!dir.equals(filesDir)) {
			result = false;
		} else if (filename.startsWith(".")) {
			result = false;
		} else if (filename.indexOf("_summary.txt") != -1) {
			result = false;
		}
		
		return result;
	}
	
}
