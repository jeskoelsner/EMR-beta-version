package org.zlwima.emurgency.mqtt.impl.paho;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import org.zlwima.emurgency.mqtt.impl.MqttException;
import org.zlwima.emurgency.mqtt.impl.MqttPersistenceException;
import org.zlwima.emurgency.mqtt.interfaces.IMqttCallback;
import org.zlwima.emurgency.mqtt.interfaces.IMqttClient;
import org.zlwima.emurgency.mqtt.interfaces.IMqttConnectOptions;
import org.zlwima.emurgency.mqtt.interfaces.IMqttMessage;
import org.zlwima.emurgency.mqtt.interfaces.IMqttTopic;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.zlwima.emurgency.mqtt.android.config.Base;

public class PahoMqttClientWrapper implements IMqttClient
{
	private static final String TOPIC_PING = "PING";	
	
	private MqttClient client;
	
	public PahoMqttClientWrapper(String serverURI, String clientId, 
		MqttClientPersistence persistence) throws MqttException
	{
		Base.log("init(serverURI="+serverURI+", clientId="+clientId+", persistence="+persistence+")");
		
		try
		{
			this.client = new MqttClient(serverURI, clientId, persistence);
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}

	@Override
	public void setCallback(final IMqttCallback callback) throws MqttException
	{		
		Base.log("setCallback(callback="+callback+")");
		//try
		//{
			this.client.setCallback(new MqttCallback()
			{			

				@Override
				public void messageArrived(String topic, MqttMessage message)
					throws Exception
				{
					callback.messageArrived(
						new PahoMqttTopicWrapper(topic), 
						new PahoMqttMessageWrapper(message));
				}
				
				@Override
				public void deliveryComplete(IMqttDeliveryToken token)
				{
					Base.log("deliveryComplete: "+token);
				}
				
				@Override
				public void connectionLost(Throwable throwable)
				{
					callback.connectionLost(throwable);
				}

			});
		//}
	}

	@Override
	public void subscribe(IMqttTopic topic) throws IllegalArgumentException,
		MqttException
	{
		Base.log("subscribe(topic="+topic+")");
		subscribe(new IMqttTopic[]{topic});
	}

	@Override
	public void subscribe(IMqttTopic[] topics) throws IllegalArgumentException,
		MqttException
	{
		Base.log("subscribe(topics="+topics+")");
		
		int amount = topics.length;
		
		String[] topicarray = new String[amount];
		int[] prioarray = new int[amount];
		
		for(int i = 0; i < amount; i++){
			topicarray[i] = topics[i].getName();
			prioarray[i] = topics[i].getQoS();
		}
		
		try
		{
			this.client.subscribe(topicarray, prioarray);
		}
		catch (MqttSecurityException e)
		{
			e.printStackTrace();
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}
	
	@Override
	public void publish(IMqttTopic topic, IMqttMessage message)
		throws MqttException
	{
		Base.log("publish(topic="+topic+", message="+message+")");
		
		MqttTopic t = this.client.getTopic(topic.getName());
		
		MqttMessage m = new MqttMessage();
		m.setRetained(message.isRetained());
		m.setQos(message.getQoS());	
		m.setPayload(message.getPayload());		
				
		try
		{
			t.publish(m);
		}
		catch (org.eclipse.paho.client.mqttv3.MqttPersistenceException e)
		{
			e.printStackTrace();
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}

	@Override
	public boolean isConnected()
	{		
		return this.client.isConnected();
	}

	@Override
	public void connect(IMqttConnectOptions options) throws MqttException
	{
		if(this.client.isConnected()){
			try
			{
				disconnect();
			}
			catch (MqttPersistenceException e)
			{
				e.printStackTrace();
				return;
			}
		}
		
		Base.log("connect(options="+options+")");
		
		MqttConnectOptions o = new MqttConnectOptions();
		o.setCleanSession(options.getCleanSession());		
		o.setKeepAliveInterval(options.getKeepAliveInterval());
		o.setUserName(options.getUserName());
		o.setPassword(options.getPassword());
		
		try
		{
			this.client.connect(o);
		}
		catch (MqttSecurityException e)
		{
			e.printStackTrace();
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}

	@Override
	public void disconnect() throws MqttException, MqttPersistenceException
	{
		if(!this.client.isConnected())return;
		
		try
		{
			this.client.disconnect();
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}

	@Override
	public void ping() throws MqttException
	{
		MqttTopic topic = this.client.getTopic(TOPIC_PING);
		
		MqttMessage message = new MqttMessage();
		message.setRetained(false);
		message.setQos(1);	
		message.setPayload(new byte[]{0});
		
		try
		{
			topic.publish(message);
		}
		catch (org.eclipse.paho.client.mqttv3.MqttPersistenceException e)
		{
			e.printStackTrace();
		}
		catch (org.eclipse.paho.client.mqttv3.MqttException e)
		{
			throw new MqttException(e);
		}
	}
}
