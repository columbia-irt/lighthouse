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

    public static class FrameUpdate extends Event {
        public String data;

        public FrameUpdate(String src) {
            data = src;
        }
    }

    public static class ProgressUpdate extends Event {
        public float completed;

        public ProgressUpdate(float completed) {
            this.completed = completed;
        }
    }


    public static class FrameStats extends Event {
        public int total;
        public int errors;

        public FrameStats(int total, int errors) {
            this.total = total;
            this.errors = errors;
        }
    }

    public static class TransferCompleted extends Event {
        public String msg;

        public TransferCompleted(String msg) {
            this.msg = msg;
        }
    }


    public static class RoIEvent extends Event {
        public RectF boundingBox;

        RoIEvent(RectF boundingBox) {
            this.boundingBox = boundingBox;
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
