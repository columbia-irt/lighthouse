package io.sece.vlc.trx;

import java.util.concurrent.TimeUnit;

public class Sleeper {
    private static final long BUSY_WAITING_THRESHOLD = TimeUnit.MILLISECONDS.toNanos(2);

    /* Spin-yield loop based alternative to Thread.sleep
     * Based on the code of Andy Malakov
     * http://andy-malakov.blogspot.fr/2010/06/alternative-to-threadsleep.html
     */
    public static void sleepNanos(long duration) throws InterruptedException {
        final long end = System.nanoTime() + duration;
        long left = duration;

        do {
            if (left > BUSY_WAITING_THRESHOLD)
                Thread.sleep(TimeUnit.NANOSECONDS.toMillis(left - BUSY_WAITING_THRESHOLD));

            if (Thread.interrupted())
                throw new InterruptedException();

            left = end - System.nanoTime();
        } while (left > 0);
    }


    public static void sleep(long duration, TimeUnit unit) throws InterruptedException {
        sleepNanos(unit.toNanos(duration));
    }
}
