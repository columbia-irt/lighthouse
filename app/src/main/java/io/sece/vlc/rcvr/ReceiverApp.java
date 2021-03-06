package io.sece.vlc.rcvr;

import android.app.Application;
import android.util.Log;

import org.opencv.android.OpenCVLoader;


public class ReceiverApp extends Application {
    public static final String TAG = "ReceiverApp";
    public static final int REQUEST_CAMERA_PERMISSION = 1;
    public static final int REQUEST_WRITING_PERMISSION = 2;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "Successfully loaded included OpenCV native libraries");
        } else {
            throw new UnsatisfiedLinkError("Cannot load OpenCV native libraries");
        }
    }
}
