package org.zlwima.emurgency.mqtt;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.zlwima.emurgency.mqtt.service.MqttService;
import org.zlwima.emurgency.mqtt.service.MqttService.ConnectionStatus;

public class MqttServiceDelegate {
	
	public enum ServerReply {
		INVALID, VALID_CONFIRMED, VALID_UNCONFIRMED, USER_EXISTS_ALREADY, USER_REGISTERED
	}
		
	public interface MqttMessageHandler {
		public void handleMessage( String topic, byte[] payload );
	}

	public interface MqttStatusHandler {
		public void handleStatus( ConnectionStatus status, String reason );
	}

	public static void startService( Context context ) {
		Intent svc = new Intent( context, MqttService.class );
		context.startService( svc );
	}

	public static void stopService( Context context ) {
		Intent svc = new Intent( context, MqttService.class );
		context.stopService( svc );
	}
	
	public static void publish( Context context, String topic, byte[] payload ) {
		Intent actionIntent = new Intent( context, MqttService.class );
		actionIntent.setAction( MqttService.MQTT_PUBLISH_MSG_INTENT );
		actionIntent.putExtra( MqttService.MQTT_PUBLISH_MSG_TOPIC, topic );
		actionIntent.putExtra( MqttService.MQTT_PUBLISH_MSG, payload );
		context.startService( actionIntent );
	}

	public static class MqttStatusReceiver extends BroadcastReceiver {
		private final List<MqttStatusHandler> statusHandlers = new ArrayList<MqttStatusHandler>();

		public void registerHandler( MqttStatusHandler handler ) {
			if( !statusHandlers.contains( handler ) ) {
				statusHandlers.add( handler );
			}
		}

		public void unregisterHandler( MqttStatusHandler handler ) {
			if( statusHandlers.contains( handler ) ) {
				statusHandlers.remove( handler );
			}
		}

		public void clearHandlers() {
			statusHandlers.clear();
		}

		public boolean hasHandlers() {
			return statusHandlers.size() > 0;
		}

		@Override
		public void onReceive( Context context, Intent intent ) {
			Bundle notificationData = intent.getExtras();

			ConnectionStatus statusCode
					= ConnectionStatus.class.getEnumConstants()[notificationData.getInt( MqttService.MQTT_STATUS_CODE )];

			String statusMsg = notificationData.getString( MqttService.MQTT_STATUS_MSG );

			for( MqttStatusHandler statusHandler : statusHandlers ) {
				statusHandler.handleStatus( statusCode, statusMsg );
			}
		}
	}

	public static class MqttMessageReceiver extends BroadcastReceiver {
		private final List<MqttMessageHandler> messageHandlers = new ArrayList<MqttMessageHandler>();

		public void registerHandler( MqttMessageHandler handler ) {
			if( !messageHandlers.contains( handler ) ) {
				messageHandlers.add( handler );
			}
		}

		public void unregisterHandler( MqttMessageHandler handler ) {
			if( messageHandlers.contains( handler ) ) {
				messageHandlers.remove( handler );
			}
		}

		public void clearHandlers() {
			messageHandlers.clear();
		}

		public boolean hasHandlers() {
			return messageHandlers.size() > 0;
		}

		@Override
		public void onReceive( Context context, Intent intent ) {
			Bundle notificationData = intent.getExtras();
			String topic = notificationData.getString( MqttService.MQTT_MSG_RECEIVED_TOPIC );
			byte[] payload = notificationData.getByteArray( MqttService.MQTT_MSG_RECEIVED_MSG );

			for( MqttMessageHandler messageHandler : messageHandlers ) {
				messageHandler.handleMessage( topic, payload );
			}
		}
	}
}
