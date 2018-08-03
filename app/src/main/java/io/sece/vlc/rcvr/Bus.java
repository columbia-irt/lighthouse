package io.sece.vlc.rcvr;

import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;

import com.google.common.eventbus.AsyncEventBus;

/**
 * An event bus based on Guava's AsyncEventBus that always delivers messages on the UI thread.
 * Thus, it is guaranteed that the method annotated with @Subscribe will be executing on the UI
 * thread and there is no need to wrap it in runOnUiThread.
 */
public class Bus extends AsyncEventBus {
    private static Bus instance;
    private static Handler uiThread = new Handler(Looper.getMainLooper());

    public static class Event {
    }

    public static class BaudRateChange extends Event {
        public int baudRate;

        public BaudRateChange(int baudRate) {
            this.baudRate = baudRate;
        }
    }


    public static class ProgressUpdate extends Event { }


    public static class DataProgress extends ProgressUpdate {
        public float completed;

        public DataProgress(float completed) {
            this.completed = completed;
        }
    }


    public static class TransferProgress extends ProgressUpdate {
        public float completed;

        public TransferProgress(float completed) {
            this.completed = completed;
        }
    }


    public static class TransferCompleted extends ProgressUpdate {
        public byte[] data;

        public TransferCompleted(byte[] src) {
            data = new byte[src.length];
            System.arraycopy(src, 0, data, 0, src.length);
        }
    }


    public static class RoIEvent extends Event {
        public RectF boundingBox;

        RoIEvent(RectF boundingBox) {
            this.boundingBox = boundingBox;
        }
    }

    public static class WriteEvent extends Event {
        public boolean writingActive;

        WriteEvent(boolean active) {
            this.writingActive = active;
        }
    }



    private Bus() {
        super(runnable -> {
            if (Looper.myLooper() == Looper.getMainLooper()) runnable.run();
            else uiThread.post(runnable);
        });
    }

    public static Bus getInstance() {
        if (null == instance)
            instance = new Bus();
        return instance;
    }

    public static void send(Object object) {
        getInstance().post(object);
    }

    public static void subscribe(Object object) {
        getInstance().register(object);
    }

    public static void unsubscribe(Object object) {
        getInstance().unregister(object);
    }
}
