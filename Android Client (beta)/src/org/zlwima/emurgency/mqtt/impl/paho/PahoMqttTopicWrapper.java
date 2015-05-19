package org.zlwima.emurgency.mqtt.impl.paho;

import org.eclipse.paho.client.mqttv3.MqttTopic;

import org.zlwima.emurgency.mqtt.interfaces.IMqttTopic;

public class PahoMqttTopicWrapper implements IMqttTopic
{
	private String topic;
	public PahoMqttTopicWrapper(String topic)
	{
		this.topic = topic;
	}

	@Override
	public String getName()
	{
		return topic;
	}

	@Override
	public int getQoS()
	{
		return 0;
	}
}
