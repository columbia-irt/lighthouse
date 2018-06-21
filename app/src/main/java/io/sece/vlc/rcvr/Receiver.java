package io.sece.vlc.rcvr;

import android.app.Application;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import io.sece.vlc.Modulator;


public class Receiver extends Application {
    public static final String TAG = "Receiver";
    public static final int REQUEST_CAMERA_PERMISSION = 1;


//    private Modulator<T> modulator;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "Successfully loaded included OpenCV native libraries");
        } else {
            throw new UnsatisfiedLinkError("Cannot load OpenCV native libraries");
        }
    }

//    public Receiver(Modulator modulator){
//        this.modulator = modulator;
//    }
//
//    public String rx(T value) throws InterruptedException{
//        return modulator.demodulate(value);
//    }


}
