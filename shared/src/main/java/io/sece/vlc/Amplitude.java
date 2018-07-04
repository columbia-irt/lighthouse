package io.sece.vlc;

public class Amplitude implements EuclideanSpace {
    int value;

    public Amplitude(int value) {
        this.value = value;
    }

    public double euclideanDistance(Object o) {
        if (!(o instanceof Amplitude))
            throw new IllegalArgumentException("Invalid type");

        return Math.abs(value - ((Amplitude)o).value);
    }
}
