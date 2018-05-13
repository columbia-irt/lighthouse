package io.sece.vlc.trx;


/**
 * A general purpose checked LED exception. These exceptions can occur while
 * the implementation is trying to control an LED, e.g., to set its intensity
 * or color. In most cases, this exception would wrap a lower-level exception
 * thrown by a particular LED driver, e.g., PiGPIOException.
 */
public class LEDException extends Exception {
    public LEDException(String s) {
        super(s);
    }

    public LEDException(String s, Throwable cause) {
        super(s, cause);
    }

    public LEDException(Throwable cause) {
        super(cause);
    }
}
