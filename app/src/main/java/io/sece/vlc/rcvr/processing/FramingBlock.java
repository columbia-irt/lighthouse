package io.sece.vlc.rcvr.processing;

import java.util.ArrayList;

import io.sece.vlc.rcvr.ViewfinderModel;

/**
 * Created by alex on 7/9/18.
 *
 *  This block aims to detect each frame by a startingsequence presubmitted to the data and a checksum/paritybit to check validity of the
 *  received data positioned in the end of the frame
 *
 *  For each incoming symbol the receiving state determines the next expected symbol and is being increased or reset by following symbols,
 *  if the state equals the size of the startingsequence the boolean variable frameInProgress is set to true and all symbols are being stored
 *  in receivedBits
 *
 *  Using the receivingBitCounter one can determine if the frame is completely sent and in which position the checksum/paritybit is
 */

public class FramingBlock {

    private ArrayList<String> startingSequence;
    private int receivingState = 0;
    private int dataAmount;
    private int receivingBitCounter = 0;
    private boolean frameInProgress = false;
    private String[] receivedBits;

    private int tailCheckingBits = 0;

    public FramingBlock (String startingSequence, int offset, int interval){
        this.startingSequence = new ArrayList<>();
        for (int i= 0 + offset; i < startingSequence.length() + offset; i+= offset){
            this.startingSequence.add(startingSequence.substring(i -offset, i));
        }

        dataAmount =  12 - this.startingSequence.size() - tailCheckingBits; // 24 Bits to transfer
        //(int)((interval / (1000 / ViewfinderModel.synced_fps)));
        receivedBits = new String[dataAmount + tailCheckingBits];
        System.out.println("startingsequence " + this.startingSequence.toString()  + " dataAmount " + dataAmount);
    }


    public String[] apply (String symbol){
        if(!frameInProgress){
            frameInProgress = checkStartingSequence(symbol);
        }else{
            receivedBits[receivingBitCounter] = symbol;
            receivingBitCounter ++;
            if(receivingBitCounter == dataAmount + tailCheckingBits){
                receivingBitCounter = 0;
                receivingState = 0;
                frameInProgress = false;
                return receivedBits;
            }
        }
        return null;
    }

    private boolean checkStartingSequence(String symbol){
        if(symbol.equals(startingSequence.get(receivingState))){
            receivingState += 1;
        }else{
            receivingState = 0;
            if(symbol.equals(startingSequence.get(0))){
                receivingState = 1;
            }
        }
        System.out.println("state: " + receivingState);
        return receivingState == (startingSequence.size());
    }



}
