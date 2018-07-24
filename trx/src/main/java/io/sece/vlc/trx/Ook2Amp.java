package io.sece.vlc.trx;


/**
 * A compatibility wrapper that makes it possible to connect an OOKModulator
 * to a LED implementing the continous version of the LED interface, e.g., a
 * PWM-controlled LED. The values to be used for on and off states are
 * configurable.
 */
class Ook2Amp implements DiscreteLEDInterface {
    private int min;
    private int max;
    private ContinuousLEDInterface led;

    public Ook2Amp(ContinuousLEDInterface led, int min, int max) {
        this.min = min;
        this.max = max;
        this.led = led;
    }

    public Ook2Amp(ContinuousLEDInterface led) {
        this(led, 0, 255);
    }

    @Override
    public void set(Boolean value) {
        if (value) led.set(max);
        else led.set(min);
    }
}
