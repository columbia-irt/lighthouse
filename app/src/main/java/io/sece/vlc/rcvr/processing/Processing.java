package io.sece.vlc.rcvr.processing;

import android.graphics.RectF;
import android.media.Image;
import android.os.HandlerThread;
import android.util.Log;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.sece.vlc.Modem;
import io.sece.vlc.rcvr.Bus;
import io.sece.vlc.rcvr.ViewfinderModel;
import io.sece.vlc.rcvr.processing.block.FrameSampler;
import io.sece.vlc.rcvr.processing.block.HueDetector;
import io.sece.vlc.rcvr.processing.block.RateMonitor;
import io.sece.vlc.rcvr.processing.block.RoIExtractor;
import io.sece.vlc.rcvr.processing.block.TransmitMonitor;


public class Processing extends HandlerThread {
    private static final String TAG = "Processing";

    private final int cores = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, cores,
            15, TimeUnit.SECONDS, queue, r -> {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    private Frame frame = new Frame();
    private AtomicLong sequence = new AtomicLong(0);

    private List<ProcessingBlock> stage1;
    private List<ProcessingBlock> stage2;

    private Modem modem;

    public static class Result extends Bus.Event {
        public Frame frame;

        public Result(Frame frame) {
            this.frame = frame;
        }
    }


    public Processing(RectF roi, Modem modem) {
        super("Processing");

        stage1 = Arrays.asList(
                new RateMonitor("camera"),
                new RoIExtractor(roi)
        );

        stage2 = Arrays.asList(
                new RateMonitor("worker"),
                new HueDetector(),
                new FrameSampler(((1000 / ViewfinderModel.synced_fps)), TimeUnit.MILLISECONDS),
                new TransmitMonitor(ViewfinderModel.synced_fps, modem, 3)
        );
    }


    @Override
    public void start() {
        super.start();
        Log.d(TAG, "Starting processing pipeline");
    }


    public void shutdown() throws InterruptedException {
        Log.d(TAG, "Shutting down processing pipeline");

        interrupt();
        quit();
        threadPool.shutdownNow();

        join();
        threadPool.awaitTermination(1, TimeUnit.SECONDS);

        Log.d(TAG, "Processing pipeline shutdown");
    }


    public void submit(Image img) {
        // Warning: Stage 1 is executed on the Image receiving thread. Perform only minimum amount
        // of processing (ideally constant time) here.
        if (currentThread().isInterrupted()) return;

        frame.set(img);
        frame.set(Frame.IMAGE_TIMESTAMP, img.getTimestamp());
        frame.set(Frame.RX_TIMESTAMP, System.nanoTime());
        frame.sequence = sequence.getAndIncrement();

        Frame f = frame;
        for (ProcessingBlock block : stage1) {
            f = block.apply(f);
            if (null == f) return;
        }

        // Make a copy of the data. Only the data inside the region of interest will be copied.
        // As a side effect, this will make the resulting (smaller) frame continuous again.
        Frame copy = frame.copy();

        try {
            threadPool.execute(() -> processFrame(copy));
        } catch (RejectedExecutionException e) {
            if (!isInterrupted())
                throw e;
        }
    }


    private void processFrame(Frame frame) {
        // Anything invoked from here is executed on a worker thread from the thread pool. Make
        // sure the frame argument is released after you no longer need the data!
        frame.set(Frame.PROCESSING_START, System.nanoTime());

        for (ProcessingBlock block : stage2) {
            if (currentThread().isInterrupted()) return;
            frame = block.apply(frame);
            if (null == frame) break;
        }

        if (null != frame) {
            frame.set(Frame.PROCESSING_END, System.nanoTime());
            frame.set(Frame.CURRENT_SEQUENCE, sequence.get() - 1);
            Bus.send(new Result(frame));
        }
    }
}
