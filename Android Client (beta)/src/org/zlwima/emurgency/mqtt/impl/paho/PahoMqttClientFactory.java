package org.zlwima.emurgency.mqtt.impl.paho;

import org.zlwima.emurgency.mqtt.impl.MqttException;
import org.zlwima.emurgency.mqtt.interfaces.IMqttClient;
import org.zlwima.emurgency.mqtt.interfaces.IMqttClientFactory;
import org.zlwima.emurgency.mqtt.interfaces.IMqttPersistence;

public class PahoMqttClientFactory implements IMqttClientFactory
{	
	@Override
	public IMqttClient create(String host, int port, String clientId,
		IMqttPersistence persistence) throws MqttException
	{
		PahoMqttClientPersistence persistenceImpl = null;
		if(persistence != null){
			persistenceImpl = new PahoMqttClientPersistence(persistence);
		}
		
		// TODO Auto-generated method stub
		return new PahoMqttClientWrapper(
			"tcp://"+host+":"+port, clientId, persistenceImpl);
	}
}
