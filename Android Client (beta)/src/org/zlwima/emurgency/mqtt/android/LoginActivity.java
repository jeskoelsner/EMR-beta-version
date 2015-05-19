package org.zlwima.emurgency.mqtt.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.zlwima.emurgency.mqtt.android.config.SharedPrefs;
import org.zlwima.emurgency.mqtt.android.ui.ApplicationDialog;
import org.zlwima.emurgency.mqtt.android.ui.FontHelper;
import org.zlwima.emurgency.backend.model.EmrUser;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.ServerReply;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageReceiver;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusReceiver;
import org.zlwima.emurgency.mqtt.R;
import org.zlwima.emurgency.mqtt.android.config.Base;
import org.zlwima.emurgency.mqtt.service.MqttService;

import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;

public class LoginActivity extends Activity implements OnClickListener, MqttMessageHandler, MqttStatusHandler {
	private MqttMessageReceiver msgReceiver;
	private MqttStatusReceiver statusReceiver;

	private Button buttonLogin;
	private Button buttonRegister;
	private CheckBox checkSave;
	private TextView inputEmail;
	private TextView inputPassword;
	private ProgressDialog loadingDialog;
	private SharedPrefs sharedPrefs;

	private ApplicationDialog dialog;

	private final DialogInterface.OnClickListener exitApplicationDialogListener = new DialogInterface.OnClickListener() {
		public void onClick( DialogInterface dialog, int id ) {
			switch( id ) {
				case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					APPLICATION.closeConnection();
					finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
			}
		}
	};

	private final DialogInterface.OnClickListener loginFailedDialogListener = new DialogInterface.OnClickListener() {
		public void onClick( DialogInterface dialog, int id ) {
			switch( id ) {
				case DialogInterface.BUTTON_NEUTRAL:
					dialog.dismiss();
					break;
			}
		}
	};

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		setContentView( R.layout.screen_login );

		// Globally add 'droid sans' font since different fonts break layout...
		FontHelper fHelper = new FontHelper( this );
		fHelper.applyCustomFont( (RelativeLayout) findViewById( R.id.loginRoot ) );

		// Hold instance of logged in user with Shared Prefs
		sharedPrefs = new SharedPrefs( getApplicationContext() );

		inputEmail = (TextView) findViewById( R.id.formEmail );
		inputPassword = (TextView) findViewById( R.id.formPassword );
		checkSave = (CheckBox) findViewById( R.id.formCheckSave );
		buttonLogin = (Button) findViewById( R.id.buttonLogin );
		buttonRegister = (Button) findViewById( R.id.buttonRegister );

		// Autofill form
		checkSave.setChecked( sharedPrefs.getAutoFill() );
		inputEmail.setText( sharedPrefs.getEmail() );

		buttonLogin.setOnClickListener( this );
		buttonRegister.setOnClickListener( this );

		dialog = new ApplicationDialog( this );
		loadingDialog = dialog.progressDialog( R.string.login_loading_title, R.string.login_loading_message );

		bindStatusReceiver();
		bindMessageReceiver();
		MqttServiceDelegate.startService( this );
	}

	@Override
	public void onBackPressed() {
		dialog.messageDialog( R.string.dialog_quit_title, R.string.dialog_quit_message, ApplicationDialog.MESSAGE_CHOICE, exitApplicationDialogListener ).show();
	}

	public void onClick( View view ) {
		Base.log( "LOGINACTIVITY onLick() " + view.getId() );
		if( view.getId() == R.id.buttonLogin ) {
			if( inputEmail.getText().toString().trim().equals( "" ) ) {
				return;
			}
			loadingDialog.show();
                        
                        //BACKDOOR
                        if(     inputEmail.getText().toString().trim().equals( "smsadmin" ) && 
                                inputPassword.getText().toString().equals( "smspass" )  ){
                            Intent intent = new Intent( getApplicationContext(), SmsActivity.class );
                            startActivity( intent );
                            finish();
                        };
                        
			EmrUser loginUser = new EmrUser();
			loginUser.setEmail( inputEmail.getText().toString() );
			loginUser.setPassword( inputPassword.getText().toString() );
			loginUser.setClientId( APPLICATION.getAndroidClientId() );
			APPLICATION.login( loginUser );
		} else if( view.getId() == R.id.buttonRegister ) {
			Intent intent = new Intent( getApplicationContext(), RegistrationActivity.class );
			startActivity( intent );
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		Base.log( "onDestroy (LoginActivity)" );
		unbindMessageReceiver();
		unbindStatusReceiver();
		super.onDestroy();
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
            Base.log( "handleMessage() LoginActivity: topic=" + topic + ", message=" + payload );

            if( topic.equals( "server/" + APPLICATION.getAndroidClientId() + "/serverMessage" ) ) {
                loadingDialog.dismiss();

                String status = new String( payload );
                if( status.equals( ServerReply.INVALID.toString() ) ) {
                        dialog.messageDialog( R.string.login_error, R.string.login_invalid_message, ApplicationDialog.MESSAGE_NEUTRAL, loginFailedDialogListener ).show();
                } else if( status.equals( ServerReply.VALID_CONFIRMED.toString() ) || status.equals( ServerReply.VALID_UNCONFIRMED.toString() ) ){
                        if( checkSave.isChecked() ) {
                                sharedPrefs.setEmail( inputEmail.getText().toString() );
                        }
                        sharedPrefs.setAutoFill( checkSave.isChecked() );

                        APPLICATION.USER.setEmail( inputEmail.getText().toString() );
                        APPLICATION.USER.setPassword( inputPassword.getText().toString() );
                        APPLICATION.USER.setClientId( APPLICATION.getAndroidClientId() );
                        APPLICATION.USER.setLevel( status.equals( ServerReply.VALID_CONFIRMED.toString() ) ? 8 : 5 );

                        Intent intent = new Intent( this, DashboardActivity.class );
                        intent.putExtra("version", "1.0"); //TODO get version number
                        startActivity( intent );
                        finish();
                }
            }
	}

	@Override
	public void handleStatus( MqttService.ConnectionStatus status, String reason ) {
		Base.log( "handleStatus() LoginActivity: status=" + status + ", reason=" + reason );
		buttonLogin.setEnabled( status == MqttService.ConnectionStatus.CONNECTED );
	}

}
