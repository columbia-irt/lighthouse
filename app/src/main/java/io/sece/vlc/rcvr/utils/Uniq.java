package io.sece.vlc.rcvr.utils;

/**
 * Implements a uniqueness detector, similar to the uniq command line tool found in UNIX.
 * Method hasChanged returns true if either this is the first invocation of the method, or if
 * the value passed to it is different from the previous (remembered) value. In other cases
 * the method returns false.
 *
 * This class is meant be used with callbacks that do not with to receive duplicate values.
 *
 * @param <T> Type to specialize for
 */
public class Uniq<T> {
    private T previous = null;

    public boolean hasChanged(T current) {
        if (null == previous || !current.equals(previous)) {
            previous = current;
            return true;
        }
        return false;
    }
}
