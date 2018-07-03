package io.sece.vlc.rcvr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview).  The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.<p>
 *
 * Supports scaling and mirroring of the graphics relative the camera's preview properties.  The
 * idea is that coordinates are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.<p>
 */
public class GraphicOverlay<T extends GraphicOverlay.Graphic> extends View {
    private final Object lock = new Object();

    private Set<T> graphics = new HashSet<>();


    public static abstract class Graphic {
        protected GraphicOverlay overlay;

        protected Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public abstract Rect boundingBox();

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }


    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }


    public void add(T graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }


    public void remove(T graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }


    public List<T> getGraphics() {
        synchronized (lock) {
            return new Vector(graphics);
        }
    }


    public Rect boundingBox() {
        return new Rect(0, 0, getWidth(), getHeight());
    }


    public boolean contains(Rect val) {
        return boundingBox().contains(val);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            for (Graphic g : graphics) {
                g.draw(canvas);
            }
        }
    }

    public RectF normalizeBoundingBox(Rect bb) {
        float w = getWidth();
        float h = getHeight();
        return new RectF(bb.left / w,bb.top / h,bb.right / w,bb.bottom / h);
    }
}
