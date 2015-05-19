package org.zlwima.emurgency.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class Shared {
	
	// PARAMETER
	public static final String PARAMETER_EMAIL = "email";
	public static final String PARAMETER_REGISTRATIONID = "registrationId";
	public static final String PARAMETER_MODEL = "model";
	public static final String PARAMETER_VERSION = "version";
	public static final String PARAMETER_CASE_LATITUDE = "caseLatitude";
	public static final String PARAMETER_CASE_LONGITUDE = "caseLongitude";
	public static final String PARAMETER_CASE_ADDRESS = "caseAddress";
	public static final String PARAMETER_CASE_NOTES = "caseNotes";
	
	// USER FIELDS
	public static final String USER_EMAIL = "email";
	public static final String USER_PASSWORD = "password";
	public static final String USER_CLIENTID = "clientId";	
	public static final String USER_LEVEL = "level";
	public static final String USER_LOGINSTATUS = "loginStatus";
	public static final String USER_REGISTRATIONID = "registrationId";
	public static final String USER_FIRSTNAME = "firstName";
	public static final String USER_LASTNAME = "lastName";
	public static final String USER_GENDER = "gender";
	public static final String USER_MOBILEPHONE = "mobilePhone";
	public static final String USER_STREET = "street";
	public static final String USER_CITY = "city";
	public static final String USER_ZIPCODE = "zipCode";
	public static final String USER_COUNTRY = "country";
	public static final String USER_BIRTHDATE = "birthdate";
	public static final String USER_CREATIONDATE = "creationDate";
	public static final String USER_RECEIVESNOTIFICATIONS = "receivesNotifications";
	public static final String USER_NOTIFICATIONRADIUS = "notificationRadius";
	public static final String USER_LOCATION = "location";
	public static final String USER_STATIC_LOCATION = "staticLocation";
	
	// GENDER
	public static final String GENDER_MALE = "male";
	public static final String GENDER_FEMALE = "female";
	
	// LOCATION FIELDS
	public static final String LOCATION_LATITUDE = "latitude";
	public static final String LOCATION_LONGITUDE = "longitude";
	public static final String LOCATION_ALTITUDE = "altitude";
	public static final String LOCATION_TIMESTAMP = "timestamp";
	public static final String LOCATION_PROVIDER = "provider";
	
	// ANDROID APP
	public static final String USER = "user";
	public static final String CONTEXT = "context";
	public static final String RECEIVER = "receiver";		
	public static final String COMMAND = "command";		
	public static final String USER_OBJECT = "userObject";
	public static final String LOCATION_OBJECT = "locationObject";
	public static final String VOLUNTEER_OBJECT = "volunteerObject";
	public static final String CASEDATA_OBJECT = "caseDataObject";	
	public static final String MESSAGE_TYPE = "messageType";
	public static final String CASE_ID = "caseId";
	
	public final class Results {
		public static final String LOCATION_UPDATE_CALLBACK = "locationUpdated";
		public static final String REGISTRATION_CALLBACK = "registrationValid";
		public static final String LOGIN_CALLBACK = "loginValid";
		public static final String GCM_REGISTRATION_SUCCESSFUL = "gcmValid";
	}

	public final class Commands {
		public static final String LOGIN = "login";
		public static final String REGISTRATION = "register";
		public static final String REGISTER_GCM = "registerGCM";
		public static final String UPDATE_LOCATION = "updateLocation";
	}
	
	public final class Rest {
		public static final int DEFAULT_PORT = 80;
		public static final int TIMEOUT_MILLIS = 5000;
		public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
		public static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8";
		public static final String WEBSERVICE_URL	= "http://137.226.188.51/emurgency/command";
		public static final String GCMSERVICE_URL	= "http://137.226.188.51/emurgency/gcm";
		public static final String WEBSOCKET_URL	= "ws://137.226.188.51:58080";		
		public static final String LOGIN_URL			= WEBSERVICE_URL + "/login";
		public static final String LOCATION_UPDATE_URL	= WEBSERVICE_URL + "/updateLocation";
		public static final String REGISTRATION_URL		= WEBSERVICE_URL + "/register";
		public static final String GCM_REGISTER_URL		= GCMSERVICE_URL + "/register";
		public static final String GCM_UNREGISTER_URL	= GCMSERVICE_URL + "/unregister";
	}
	
	public final class WebsocketCallback {
		public static final int CLIENT_IS_STATIC_CLIENT = -5;
		public static final int CLIENT_SENDS_CASE_ID = 2;
		public static final int CLIENT_SENDS_ACCEPT_MISSION = 3;
		public static final int CLIENT_SENDS_LOCATION_UPDATE = 7;
		public static final int SERVER_SENDS_CASEDATA = 11;
		public static final int SERVER_SENDS_CLOSE_CASE = 15;
	}
	
	public final class UpdateLocationCallback {
		public static final int UPDATED_USER_IS_CONFIRMED = 1;
		public static final int UPDATED_USER_IS_NOT_CONFIRMED = 5;	
		public static final int FAILED = 8;
	}
	
	public final class RegisterCallback {
		public static final int REGISTERED = 0;
		public static final int ERROR = 1;
		public static final int EXISTS_ALREADY = 2;
	}
	
	public final class LoginCallback {
		public static final int INVALID = 0;
		public static final int VALID = 1;
		public static final int CONFIRMED = 2;
	}
	
	public final class AcceptMissionCallback {
		public static final int ACCEPTED = 2;
		public static final int FAILED = 6;
	}		
	
	/*
	 * Calculates the distance between 2 coordinates (lat/long) in meters
	 */
	public static double calculateDistance( double lat1, double long1, double lat2, double long2 ) {
		double earthRadius = 6371000; // in meters
		double dLat = Math.toRadians( lat2 - lat1 );
		double dLng = Math.toRadians( long2 - long1 );
		double sindLat = Math.sin( dLat / 2 );
		double sindLng = Math.sin( dLng / 2 );
		double a = Math.pow( sindLat, 2 ) + Math.pow( sindLng, 2 ) * Math.cos( lat1 ) * Math.cos( lat2 );
		double c = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );
		double dist = earthRadius * c;
		return dist;
	}
	
	/*
	 * Reads an InputStream and returns it as String
	 */
	public static String streamToString( InputStream is ) throws IOException {
		BufferedReader r = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
		StringBuilder total = new StringBuilder();
		String line;
		while( (line = r.readLine()) != null ) {
			total.append( line );
		}
		return total.toString();
	}	
	
}
