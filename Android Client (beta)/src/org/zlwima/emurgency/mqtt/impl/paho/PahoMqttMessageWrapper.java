package org.zlwima.emurgency.mqtt.impl.paho;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.zlwima.emurgency.mqtt.impl.MqttException;
import org.zlwima.emurgency.mqtt.interfaces.IMqttMessage;

public class PahoMqttMessageWrapper implements IMqttMessage
{
	private MqttMessage message;
	
	public PahoMqttMessageWrapper(MqttMessage message)
	{
		this.message = message;
	}
	
	@Override
	public byte[] getPayload() throws MqttException
	{
		return message.getPayload();
	}

	@Override
	public int getQoS()
	{
		return message.getQos();
	}	
	
	public String toString(){
		return "PahoMqttMessageWrapper{"+message.toString()+"}";
	}

	@Override
	public boolean isRetained()
	{
		return message.isRetained();
	}

	@Override
	public boolean isDuplicate()
	{
		return message.isDuplicate();
	}
}
