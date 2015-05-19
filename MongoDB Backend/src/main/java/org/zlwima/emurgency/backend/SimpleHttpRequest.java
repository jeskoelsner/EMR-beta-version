package org.zlwima.emurgency.backend;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class SimpleHttpRequest {
	
	protected static final Logger logger = Logger.getLogger( SimpleHttpRequest.class.getName() );

	public static void httpPost( String url, String data ) { 
		HttpClient httpClient = new DefaultHttpClient();
		
		try {
			HttpPost httpPost = new HttpPost( url );		
		    //httpPost.addHeader( "Content-type", "application/json;charset=UTF-8" );
		    //httpPost.addHeader( "Accept", "application/json;charset=UTF-8" );
			StringEntity entity =  new StringEntity( data, "UTF-8" );
			entity.setContentType( "application/json;charset=UTF-8" );
			entity.setContentEncoding( new BasicHeader( HTTP.CONTENT_TYPE, "application/json;charset=UTF-8" ) );
			httpPost.setEntity( entity );
			HttpResponse response = httpClient.execute( httpPost );
			String result = response.getEntity() != null ? EntityUtils.toString( response.getEntity() ) : "no response entity";			
			logger.log( Level.INFO, "postHttpRequest got response: {0}", result );
			
		} catch( Exception ex ) {
			logger.log( Level.INFO, "EXCEPTION: {0}", ex.getMessage());
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
		
	}
}
