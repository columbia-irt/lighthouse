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
import io.sece.vlc.rcvr.processing.block.TransmitMonitor;
import io.sece.vlc.rcvr.utils.MovingAverage;


public class Statistics extends AppCompatTextView {

    private double cameraFrameRate = 0;
    private double workerFrameRate = 0;
    private MovingAverage processingDelay = new MovingAverage(100, TimeUnit.MILLISECONDS);
    private MovingAverage processingTime = new MovingAverage(100, TimeUnit.MILLISECONDS);
    private MovingAverage queueLength = new MovingAverage(100, TimeUnit.MILLISECONDS);
    private boolean signalLock = false;
    private double signalRate = 0;
    private float completed = 0f;
    private String frameData = "";
    private int framesTotal = 0;
    private int framesError = 0;


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
        StringBuilder b = new StringBuilder();

        b.append(String.format(Locale.US, "Camera frame rate: %.1f fps\n", cameraFrameRate));
        b.append(String.format(Locale.US, "Processing delay: %.0f ms\n", processingDelay.value));
        b.append(String.format(Locale.US, "Processing time: %.0f ms\n", processingTime.value));
        b.append(String.format(Locale.US, "Processing frame rate: %.1f fps\n", workerFrameRate));
        b.append(String.format(Locale.US, "Queue length: %.0f\n", queueLength.value));
        b.append(String.format(Locale.US, "Signal: locked=%b rate=%.0f Bd\n", signalLock, signalRate));
        b.append(String.format(Locale.US, "Transferred: %.1f %%\n", completed));
        b.append(String.format(Locale.US, "Frames: total=%d, errors=%d\n", framesTotal, framesError));
        b.append(String.format(Locale.US, "Receiving: %s", frameData));

        setText(b.toString());
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

        if (ev.id.equals("camera")) {
            cameraFrameRate = ev.fps;
        } else {
            workerFrameRate = ev.fps;
        }
        updateStatistics();
    }


    @Subscribe
    private void onResult(Processing.Result ev) {
        long delay = ev.frame.getLongAttr(Frame.PROCESSING_START) - ev.frame.getLongAttr(Frame.IMAGE_TIMESTAMP);
        processingDelay.update(TimeUnit.NANOSECONDS.toMillis(delay));

        long time = ev.frame.getLongAttr(Frame.PROCESSING_END) - ev.frame.getLongAttr(Frame.PROCESSING_START);
        processingTime.update(TimeUnit.NANOSECONDS.toMillis(time));

        queueLength.update(ev.frame.getLongAttr(Frame.CURRENT_SEQUENCE) - ev.frame.sequence);
        updateStatistics();
    }

    @Subscribe
    private void onMonitorUpdate(TransmitMonitor.Event ev) {
        signalRate = ev.fps;
        signalLock = ev.transmissionInProgress;
        updateStatistics();
    }

    @Subscribe
    private void onFrameUpdate(Bus.FrameUpdate ev) {
        frameData = ev.data;
        updateStatistics();
    }

    @Subscribe
    private void onProgressUpdate(Bus.ProgressUpdate ev) {
        completed = ev.completed;
        updateStatistics();
    }

    @Subscribe
    private void onFrameStats(Bus.FrameStats ev) {
        framesTotal = ev.total;
        framesError = ev.errors;
        updateStatistics();
    }
}
