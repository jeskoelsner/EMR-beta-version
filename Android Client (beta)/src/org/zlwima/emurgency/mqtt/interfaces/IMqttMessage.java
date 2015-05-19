package org.zlwima.emurgency.mqtt.interfaces;

import org.zlwima.emurgency.mqtt.impl.MqttException;

public interface IMqttMessage
{
	public int getQoS();
	public byte[] getPayload() throws MqttException;
	public boolean isRetained();	
	public boolean isDuplicate();
}
