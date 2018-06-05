package io.sece.vlc.trx;

import io.sece.pigpio.PiGPIOPin;

public class watchDog implements Runnable {

    private Thread thread;
    private int timeout;


    public watchDog(Thread thread, int timeout)
    {
        this.thread = thread;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            PiGPIOPin r = new PiGPIOPin(22);
            PiGPIOPin g = new PiGPIOPin(27);
            PiGPIOPin b = new PiGPIOPin(17);
            PiRgbLED   led = new PiRgbLED(r, g, b);
            Thread.sleep(timeout * 1000);
            if(thread != null && thread.isAlive()) {
                thread.stop();
            }
            led.set(io.sece.vlc.Color.BLACK);

        }
        catch(Exception e)
        {
            System.out.println(e.fillInStackTrace());
        }
    }
}
