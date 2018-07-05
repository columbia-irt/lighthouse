package io.sece.vlc;

import java.util.Locale;
import java.util.Objects;

public class Amplitude implements Coordinate<Amplitude> {
    int value;

    public Amplitude(int value) {
        this.value = value;
    }

    @Override
    public double euclideanDistanceTo(Amplitude other) {
        // Speed optimization, return 0 right away if the other element is equal.
        if (other.equals(this))
            return 0;

        return Math.abs(this.value - other.value);
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Amplitude))
            return false;

        Amplitude a = (Amplitude)o;
        return value == a.value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(value);
    }


    @Override
    public String toString() {
        return String.format(Locale.US,"%d", value);
    }
}
