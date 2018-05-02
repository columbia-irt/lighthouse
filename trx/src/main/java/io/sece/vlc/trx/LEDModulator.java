package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIO;
import io.sece.pigpio.PiGPIOException;

interface LEDModulator
{
    void setSymbols(String symbols) throws PiGPIOException;
}