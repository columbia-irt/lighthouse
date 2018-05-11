package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;
import java.awt.Color;

interface LEDInterface
{
	void setIntensity(boolean off) throws PiGPIOException;
	void setIntensity(int value) throws PiGPIOException;
	void setColor(Color color) throws PiGPIOException;
}
