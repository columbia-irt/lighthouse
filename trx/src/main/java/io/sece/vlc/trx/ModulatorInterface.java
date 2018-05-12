package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIOException;
import java.awt.Color;


public interface ModulatorInterface <T> {
    //public Color modulate(T symbol);
    public void run() throws PiGPIOException, InterruptedException;
}
