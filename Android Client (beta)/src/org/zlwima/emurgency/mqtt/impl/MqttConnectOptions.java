package org.zlwima.emurgency.mqtt.impl;

import org.zlwima.emurgency.mqtt.interfaces.IMqttConnectOptions;

public class MqttConnectOptions implements IMqttConnectOptions
{
	@Override
	public void setCleanSession(boolean cleanStart)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKeepAliveInterval(short keepAliveSeconds)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserName(String username)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPassword(char[] password)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getCleanSession()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getKeepAliveInterval()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUserName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getPassword()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
