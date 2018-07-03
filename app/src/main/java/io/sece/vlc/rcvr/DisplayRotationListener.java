package io.sece.vlc.rcvr;

import android.content.Context;
import android.view.Display;
import android.view.OrientationEventListener;

import io.sece.vlc.rcvr.utils.Uniq;


public class DisplayRotationListener {
    private Display display;
    private Context context;
    private OrientationEventListener listener;
    private Uniq<Integer> uniq = new Uniq<>();

    public interface RotationListener {
        void onChange(int rotation);
    }


    public DisplayRotationListener(Context context, Display display) {
        this.context = context;
        this.display = display;
    }


    public void onChange(RotationListener f) {
        listener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                int r = display.getRotation();
                if (uniq.hasChanged(r))
                    f.onChange(r);
            }
        };

        listener.enable();
    }


    public void disable() {
        if (null != listener) {
            listener.disable();
            listener = null;
        }
    }
}
