package io.sece.vlc.rcvr;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;


public class RoIIndicator extends GraphicOverlay.Graphic {
    private Point center;
    private int radius;

    private static Paint paint1;
    private static Paint paint2;

    static {
        paint1 = new Paint();
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(6.0f);
        paint1.setColor(Color.WHITE);

        paint2 = new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(14.0f);
        paint2.setColor(Color.BLACK);
    }


    public RoIIndicator(GraphicOverlay overlay, Point center, int radius) {
        super(overlay);
        this.center(center);
        this.radius(radius);
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
        Rect rect = new Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        canvas.drawRect(rect, paint2);
        canvas.drawRect(rect, paint1);
    }


    private static Rect boundingBox(Point center, int radius) {
        return new Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
    }


    public Rect boundingBox() {
        return boundingBox(center, radius);
    }
}
