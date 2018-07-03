package io.sece.vlc.rcvr;



/**
 * Created by alex on 6/26/18.
 * <p>
 * The Synchronization Procedure is run at the fastest possible FPS Rate.
 * <p>
 * This module is in charge to check if the incoming symbols
 * fit to the given starting sequence.
 * Furthermore it automatically calculates the timestamp for
 * starting the transmission. Because there is a change between every symbol
 * in the starting sequence, a good timestamp to start transmission is the point of time
 * when there are two incoming values representing the same symbol.
 * If there are some more incoming values which are different from the previous ones, there is
 * to add the specified delay times each symbol after the double sampled ones.
 *
 * TODO: All timestamps have to be replaces with the ones created by ReceiverClass
 */

public class SynchronizationModule {

    private int delay;
    private String prevSymbol = "-1";

    private int symbolCounter = 1;
    private int symbolsCounterAfter = 1;

    private long timestamp;
    private long timestampCalculated;

    private String startingSequence;
    public static String receivedStartingSequence = "";

    public SynchronizationModule(String startingSequence, int delay) {
        this.startingSequence = startingSequence;
        this.delay = delay;
    }

    public boolean symbolReceived(String currSymbol) {
        if (receivedStartingSequence.length() > startingSequence.length()) {
            receivedStartingSequence = receivedStartingSequence.substring(receivedStartingSequence.length() - startingSequence.length(), receivedStartingSequence.length());
        }

        if (prevSymbol.equals(currSymbol)) {
            symbolsCounterAfter = 1;
            symbolCounter++;
            timestamp += System.currentTimeMillis();
        } else {
            receivedStartingSequence += currSymbol;
            if (symbolCounter > 1) {
                timestampCalculated = timestamp / symbolCounter;
            }
            symbolsCounterAfter++;
            symbolCounter = 1;
            timestamp = System.currentTimeMillis();
        }
        System.out.println(System.currentTimeMillis() + " Received Starting Sequence " + receivedStartingSequence.length() + " " + receivedStartingSequence);

        prevSymbol = currSymbol;
        if (receivedStartingSequence.equals(startingSequence)) {
            long timeToStart = timestampCalculated + ((1000/ delay) * symbolsCounterAfter) + delay;
            System.out.println("TimeToStart: " + timeToStart);
            ViewfinderFragment.timeToStartSynchronized = timeToStart;
            return true;
        }
        return false;
    }
}
