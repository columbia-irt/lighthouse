package io.sece.vlc.rcvr.processing.block;


import java.io.IOException;

import io.sece.vlc.rcvr.CSVWrite;
import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.ProcessingBlock;

public class Writer implements ProcessingBlock {
    CSVWrite csvWrite;

    private boolean isActive = false;

    public Writer() {
        csvWrite = new CSVWrite("values_");
    }


    public synchronized Frame apply(Frame frame) {
        long hue = frame.getColorAttr(Frame.HUE).hue;
        long br = frame.getColorAttr(Frame.HUE).brightness;
        long ts = frame.getLongAttr(Frame.IMAGE_TIMESTAMP);
         if(csvWrite != null) {
            String[] data = {String.valueOf(ts),String.valueOf(hue),String.valueOf(br)};

            // can not escalate the Exception in the next instance because the apply method is not defined that way
             try {
                 csvWrite.write(data);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
        return frame;
    }
}
