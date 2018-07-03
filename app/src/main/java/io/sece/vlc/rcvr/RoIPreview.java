package io.sece.vlc.rcvr;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.google.common.eventbus.Subscribe;

import org.opencv.android.Utils;

import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.Processing;


public class RoIPreview extends AppCompatImageView {

    public RoIPreview(Context context) {
        super(context);
    }

    public RoIPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoIPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Subscribe
    public void onNewPreview(Processing.Result ev) {
        Bitmap bitmap = Bitmap.createBitmap(ev.frame.width, ev.frame.height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(ev.frame.rgba(), bitmap);
        setImageBitmap(bitmap);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Bus.subscribe(this);
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Bus.unsubscribe(this);
    }

}
