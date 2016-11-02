package no.bcdc.cdigenerator.importers;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A filename filter for model files.
 * 
 * It is constructed with the name of an importer, and will
 * only allow files that begin with that name.
 * 
 * @author Steve Jones
 *
 */
public class ModelFilenameFilter implements FilenameFilter {

	/**
	 * The name of the importer whose files we're interested in
	 */
	private String fileNameStart;
	
	/**
	 * Constructor
	 * @param importerName The name of the importer whose files we're interested in
	 */
	public ModelFilenameFilter(String importerName) {
		fileNameStart = importerName.replaceAll(" ", "_");
	}
	
	@Override
	public boolean accept(File dir, String name) {
		boolean result = true;
		
		if (!name.startsWith(fileNameStart)) {
			result = false;
		} else {
			
			File testFile = new File(dir, name);
			if (!testFile.exists()) {
				result = false;
			} else if (testFile.isDirectory()) {
				result = false;
			}
		}

		return result;
	}

}
