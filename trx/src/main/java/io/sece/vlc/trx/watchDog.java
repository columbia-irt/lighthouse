package io.sece.vlc.trx;

import java.awt.Color;

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
            Thread.sleep(timeout * 1000);
            if(thread.isAlive()) {
                thread.stop();
            }

        }
        catch(InterruptedException e)
        {
            System.out.println(e.fillInStackTrace());
        }
    }
}
