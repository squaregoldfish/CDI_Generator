package no.bcdc.cdigenerator.importers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import no.bcdc.cdigenerator.output.PangaeaImporter;

public class SocatPangaea extends PangaeaImporter {

	/**
	 * The constructor does nothing.
	 */
	public SocatPangaea() {
		super();
	}

	@Override
	public String getDataSetIdsDescriptor() {
		return "DOIs";
	}
	
	@Override
	public String getDataSetIdDescriptor() {
		return "DOI";
	}
	
	@Override
	public boolean validateIdFormat(String id) {
		boolean result = false;
		
		if (null != id) {
			result= id.matches("10\\.1594/PANGAEA\\.[0-9]+");
		}
		
		return result;
	}
	
	@Override
	protected String getDataSetData(String dataSetId) throws Exception {
		
		String result = null;
		
		HttpsURLConnection conn = null;
		InputStream stream = null;
		StringWriter writer = null;
		
		
		try {
			URL url = makeUrl(dataSetId);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			
			stream = conn.getInputStream();
			writer = new StringWriter();
			IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
			result = writer.toString();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				writer.close();
				stream.close();
				conn.disconnect();
			} catch (IOException e) {
				// Do nothing - we can say that we tried.
			}
		}
		
		
		return result;
		
	}
	
	private URL makeUrl(String dataSetId) throws MalformedURLException {
		StringBuffer url = new StringBuffer("https://doi.pangaea.de/");
		url.append(dataSetId);
		url.append("?format=textfile");
		
		return new URL(url.toString());
	}
}
