package io.sece.vlc.rcvr.processing;

import java.util.ArrayList;

import io.sece.vlc.DataFrame;
import io.sece.vlc.rcvr.ViewfinderModel;

/**
 * Created by alex on 7/9/18.
 *
 *  This block aims to detect each frame by a startingsequence presubmitted
 *
 *  For each incoming symbol the receiving state determines the next expected symbol and is being increased or reset by following symbols
 *
 */

public class FramingBlock {

    private ArrayList<String> startingSequence;
    private int receivingState = 0;

    private String received = "";
    private boolean framingStarted = false;

    private int dataAmount = 24;

    public FramingBlock (String startingSequence, int offset, int interval){
        this.startingSequence = new ArrayList<>();
        for (int i= 0 + offset; i < startingSequence.length() + offset; i+= offset){
            this.startingSequence.add(startingSequence.substring(i -offset, i));
        }
        dataAmount = dataAmount - (offset * this.startingSequence.size());
//        dataAmount =  (int)((interval / (1000 / ViewfinderModel.synced_fps)));
//        calculate the amount of bits in specific periode of time

        System.out.println("startingsequence " + this.startingSequence.toString() + " bits for actual data: " + dataAmount);
    }


    public DataFrame apply (String symbol){
        if(!checkStartingSequence(symbol)){
            if(framingStarted){
                received += symbol;
            }
        }else{
            framingStarted = true;
            String res =  received;
            received = "";
            receivingState= 0;
            return new DataFrame(res, dataAmount);
        }
        return null;
    }

    private boolean checkStartingSequence(String symbol){
        if(symbol.equals(startingSequence.get(receivingState))){
            receivingState += 1;
            System.out.println("state: " + receivingState);
        }else{
            receivingState = 0;
        }
        return receivingState == (startingSequence.size());
    }


}
