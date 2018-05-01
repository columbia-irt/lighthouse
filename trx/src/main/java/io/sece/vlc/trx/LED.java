package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;

interface LEDInterface
{
	void setIntensity(boolean onoff);
	void setIntensity(int value);
	void setColor(int red, int green, int blue);
}

class TriColorLED implements LEDInterface
{
	
	public void setIntensity(boolean onoff)
	{
		throw new UnsupportedOperationException();
	}
	
	public void setIntensity(int value)
	{
		throw new UnsupportedOperationException();
	}
	
	public void setColor(int red, int green, int blue)
	{
		try
		{
			PiGPIO.gpioPWM(22, red);
			PiGPIO.gpioPWM(27, green);
			PiGPIO.gpioPWM(17, blue);
		}
		catch(Exception e)
		{
			System.out.println("error in setColor: " + e);
		}
	}
}
