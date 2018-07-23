package io.sece.vlc.rcvr;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.common.eventbus.Subscribe;

import io.sece.vlc.DataFrame;
import io.sece.vlc.rcvr.processing.block.TransmitMonitor;

public class RoIIndicator extends GraphicOverlay.Graphic {
    private Point center;
    private int radius;

    private static Paint paint;
    private static Paint shadow;
    private static Paint arc1;
    private static Paint arc2;
    private double completed = 0.0d;
    private double unchecked = 0.0d;


    static {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(15.0f);
        paint.setColor(Color.WHITE);

        shadow = new Paint(paint);
        shadow.setStrokeWidth(20.0f);
        shadow.setColor(Color.BLACK);
        shadow.setAlpha(127);

        arc1 = new Paint(paint);
        arc1.setStrokeWidth(35.0f);
        arc1.setColor(Color.RED);

        arc2 = new Paint(arc1);
        arc2.setAlpha(127);
    }


    public RoIIndicator(GraphicOverlay overlay, Point center, int radius) {
        super(overlay);
        this.center(center);
        this.radius(radius);
        Bus.subscribe(this);
    }


    public Point center() {
        return center;
    }


    public Point center(Point value) {
        Rect bb = boundingBox(value, radius);
        Rect ov = overlay.boundingBox();

        if (!ov.contains(bb)) {
            if (bb.left < ov.left) {
                value.x += ov.left - bb.left;
                bb = boundingBox(value, radius);
            }

            if (bb.top < ov.top) {
                value.y += ov.top - bb.top;
                bb = boundingBox(value, radius);
            }

            if (bb.right > ov.right) {
                value.x -= bb.right - ov.right;
                bb = boundingBox(value, radius);
            }

            if (bb.bottom > ov.bottom) {
                value.y -= bb.bottom - ov.bottom;
            }
        }

        center = value;
        postInvalidate();
        return center;
    }


    public int radius() {
        return radius;
    }


    public int radius(int value) {
        if (value < 10) value = 10;

        Rect bb = boundingBox(center, value);
        Rect ov = overlay.boundingBox();

        if (!ov.contains(bb)) {
            if (bb.left < ov.left) {
                value -= ov.left - bb.left;
                bb = boundingBox(center, value);
            }

            if (bb.top < ov.top) {
                value -= ov.top - bb.top;
                bb = boundingBox(center, value);
            }

            if (bb.right > ov.right) {
                value -= bb.right - ov.right;
                bb = boundingBox(center, value);
            }

            if (bb.bottom > ov.bottom) {
                value -= bb.bottom - ov.bottom;
            }
        }

        radius = value;
        postInvalidate();
        return radius;
    }


    @Override
    public void draw(Canvas canvas) {
        RectF bb = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        canvas.drawCircle(center.x, center.y, radius, shadow);
        canvas.drawCircle(center.x, center.y, radius, paint);
        canvas.drawArc(bb, 270, (int)(completed * 360d / 100d), false, arc1);
        canvas.drawArc(bb, 270, (int)(unchecked * 360d / 100d), false, arc2);
    }


    private static Rect boundingBox(Point center, int radius) {
        return new Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
    }


    public Rect boundingBox() {
        return boundingBox(center, radius);
    }


    @Subscribe
    private void onProgressUpdate(Bus.ProgressUpdate ev) {
        completed = ev.completed;
        postInvalidate();
    }


    @Subscribe
    private void onMonitorUpdate(TransmitMonitor.Event ev) {
        if (ev.transmissionInProgress) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.WHITE);
        }
        postInvalidate();
    }

}
