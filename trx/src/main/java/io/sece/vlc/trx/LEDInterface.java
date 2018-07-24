package io.sece.vlc.trx;


/**
 * An abstract base class for all LED drivers that provide a means to set some
 * parameters of the emitted light. The method set (which must be parametrized
 * by the driver) provides an interface to set those parameters.
 */
public interface LEDInterface<T> {
    void set(T value);
}
