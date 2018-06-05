package io.sece.vlc.rcvr;


import java.lang.reflect.Array;
import java.util.Vector;

/**
 * Created by alex on 5/15/18.
 *
 * removed synchronized behaviour
 * no need for it - all calls of CircularBuffer Functions from same Thread
 */

public class CircularBuffer<T> {
    private T[] array;
    private int currIndex = -1;

    public CircularBuffer(int maxSize) {
        array= (T[]) new Object[maxSize];
    }

    public T get() {
        return array[currIndex];
    }

    public T get(int index) {
//        return array[index % array.length];
        throw new UnsupportedOperationException();
    }


    public void put(T frame){
        currIndex = ((currIndex + 1) % array.length);
        array[currIndex] =  frame;
//        notifyAll();
    }

    public boolean isEmpty() {
        return currIndex == -1;
    }
}
