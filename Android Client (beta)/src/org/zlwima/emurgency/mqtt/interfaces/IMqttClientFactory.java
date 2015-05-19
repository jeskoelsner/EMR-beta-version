package org.zlwima.emurgency.mqtt.interfaces;

import org.zlwima.emurgency.mqtt.impl.MqttException;

public interface IMqttClientFactory
{
	public IMqttClient create(String host, int port, String clientId, IMqttPersistence persistence) throws MqttException;
}
