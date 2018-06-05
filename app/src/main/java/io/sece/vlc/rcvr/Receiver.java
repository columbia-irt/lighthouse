package io.sece.vlc.rcvr;

import android.app.Application;
import android.util.Log;

import org.opencv.android.OpenCVLoader;


public class Receiver extends Application {
    public static final String TAG = "Receiver";

    public static final int REQUEST_CAMERA_PERMISSION = 1;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "Successfully loaded included OpenCV native libraries");
        } else {
            throw new UnsatisfiedLinkError("Cannot load OpenCV native libraries");
        }
    }
}
