package io.sece.vlc.rcvr;


import android.widget.SeekBar;
import io.sece.vlc.rcvr.utils.LinearScaler;


public class Slider {
    private SeekBar widget;
    private LinearScaler scaler;


    public interface SliderListener {
        void onChange(int value);
    }


    public Slider(SeekBar widget) {
        this.widget = widget;
    }


    public Slider(int min, int max, int init, SeekBar widget) {
        this(widget);

        int smax, sinit;
        if (min != 0) {
            // SeekBar does not seem to support intervals that do not start at 0, so we have to
            // shift the entire interval and translate values we get back from the seek bar.
            smax = max - min;
            sinit = init - min;
        } else {
            smax = max;
            sinit = init;
        }
        widget.setMax(smax);
        widget.setProgress(sinit);

        scaler = new LinearScaler(min, max, 0, widget.getMax());
    }


    public void onChange(SliderListener f) {
        widget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                f.onChange(transform(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
         });
    }


    private int transform(int value) {
        if (null != scaler)  value = scaler.invoke(value);
        return value;
    }
}
