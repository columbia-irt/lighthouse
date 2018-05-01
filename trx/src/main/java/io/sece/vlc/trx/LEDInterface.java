package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;

interface LEDInterface
{
	void setIntensity(boolean onoff);
	void setIntensity(int value);
	void setColor(int red, int green, int blue);
}
