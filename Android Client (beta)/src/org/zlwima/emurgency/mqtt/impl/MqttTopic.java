package org.zlwima.emurgency.mqtt.impl;

import org.zlwima.emurgency.mqtt.interfaces.IMqttTopic;

public class MqttTopic implements IMqttTopic
{
	private String name;
	private int qos;
	
	public MqttTopic(String name){
		setName(name);
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public int getQoS()
	{
		return this.qos;
	}
	
	public void setQoS(int qos)
	{
		this.qos = qos;
	}	
}
