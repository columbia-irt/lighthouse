package io.sece.vlc.trx;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.sece.vlc.Coordinate;


class Transmitter<T extends Coordinate> implements Runnable {
    private static final int QUEUE_SIZE = 8;
    private LEDInterface<T> led;
    private long interval;
    private ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);


    public Transmitter(LEDInterface<T> led, int interval, TimeUnit unit) {
        this.led = led;
        this.interval = unit.toNanos(interval);
    }


    public void enqueue(List<T> data) throws InterruptedException {
        queue.put(data);
    }


    @Override
    public void run() {
        List<T> data;
        long timestamp = System.nanoTime();

        try {
            while (true) {
                data = queue.take();
                for (T v : data) {
                    try {
                        led.set(v);
                    } catch (LEDException e) {
                        System.out.println(e);
                    }

                    timestamp += interval;
                    Sleeper.sleepNanos(timestamp - System.nanoTime());
                }
            }
        } catch (InterruptedException e) { }
    }
}
