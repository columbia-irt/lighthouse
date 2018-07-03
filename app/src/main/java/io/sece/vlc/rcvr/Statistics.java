package io.sece.vlc.rcvr;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.google.common.eventbus.Subscribe;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.sece.vlc.rcvr.processing.Frame;
import io.sece.vlc.rcvr.processing.block.RateMonitor;
import io.sece.vlc.rcvr.processing.Processing;
import io.sece.vlc.rcvr.utils.MovingAverage;


public class Statistics extends AppCompatTextView {

    private double cameraFrameRate = 0;
    private double workerFrameRate = 0;
    private MovingAverage processingDelay = new MovingAverage(100, TimeUnit.MILLISECONDS);
    private MovingAverage processingTime = new MovingAverage(100, TimeUnit.MILLISECONDS);
    private MovingAverage queueLength = new MovingAverage(100, TimeUnit.MILLISECONDS);


    public Statistics(Context context) {
        super(context);
    }

    public Statistics(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public Statistics(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void updateStatistics() {
        setText(String.format(Locale.US,
                "Camera frame rate: %.1f fps\nProcessing delay: %.0f ms\nProcessing time: %.0f ms\nProcessing queue: %.0f\nProcessing frame rate: %.1f fps",
                cameraFrameRate, processingDelay.value, processingTime.value, queueLength.value, workerFrameRate));
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


    @Subscribe
    private void onAvgFrameRateChange(RateMonitor.Event ev) {
        // By the time this gets involved on the UI thread the activity may have been
        // destroyed, e.g., as result of a rotation event.
        if (getContext() == null) return;

        if (ev.id == "camera") {
            cameraFrameRate = ev.fps;
        } else {
            workerFrameRate = ev.fps;
        }
        updateStatistics();
    }


    @Subscribe
    private void onResult(Processing.Result ev) {
        long delay = ev.frame.get(Frame.PROCESSING_START) - ev.frame.get(Frame.IMAGE_TIMESTAMP);
        processingDelay.update(TimeUnit.NANOSECONDS.toMillis(delay));

        long time = ev.frame.get(Frame.PROCESSING_END) - ev.frame.get(Frame.PROCESSING_START);
        processingTime.update(TimeUnit.NANOSECONDS.toMillis(time));

        queueLength.update(ev.frame.get(Frame.CURRENT_SEQUENCE) - ev.frame.sequence);

        updateStatistics();
    }
}
