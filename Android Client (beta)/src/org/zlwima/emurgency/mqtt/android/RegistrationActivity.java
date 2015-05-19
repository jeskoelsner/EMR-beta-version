package org.zlwima.emurgency.mqtt.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import org.zlwima.emurgency.mqtt.R;
import org.zlwima.emurgency.mqtt.android.config.Validator;
import org.zlwima.emurgency.backend.model.EmrUser;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate;
import org.zlwima.emurgency.mqtt.android.config.Base;
import org.zlwima.emurgency.mqtt.service.MqttService;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusHandler;

import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;

public class RegistrationActivity extends Activity implements MqttMessageHandler, MqttStatusHandler, OnFocusChangeListener {
	private MqttServiceDelegate.MqttMessageReceiver msgReceiver;
	private MqttServiceDelegate.MqttStatusReceiver statusReceiver;

	private EditText emailForm;
	private EditText firstNameForm;
	private EditText lastNameForm;
	private EditText passwordForm;
	private EditText passcheckForm;
	private Button submitButton;

	private Resources resources;
	private Builder messageDialog;
	private ProgressDialog progressDialog;

	//private RestReceiver restReceiver;
	private Validator validate;

	/**
	 * First creation of the screen when activity starts. Devices with hardware
	 * keyboard will call onCreate if keyboard is opened/closed
	 */
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.screen_registration );

		emailForm = (EditText) findViewById( R.id.regEmail );
		firstNameForm = (EditText) findViewById( R.id.regFirstName );
		lastNameForm = (EditText) findViewById( R.id.regLastName );
		passwordForm = (EditText) findViewById( R.id.regPass );
		passcheckForm = (EditText) findViewById( R.id.regPassValid );

		// init validator & restreceiver
		validate = new Validator( this );
		//restReceiver = new RestReceiver(this);

		// get stringressources & prebuild message
		resources = getResources();
		messageDialog = new AlertDialog.Builder( this );
		progressDialog = new ProgressDialog( this );
		progressDialog.setMessage( resources.getString( R.string.registration_loading ) );

		// set focuslisteners for validation checks
		emailForm.setOnFocusChangeListener( this );
		firstNameForm.setOnFocusChangeListener( this );
		lastNameForm.setOnFocusChangeListener( this );
		passwordForm.setOnFocusChangeListener( this );
		passcheckForm.setOnFocusChangeListener( this );

		submitButton = (Button) findViewById( R.id.buttonRegister );
		submitButton.setOnClickListener( new View.OnClickListener() {
			public void onClick( View v ) {
				progressDialog.show();
				EmrUser registerUser = new EmrUser();
				registerUser.setEmail( emailForm.getText().toString() );
				registerUser.setPassword( passwordForm.getText().toString() );
				registerUser.setClientId( APPLICATION.getAndroidClientId() );
				APPLICATION.registration( registerUser );
			}
		} );

		bindStatusReceiver();
		bindMessageReceiver();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent( getApplicationContext(), LoginActivity.class );
		startActivity( intent );
		finish();
	}

	@Override
	public void onDestroy() {
		Base.log( "onDestroy (LoginActivity)" );
		unbindMessageReceiver();
		unbindStatusReceiver();
		validate = null;
		progressDialog = null;
		messageDialog = null;
		super.onDestroy();
	}

	private void showMessageDialog( String title, String text, String buttonText, DialogInterface.OnClickListener listener ) {
		messageDialog.setTitle( title );
		messageDialog.setMessage( text );
		messageDialog.setPositiveButton( buttonText, listener );
		messageDialog.show();
	}

	public void onFocusChange( View view, boolean hasFocus ) {
		// TODO: do complete validation on each focus
		if( !hasFocus ) {
			submitButton.setEnabled( true );
			EditText target = (EditText) view;
			int id = target.getId();
			if( id == R.id.regEmail ) {
				if( !validate.checkEmail( target.getText().toString() ) ) {
					target.setError( validate.wrongEmail() );
					submitButton.setEnabled( false );
				} else {
					target.setError( null );
				}
			}
			if( id == R.id.regFirstName ) {
				if( !validate.checkFirstName( target.getText().toString() ) ) {
					target.setError( validate.wrongFirstName() );
					submitButton.setEnabled( false );
				} else {
					target.setError( null );
				}
			}
			if( id == R.id.regLastName ) {
				if( !validate.checkLastName( target.getText().toString() ) ) {
					target.setError( validate.wrongLastName() );
					submitButton.setEnabled( false );
				} else {
					target.setError( null );
				}
			}
			if( id == R.id.regPass ) {
				if( !validate.checkPassword( target.getText().toString() ) ) {
					target.setError( validate.wrongPassword() );
					submitButton.setEnabled( false );
				} else {
					target.setError( null );
				}
			}
			if( id == R.id.regPassValid ) {
				if( !validate.samePasswords( target.getText().toString(),
						passwordForm.getText().toString() ) ) {
					target.setError( validate.wrongPasswordCheck() );
					submitButton.setEnabled( false );
				} else {
					target.setError( null );
				}
			}
		}
	}

	private void bindMessageReceiver() {
		msgReceiver = new MqttServiceDelegate.MqttMessageReceiver();
		msgReceiver.registerHandler( this );
		registerReceiver( msgReceiver, new IntentFilter( MqttService.MQTT_MSG_RECEIVED_INTENT ) );
	}

	private void unbindMessageReceiver() {
		if( msgReceiver != null ) {
			msgReceiver.unregisterHandler( this );
			unregisterReceiver( msgReceiver );
			msgReceiver = null;
		}
	}

	private void bindStatusReceiver() {
		statusReceiver = new MqttServiceDelegate.MqttStatusReceiver();
		statusReceiver.registerHandler( this );
		registerReceiver( statusReceiver, new IntentFilter( MqttService.MQTT_STATUS_INTENT ) );
	}

	private void unbindStatusReceiver() {
		if( statusReceiver != null ) {
			statusReceiver.unregisterHandler( this );
			unregisterReceiver( statusReceiver );
			statusReceiver = null;
		}
	}

	@Override
	public void handleMessage( String topic, byte[] payload ) {
		Base.log( "handleMessage() RegistrationActivity: topic=" + topic + ", message=" + payload );
		if( topic.equals( "server/" + APPLICATION.getAndroidClientId() + "/serverMessage" ) ) {
			progressDialog.dismiss();

			String status = new String( payload );
			if( status.equals( MqttServiceDelegate.ServerReply.USER_EXISTS_ALREADY.toString() ) ) {
				DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int id ) {
						dialog.dismiss();
					}
				};
				showMessageDialog(
						resources.getString( R.string.registration_title ),
						resources.getString( R.string.registration_exists ),
						resources.getString( R.string.button_positive_ok ),
						clickListener );

			} else if( status.equals( MqttServiceDelegate.ServerReply.USER_REGISTERED.toString() ) ) {
				DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int id ) {
						dialog.dismiss();
						onBackPressed();
					}
				};
				showMessageDialog(
						resources.getString( R.string.registration_title ),
						resources.getString( R.string.registration_success ),
						resources.getString( R.string.button_positive_ok ),
						clickListener );
			}
		}
	}

	@Override
	public void handleStatus( MqttService.ConnectionStatus status, String reason ) {
		Base.log( "handleStatus() RegistrationActivity: status=" + status + ", reason=" + reason );
		submitButton.setEnabled( status == MqttService.ConnectionStatus.CONNECTED );
	}

}
