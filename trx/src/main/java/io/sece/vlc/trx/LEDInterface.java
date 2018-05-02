package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

interface LEDInterface
{
	void setIntensity(boolean off) throws PiGPIOException;
	void setIntensity(int value) throws PiGPIOException;
	void setColor(int red, int green, int blue) throws PiGPIOException;
}
