package no.bcdc.cdigenerator.importers.Pangaea;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * Basic tester for the PangaVista web service.
 * Tests getting a new session and retrieving metadata,
 * since they're the only bits we need.
 * 
 * @author Steve Jones
 *
 */
public class PangaVistaTest {

	public static void main(String[] args) {
		
		System.out.println("PangaVista Test");
		
		try {
	  
	        Service service = new Service();
	        Call call = (Call) service.createCall();
	  
	        call.setTargetEndpointAddress(new java.net.URL(PangaVistaImporter.END_POINT));
	        call.setOperationName(new QName(PangaVistaImporter.OPERATION_NAME_URI, PangaVistaImporter.OPERATION_REGISTER_SESSION));
	  
	        String sessionId = (String) call.invoke( new Object[] {} );
	  

	        Call mdCall = (Call) service.createCall();
	        mdCall.setTargetEndpointAddress(new java.net.URL(PangaVistaImporter.END_POINT));
	        mdCall.setOperationName(new QName(PangaVistaImporter.OPERATION_NAME_URI, PangaVistaImporter.OPERATION_METADATA));
	        
	        mdCall.addParameter("session", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
	        mdCall.addParameter("URI", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
	        
	        String metadata = (String) mdCall.invoke(new Object[] { sessionId, "10.1594/PANGAEA.850058" });
	        
	        System.out.println(metadata);
		
		
		} catch (Exception e) {
	        System.err.println(e.getMessage());
	      }
	}

}
