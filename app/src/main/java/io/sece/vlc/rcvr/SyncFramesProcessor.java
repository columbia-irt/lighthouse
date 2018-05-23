package io.sece.vlc.rcvr;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alex on 5/15/18.
 */

public class SyncFramesProcessor implements Runnable
{
    private LinkedBlockingQueue syncBlockingQueue;

    public SyncFramesProcessor(LinkedBlockingQueue syncBlockingQueue) {
        this.syncBlockingQueue = syncBlockingQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                System.out.println("Pre Process ");
                processFrame((Mat)syncBlockingQueue.take());
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
    }

    public void processFrame(Mat pFrame) {
        System.out.println("Done Process ");
    }

}
