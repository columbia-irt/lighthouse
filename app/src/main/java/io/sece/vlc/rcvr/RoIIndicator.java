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
    private static Paint dataProgressStyle;
    private static Paint transferProgressStyle;
    private double dataProgress = 0.0d;
    private double transferProgress = 0.0d;


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

        dataProgressStyle = new Paint(paint);
        dataProgressStyle.setStrokeWidth(35.0f);
        dataProgressStyle.setColor(Color.RED);

        transferProgressStyle = new Paint(dataProgressStyle);
        transferProgressStyle.setAlpha(127);
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


    private void drawROI(Canvas canvas) {
        canvas.drawCircle(center.x, center.y, radius, shadow);
        canvas.drawCircle(center.x, center.y, radius, paint);
    }


    private void drawProgressIndicator(Canvas canvas) {
        RectF r = new RectF(boundingBox());
        canvas.drawArc(r, 270, (int)(transferProgress * 360d / 100d), false, transferProgressStyle);
        canvas.drawArc(r, 270, (int)(dataProgress * 360d / 100d), false, dataProgressStyle);
    }


    @Override
    public void draw(Canvas canvas) {
        drawROI(canvas);
        drawProgressIndicator(canvas);
    }


    private static Rect boundingBox(Point center, int radius) {
        return new Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
    }


    public Rect boundingBox() {
        return boundingBox(center, radius);
    }


    @Subscribe
    private void onUpdate(Bus.DataProgress ev) {
        dataProgress = ev.completed;
        postInvalidate();
    }


    @Subscribe
    private void onUpdate(Bus.TransferProgress ev) {
        transferProgress = ev.completed;
        postInvalidate();
    }


    @Subscribe
    private void onUpdate(TransmitMonitor.Event ev) {
        if (ev.transmissionInProgress) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.WHITE);
        }
        postInvalidate();
    }

}
