package io.sece.vlc;

import java.util.ArrayList;
import java.util.List;


public class LineCoder {
    private static final int NO_MAX_SIZE = -1;
    private static final int STATE_START = 0;
    private static final int STATE_S1 = 1;
    private static final int STATE_S2 = 2;
    private static final int STATE_D = 3;
    private static final int STATE_DS1 = 4;
    private static final int STATE_DS2 = 5;
    private static final int STATE_RESET = 6;
    private static final int STATE_D1 = 1;

    public static final class FrameTooLong extends Exception { }

    public final int[] marker;
    private int state = STATE_START;
    private final int maxSize;

    private final ArrayList<Integer> buffer = new ArrayList<>();


    public LineCoder(int[] marker) {
        this(marker, NO_MAX_SIZE);
    }


    public LineCoder(int[] marker, int maxSize) {
        if (marker.length != 3)
            throw new IllegalArgumentException("Bug: Frame marker must consist of three symbols");

        this.marker = marker.clone();
        this.maxSize = maxSize;
    }


    public void reset() {
        reset(STATE_START);
    }


    private void reset(int state) {
        buffer.clear();
        this.state = state;
    }


    public List<Integer> get() {
        return buffer;
    }


    public float percentCompleted() {
        if (maxSize == NO_MAX_SIZE) return 0f;
        return (float)get().size() / (float)maxSize * 100f;
    }


    private void store(int... symbols) throws FrameTooLong {
        if (maxSize != NO_MAX_SIZE && (buffer.size() + symbols.length) > maxSize)
            throw new FrameTooLong();

        for (int s : symbols) buffer.add(s);
    }


    public List<Integer> decode(int symbol) throws FrameTooLong {
        try {
            switch (state) {
                case STATE_START:
                    if (symbol == marker[0]) state = STATE_S1;
                    break;

                case STATE_S1:
                    if (symbol == marker[1]) {
                        state = STATE_S2;
                    } else {
                        state = STATE_START;
                    }
                    break;

                case STATE_S2:
                    if (symbol == marker[2]) {
                        state = STATE_D;
                    } else if (symbol == marker[0]) {
                        state = STATE_S1;
                    } else {
                        state = STATE_START;
                    }
                    break;

                case STATE_RESET:
                    reset(STATE_D);
                    /* fall through to STATE_D */

                case STATE_D:
                    if (symbol == marker[0]) state = STATE_DS1;
                    else store(symbol);
                    break;

                case STATE_DS1:
                    if (symbol == marker[1]) {
                        state = STATE_DS2;
                    } else if (symbol == marker[0]) {
                        store(symbol);
                    } else {
                        store(marker[0], symbol);
                        state = STATE_D;
                    }
                    break;

                case STATE_DS2:
                    if (symbol == marker[2]) {
                        state = STATE_RESET;
                        return buffer;
                    } else if (symbol == marker[1]) {
                        store(marker[0], marker[1]);
                        state = STATE_D;
                    } else {
                        reset(STATE_START);
                    }
                    break;

                default:
                    throw new RuntimeException("Bug: Invalid state reached");
            }
        } catch (FrameTooLong e) {
            reset(STATE_START);
            throw e;
        }

        return null;
    }


    public List<Integer> encode(List<Integer> symbols) throws FrameTooLong {
        if (maxSize != NO_MAX_SIZE && maxSize < symbols.size())
            throw new FrameTooLong();

        reset();

        for (int s : marker) buffer.add(s);

        for (int s : symbols) {
            buffer.add(s);
            switch (state) {
                case STATE_START:
                    if (s == marker[0]) state = STATE_D1;
                    break;

                case STATE_D1:
                    if (s == marker[1]) {
                        buffer.add(marker[1]);
                        state = STATE_START;
                    } else if (s == marker[0]) {
                        state = STATE_D1;
                    } else {
                        state = STATE_START;
                    }
                    break;

                default:
                    throw new RuntimeException("Bug: Invalid state reached");
            }
        }
        return buffer;
    }
}
