package io.sece.vlc.trx;


import io.sece.vlc.Color;

public class WatchDog implements Runnable {
    private Thread thread;
    private int timeout;
    private ColorLEDInterface led;


    public WatchDog(Thread thread, int timeout, ColorLEDInterface led) {
        this.thread = thread;
        this.timeout = timeout;
        this.led = led;
    }


    @Override
    public void run() {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            return;
        }

        System.out.println("Watchdog triggered");

        if (thread == null || !thread.isAlive()) return;

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            led.set(Color.BLACK);
        }
    }
}
