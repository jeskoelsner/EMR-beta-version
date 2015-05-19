package org.zlwima.emurgency.webapp;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.zlwima.emurgency.backend.Backend;
import org.zlwima.emurgency.backend.Shared;
import org.zlwima.emurgency.backend.SimpleHttpRequest;
import org.zlwima.emurgency.backend.model.EmrCaseData;
import org.zlwima.emurgency.backend.model.EmrUser;
import org.zlwima.emurgency.backend.model.EmrVolunteer;
import org.zlwima.emurgency.backend.model.User;

import static org.zlwima.emurgency.webapp.StartupServlet.CASES;
import static org.zlwima.emurgency.webapp.StartupServlet.PROFILER;
import static org.zlwima.emurgency.webapp.StartupServlet.PUBLISHER;

public class Publisher implements MqttCallback {

	public interface UIListener {
		public void onRefresh( CaseReply reply, EmrCaseData caseData );
	}

	public enum ServerReply {
		INVALID, VALID_CONFIRMED, VALID_UNCONFIRMED, USER_EXISTS_ALREADY, USER_REGISTERED
	}

	public enum CaseReply {
		UPDATE_CASE, ACCEPT_CASE, CLOSE_CASE
	}

	private final String CLIENT_SUBSCRIPTION = "client/#";
	
	private String BROKER_URL = "";
	private String SESSION_ID = "";
	
	private MqttClient mqttClient;

	public Publisher( String brokerUrl, String sessionId ) {
		this.BROKER_URL = brokerUrl;
		this.SESSION_ID = sessionId;
		initNewMqttClient();
	}

	List<UIListener> uiListener = new ArrayList<UIListener>();

	public void refreshUIListeners( CaseReply reply, EmrCaseData caseToRefresh ) {
		System.out.println( "-> refreshUIListeners() CASEID: " + caseToRefresh.getCaseId() );
		for( UIListener listener : uiListener ) {
			try {
				listener.onRefresh( reply, caseToRefresh );
			} catch( Exception ex ) {
				System.out.println( "listener.OnRefresh (" + listener + ") EXCEPTION: " + ex.getMessage() );
			}
		}
	}

	public void addUIListener( UIListener listener ) {
		System.out.println( "-> addUIListener()" );
		uiListener.add( listener );
	}

	public void removeUIListener( UIListener listener ) {
		System.out.println( "-> removeUIListener()" );		
		uiListener.remove( listener );
	}

	private void initNewMqttClient() {
		System.out.println( "INITIALIZING NEW PUBLISHER..." );
		try {
			mqttClient = new MqttClient( BROKER_URL, SESSION_ID + System.currentTimeMillis(), new MemoryPersistence() );
		} catch( MqttException e ) {
			System.out.println( "MqttException in Publisher().initNewMqttClient() " + e.getMessage() );
			System.exit( 1 );
		}
	}

