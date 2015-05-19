package org.zlwima.emurgency.mqtt.android.config;

import android.util.Log;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.client.ClientProtocolException;

public class Base {

	public static final String TAG = "EMR";

	/**
	 * Log messages in debug mode. Skip in production
	 * FIXME doesn't do well. it's never set on debug!
	 */
	public static void log( String msg ) {
		Log.d( TAG, msg );
		if( Log.isLoggable( TAG, Log.DEBUG ) ) {
			Log.d( TAG, msg );
		}
	}

	/**
	 * Formats exceptions into log
	 */
	public static void logException( Exception e ) {
		StringBuilder builder = new StringBuilder();
		builder.append( "EMR EXCEPTION Logger... " );

		if( e instanceof JsonSyntaxException ) {
			builder.append( "\t[JsonSyntaxException " );
		} else if( e instanceof NullPointerException ) {
			builder.append( "\t[NullPointerException " );
		} else if( e instanceof IOException ) {
			builder.append( "\t[IOException " );
		} else if( e instanceof ClientProtocolException ) {
			builder.append( "\t[ClientProtocolException " );
		} else if( e instanceof UnsupportedEncodingException ) {
			builder.append( "\t[UnsupportedEncodingException " );
		} else if( e instanceof FileNotFoundException ) {
			builder.append( "\t[FileNotFoundException " );
		} else {
			builder.append( "\t[UnfilteredException " );
		}

		//includes class, cause, msg & trace
		builder.append( String.format( " in %s] STOPPED AT\n\tMETHOD: %s\n\tLINE: %s\n\tMESSAGE: %s\n\tFULL TRACE: %s\n",
				e.getStackTrace()[0].getClassName() + "->" + e.getStackTrace()[0].getFileName(),
				e.getStackTrace()[0].getMethodName(),
				e.getStackTrace()[0].getLineNumber(),
				e.getMessage(),
				e.getStackTrace().toString() ) );

		Log.e( TAG, builder.toString() );
	}
}
