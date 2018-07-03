package io.sece.vlc.rcvr;

import android.annotation.SuppressLint;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;




public class GestureControl {
    private View view;

    private ScaleGestureDetector scaleDetector;
    private GestureDetectorCompat gestureDetector;


    public interface ZoomListener {
        void onZoomChanged(float zoom);
    }

    public interface TapListener {
        void onTap(int x, int y);
    }



    @SuppressLint("ClickableViewAccessibility")
    public GestureControl(View view) {
        this.view = view;

        view.setOnTouchListener((v, ev) -> {
            boolean rv = false;

            if (null != scaleDetector)
                rv |= scaleDetector.onTouchEvent(ev);

            if (null != gestureDetector)
                rv |= gestureDetector.onTouchEvent(ev);

            return rv;
        });
    }


    public void onZoom(ZoomListener f) {
        scaleDetector = new ScaleGestureDetector(view.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                f.onZoomChanged(factor);
                return true;
            }
        });
    }


    public void onTap(TapListener f) {
        gestureDetector = new GestureDetectorCompat(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent ev) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent ev) {
                f.onTap((int)ev.getX(), (int)ev.getY());
                return true;
            }
        });
    }
}