	public void start() {
		System.out.println( "STARTING PUBLISHER..." );
		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession( false );
			mqttClient.connect( options );
			mqttClient.setCallback( this );
			mqttClient.subscribe( CLIENT_SUBSCRIPTION );
			System.out.println( "OPTIONS: " + options.getConnectionTimeout() + " / " + options.getKeepAliveInterval() );
		} catch( MqttException e ) {
			System.out.println( "MqttException in start() " + e.getMessage() );
			System.exit( 1 );
		}
	}

	public void stop() {
		System.out.println( "STOPING PUBLISHER..." );
		try {
			mqttClient.disconnect();
			mqttClient.close();
		} catch( MqttException ex ) {
			System.out.println( "STOPING FAILED..." + ex.getMessage() );
		}
	}

	public void sendMqttMessage( String topic, String message ) throws MqttException {
		System.out.println( "sendMqttMessage with Topic: " + topic + " / Message: " + message );
		mqttClient.getTopic( topic ).publish( new MqttMessage( message.getBytes() ) );
	}

	// broadcast caseData to all users in its notification list
	public void broadcastCaseData( EmrCaseData caseData, boolean isNew ) {
            if(isNew){
                ForwardedCaseData forwardedCaseData = new ForwardedCaseData(
			caseData.getCaseId(),
			caseData.getCaseLocation().getLatitude(),
			caseData.getCaseLocation().getLongitude(),
			caseData.getCaseAddress(),				
			caseData.getCaseNotes(),
			caseData.getNotifiedUsers().size()
		);
		
		SimpleHttpRequest.httpPost( "http://as-emurgency.appspot.com/api/case/add", new Gson().toJson( forwardedCaseData ) );
            }
		System.out.println( "BROADCASTING CASE: " + caseData.getCaseId() + " TO: " + caseData.getNotifiedUsers().size() + " USERS" );
		long caseRunningTime = System.currentTimeMillis() - caseData.getCaseStartTimeMillis();
		caseData.setCaseRunningTimeMillis( caseRunningTime );
                
		if( caseRunningTime < caseData.getCaseTimeOutValue() ) {
			try {
				for( EmrUser aUser : caseData.getNotifiedUsers() ) {
					sendMqttMessage( "server/" + aUser.getClientId() + "/updateCase", caseData.toJson() );
				}
			} catch( MqttException ex ) {
				System.out.println( "BROADCAST EXCEPTION: " + ex.getMessage() );
			}
		} else {
			System.out.println( "BROADCASTER TIMES OUT CASE " + caseData.getCaseId() );
			CASES.remove( caseData );
		}
	}

	@Override
	public void deliveryComplete( IMqttDeliveryToken mdt ) {
		System.out.println( "*** DELIVERY COMPLETE ***" );
	}

	@Override
	public void connectionLost( Throwable thrwbl ) {
		System.out.println( "*** CONNECTION LOST ***" );
		initNewMqttClient();
	}

	@Override
	public void messageArrived( String topic, MqttMessage message ) throws Exception {
		System.out.println( "MESSAGE RECEIVED (PUBLISHER): " + topic.toString() + " ... " + message.toString() );
		String action = topic.substring( topic.lastIndexOf( "/" ) );
		String clientId = topic.split( "/" )[1];
		System.out.println( "ACTION:" + action + " / CLIENTID:" + clientId );

		if( action.equals( "/updateLocation" ) ) {
			receivedUpdateLocation( message.toString(), clientId );
		} else if( action.equals( "/acceptCase" ) ) {
			receivedAcceptCase( message.toString(), clientId );
		} else if( action.equals( "/login" ) ) {
			receivedLogin( message.toString() );
		} else if( action.equals( "/logout" ) ) {
			receivedLogout( message.toString() );
		} else if( action.equals( "/registration" ) ) {
			receivedRegistration( message.toString() );
		} else if( action.equals( "/arrivedAtCase" ) ) {
			receivedArrivedAtCase( message.toString() );
		} else if( action.equals( "/sms" ) ) {
			receivedSMS( message.toString() );
		}
	}
        
        private void receivedSMS( String caseData ) {     
            EmrCaseData newCaseData = new Gson().fromJson(caseData, EmrCaseData.class);
				
            newCaseData.setCaseTimeOutValue(300000);

            CASES.add(newCaseData);

            PROFILER.setVolunteersByRadius(newCaseData);

            //published to users in caseData object
            PUBLISHER.broadcastCaseData(newCaseData, true);
	}
        
        private void receivedArrivedAtCase( String msg ) {     
                EmrCaseData caseData = getCaseByCaseId( msg );
		if( caseData != null ) {
                    for(EmrVolunteer volunteer : caseData.getVolunteers()){
                        try {
                            sendMqttMessage( "server/" + volunteer.getClientId() + "/closeCase", caseData.toJson());
                        } catch (MqttException ex) {
                            System.out.println( "Exception in receivedArrivedAtCase() " + ex.getMessage() );
                        }
                    }
		}
	}

	private void receivedUpdateLocation( String msg, String clientId ) {
		EmrUser updateUser = new Gson().fromJson( msg, EmrUser.class );
		if( updateUser != null ) {
			updateUser.getLocation().setTimestamp( System.currentTimeMillis() );
			// update location in database
			Backend.getInstance().updateSingleUserField(
					Shared.USER_EMAIL, updateUser.getEmail(),
					Shared.USER_LOCATION, updateUser.getLocation() );
                        Backend.getInstance().updateSingleUserField(
					Shared.USER_EMAIL, updateUser.getEmail(),
					Shared.USER_LOCATION_HISTORY, updateUser.getLocationHistory());
			// broadcast this location if the user is involved in an active case
			for( EmrCaseData aCase : CASES ) {
				for( EmrVolunteer aVolunteer : aCase.getVolunteers() ) {
					if( aVolunteer.getClientId().equals( clientId ) ) {
						broadcastCaseData( aCase, false );
						refreshUIListeners( CaseReply.UPDATE_CASE, aCase );
						return;
					}
				}
			}
		}
	}

	private void receivedAcceptCase( String msg, String clientId ) {
		EmrCaseData caseData = getCaseByCaseId( msg );
		EmrUser acceptingUser = Backend.getInstance().findUserByClientId( clientId );
		if( caseData == null || acceptingUser == null ) {
			System.out.println( "CASE OR USER == NULL " + caseData + " / " + acceptingUser );
		} else {
			// add volunteer to caseData and broadcast the case
			EmrVolunteer volunteer = new EmrVolunteer();
			volunteer.setEmail( acceptingUser.getEmail() );
			volunteer.setLocation( acceptingUser.getLocation() );
			volunteer.setClientId( clientId );
			caseData.getVolunteers().add( volunteer );
			broadcastCaseData( caseData, false );

			refreshUIListeners( CaseReply.ACCEPT_CASE, caseData );
			System.out.println( "CASEDATA: " + caseData.toString() );
		}
	}

	private void receivedLogin( String msg ) {
		String loginResult = ServerReply.INVALID.toString();
		try {
			EmrUser loginUser = new Gson().fromJson( msg, EmrUser.class );
			if( Backend.getInstance().findUserByEmailAndPassword( loginUser.getEmail(), loginUser.getPassword() ) ) {
				User databaseUser = Backend.getInstance().findUserByEmail( loginUser.getEmail() );
				databaseUser.setClientId( loginUser.getClientId() );
				databaseUser.setLoginStatus( true );
				Backend.getInstance().saveUser( databaseUser );
				loginResult = (databaseUser.getLevel() > 0)
						? ServerReply.VALID_CONFIRMED.toString()
						: ServerReply.VALID_UNCONFIRMED.toString();
			}
			sendMqttMessage( "server/" + loginUser.getClientId() + "/serverMessage", loginResult );
		} catch( Exception ex ) {
			System.out.println( "Exception in messageArrived(/login) " + ex.getMessage() );
		}
		System.out.println( "*** /login *** users now: " + Backend.getInstance().getLogedInUsers().size() + " *** cases: " + CASES.size() );
	}

	private void receivedLogout( String msg ) {
		EmrUser logoutUser = new Gson().fromJson( msg, EmrUser.class );
		Backend.getInstance().updateSingleUserField(
				Shared.USER_EMAIL, logoutUser.getEmail(),
				Shared.USER_LOGINSTATUS, false );
		System.out.println( "*** /logout *** users now: " + Backend.getInstance().getLogedInUsers().size() + " *** cases: " + CASES.size() );
	}

	private void receivedRegistration( String msg ) {
		String registerResult = ServerReply.USER_EXISTS_ALREADY.toString();
		try {
			User registerUser = new Gson().fromJson( msg, User.class );
			// check if the requests email exists in database already
			if( Backend.getInstance().findUserByEmail( registerUser.getEmail() ) == null ) {
				Backend.getInstance().addUser( registerUser );
				registerResult = ServerReply.USER_REGISTERED.toString();
			}
			sendMqttMessage( "server/" + registerUser.getClientId() + "/serverMessage", registerResult );
		} catch( Exception ex ) {
			System.out.println( "Exception in messageArrived(/register) " + ex.getMessage() );
		}
	}

	public EmrCaseData getCaseByCaseId( String caseId ) {
		for( EmrCaseData aCase : CASES ) {
			if( aCase.getCaseId().equals( caseId ) ) {
				return aCase;
			}
		}
		System.out.println( "CASE NOT FOUND " + caseId );
		return null;
	}

//	public ArrayList<EmrCaseData> getCasesBySession( String sessionId ) {
//		ArrayList<EmrCaseData> caseList = new ArrayList<EmrCaseData>();
//
//		if( sessionId != null && CASES != null ) {
//			for( EmrCaseData aCase : CASES ) {
//				if( aCase.getCaseInitializerId().equals( sessionId ) ) {
//					caseList.add( aCase );
//				}
//			}
//		}
//		System.out.println( "Cases.size from Session [" + sessionId + "] = " + caseList.size() );
//		return caseList;
//	}

}
