package org.zlwima.emurgency.mqtt.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import org.zlwima.emurgency.backend.model.EmrUser;
import static org.zlwima.emurgency.mqtt.MqttApplication.APPLICATION;

import org.zlwima.emurgency.mqtt.R;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttMessageHandler;
import org.zlwima.emurgency.mqtt.MqttServiceDelegate.MqttStatusHandler;
import org.zlwima.emurgency.mqtt.android.config.Base;
import org.zlwima.emurgency.mqtt.receiver.SmsReceiver;
import org.zlwima.emurgency.mqtt.service.MqttService;

public class SmsActivity extends Activity implements MqttMessageHandler, MqttStatusHandler {
	private MqttServiceDelegate.MqttMessageReceiver msgReceiver;
	private MqttServiceDelegate.MqttStatusReceiver statusReceiver;

	private TextView statusText;
        private EmrUser tmpUser;

	private Resources resources;

        private SmsReceiver rec;
        
	/**
	 * First creation of the screen when activity starts. Devices with hardware
	 * keyboard will call onCreate if keyboard is opened/closed
	 */
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.screen_sms );

		statusText = (TextView) findViewById( R.id.status_text );
                
                rec = new SmsReceiver();
                IntentFilter filter = new IntentFilter( "android.provider.Telephony.SMS_RECEIVED" );
                
                registerReceiver( rec, filter );

		// get stringressources & prebuild message
		resources = getResources();
                bindMessageReceiver();
                bindStatusReceiver();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent( getApplicationContext(), LoginActivity.class );
		startActivity( intent );
		finish();
	}

	@Override
	public void onDestroy() {
		Base.log( "onDestroy (SmsActivity)" );
                unregisterReceiver( rec );
                rec.abortBroadcast();
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
                Base.log( "handleMessage() SmsActivity: topic=" + topic + ", message=" + payload );
		//statusText.setText("Status: " + topic + ": " + payload);
                //TODO bubble replies
	}

	@Override
	public void handleStatus( MqttService.ConnectionStatus status, String reason ) {
		Base.log( "handleStatus() SmsActivity: status=" + status + ", reason=" + reason );
		statusText.setText("Status: " + status + ": " + reason);
	}

}
