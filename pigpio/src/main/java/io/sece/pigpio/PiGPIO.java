package io.sece.pigpio;


public class PiGPIO {
    public static final int PI_OFF = 0;
    public static final int PI_ON  = 1;

    public static final int PI_CLEAR = 0;
    public static final int PI_SET   = 1;

    public static final int PI_PUD_OFF  = 0;
    public static final int PI_PUD_DOWN = 1;
    public static final int PI_PUD_UP   = 2;

    public static final boolean PI_LOW  = false;
    public static final boolean PI_HIGH = true;

    public static final int PI_GPIO2  = 2;
    public static final int PI_GPIO3  = 3;
    public static final int PI_GPIO4  = 4;
    public static final int PI_GPIO5  = 5;
    public static final int PI_GPIO6  = 6;
    public static final int PI_GPIO7  = 7;
    public static final int PI_GPIO8  = 8;
    public static final int PI_GPIO9  = 9;
    public static final int PI_GPIO10 = 10;
    public static final int PI_GPIO11 = 11;
    public static final int PI_GPIO12 = 12;
    public static final int PI_GPIO13 = 13;
    public static final int PI_GPIO14 = 14;
    public static final int PI_GPIO15 = 15;
    public static final int PI_GPIO16 = 16;
    public static final int PI_GPIO17 = 17;
    public static final int PI_GPIO18 = 18;
    public static final int PI_GPIO19 = 19;
    public static final int PI_GPIO20 = 20;
    public static final int PI_GPIO21 = 21;
    public static final int PI_GPIO22 = 22;
    public static final int PI_GPIO23 = 23;
    public static final int PI_GPIO24 = 24;
    public static final int PI_GPIO25 = 25;
    public static final int PI_GPIO26 = 26;
    public static final int PI_GPIO27 = 27;

    public static final int PI_INPUT  = 0;
    public static final int PI_OUTPUT = 1;
    public static final int PI_ALT0   = 4;
    public static final int PI_ALT1   = 5;
    public static final int PI_ALT2   = 6;
    public static final int PI_ALT3   = 7;
    public static final int PI_ALT4   = 3;
    public static final int PI_ALT5   = 2;

    private PiGPIO() { }

    public static native void gpioInitialize()                     throws PiGPIOException;
    public static native void gpioTerminate();
    public static native void gpioSetMode(int gpio, int mode)      throws PiGPIOException;
    public static native int  gpioGetMode(int gpio)                throws PiGPIOException;
    public static native void gpioSetPullUpDown(int gpio, int pud) throws PiGPIOException;
    public static native int  gpioRead(int gpio)                   throws PiGPIOException;
    public static native void gpioWrite(int gpio, int level)       throws PiGPIOException;
    public static native void gpioPWM(int gpio, int dutycycle)     throws PiGPIOException;
    public static native int  gpioGetPWMdutycycle(int gpio)        throws PiGPIOException;

    static {
        System.loadLibrary("pigpio-java");
    }
}
