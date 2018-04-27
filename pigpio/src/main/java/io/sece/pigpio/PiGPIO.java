package io.sece.pigpio;


public class PiGPIO {
    /*
     * The level of a GPIO. Low or High.
     */
    public static final int PI_OFF   = 0;
    public static final int PI_ON    = 1;
    public static final int PI_CLEAR = 0;
    public static final int PI_SET   = 1;
    public static final int PI_LOW   = 0;
    public static final int PI_HIGH  = 1;

    /*
     * The operational mode of a GPIO, normally INPUT or OUTPUT.
     */
    public static final int PI_INPUT  = 0;
    public static final int PI_OUTPUT = 1;
    public static final int PI_ALT0   = 4;
    public static final int PI_ALT1   = 5;
    public static final int PI_ALT2   = 6;
    public static final int PI_ALT3   = 7;
    public static final int PI_ALT4   = 3;
    public static final int PI_ALT5   = 2;

    /*
     * The setting of the pull up/down resistor for a GPIO, which may be off,
     * pull-up, or pull-down.
     */
    public static final int PI_PUD_OFF  = 0;
    public static final int PI_PUD_DOWN = 1;
    public static final int PI_PUD_UP   = 2;

    /*
     * The hardware PWM dutycycle.
     */
    public static final int PI_HW_PWM_RANGE = 1000000;

    /*
     * The hardware PWM frequency.
     */
    public static final int PI_HW_PWM_MIN_FREQ     = 1;
    public static final int PI_HW_PWM_MAX_FREQ     = 125000000;

    public static final int PI_MIN_DUTYCYCLE_RANGE = 25;
    public static final int PI_MAX_DUTYCYCLE_RANGE = 40000;

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

    private PiGPIO() { }


    /* Initialises the library.
     *
     * Returns the pigpio version number if OK, otherwise PI_INIT_FAILED.
     *
     * gpioInitialise must be called before using the other library functions
     * with the following exceptions:
     *   gpioCfg*
     *   gpioVersion
     *   gpioHardwareRevision
     *
     * Example
     *
     * if (gpioInitialise() < 0)
     * {
     *    // pigpio initialisation failed.
     * } else {
     *    // pigpio initialised okay.
     * }
     */
    public static native void gpioInitialise() throws PiGPIOException;


    /* Terminates the library.
     *
     * Returns nothing.
     * Call before program exit.
     *
     * This function resets the used DMA channels, releases memory, and
     * terminates any running threads.
     *
     * Example
     *   gpioTerminate();
     */
    public static native void gpioTerminate();


    /* Returns the hardware revision.
     *
     * If the hardware revision can not be found or is not a valid hexadecimal
     * number the function returns 0.
     *
     * The hardware revision is the last few characters on the Revision line
     * of /proc/cpuinfo.
     *
     * The revision number can be used to determine the assignment of GPIO to
     * pins (see gpio).
     *
     * There are at least three types of board.
     *
     * Type 1 boards have hardware revision numbers of 2 and 3.
     * Type 2 boards have hardware revision numbers of 4, 5, 6, and 15.
     * Type 3 boards have hardware revision numbers of 16 or greater.
     *
     * for "Revision : 0002" the function returns 2.
     * for "Revision : 000f" the function returns 15.
     * for "Revision : 000g" the function returns 0.
     */
    public static native int gpioHardwareRevision() throws PiGPIOException;


    /* Returns the pigpio version.
     */
    public static native int gpioVersion();


    /* Sets the GPIO mode, typically input or output.
     *
     * gpio: 0-53
     * mode: 0-7
     *
     * Returns 0 if OK, otherwise PI_BAD_GPIO or PI_BAD_MODE.
     *
     * Arduino style: pinMode.
     *
     * Example
     *
     *   gpioSetMode(17, PI_INPUT);  // Set GPIO17 as input.
     *   gpioSetMode(18, PI_OUTPUT); // Set GPIO18 as output.
     *   gpioSetMode(22,PI_ALT0);    // Set GPIO22 to alternative mode 0.
     *
     * See
     * http://www.raspberrypi.org/documentation/hardware/raspberrypi/bcm2835/BCM2835-ARM-Peripherals.pdf
     * page 102 for an overview of the modes.
     */
    public static native void gpioSetMode(int gpio, int mode) throws PiGPIOException;


    /* Gets the GPIO mode.
     *
     * gpio: 0-53
     *
     * Returns the GPIO mode if OK, otherwise PI_BAD_GPIO.
     *
     * Example
     *
     *   if (gpioGetMode(17) != PI_ALT0)
     *   {
     *     gpioSetMode(17, PI_ALT0);  // set GPIO17 to ALT0
     *   }
     */
    public static native int gpioGetMode(int gpio) throws PiGPIOException;


    /* Sets or clears resistor pull ups or downs on the GPIO.
     *
     * gpio: 0-53
     * pud: 0-2
     *
     * Returns 0 if OK, otherwise PI_BAD_GPIO or PI_BAD_PUD.
     *
     * Example
     *
     *   gpioSetPullUpDown(17, PI_PUD_UP);   // Sets a pull-up.
     *   gpioSetPullUpDown(18, PI_PUD_DOWN); // Sets a pull-down.
     *   gpioSetPullUpDown(23, PI_PUD_OFF);  // Clear any pull-ups/downs.
     */
    public static native void gpioSetPullUpDown(int gpio, int pud) throws PiGPIOException;


    /* Reads the GPIO level, on or off.
     *
     * gpio: 0-53
     *
     * Returns the GPIO level if OK, otherwise PI_BAD_GPIO.
     *
     * Arduino style: digitalRead.
     *
     * Example
     *
     *   printf("GPIO24 is level %d", gpioRead(24));
     */
    public static native int gpioRead(int gpio) throws PiGPIOException;


    /* Sets the GPIO level, on or off.
     *
     * gpio: 0-53
     * level: 0-1
     *
     * Returns 0 if OK, otherwise PI_BAD_GPIO or PI_BAD_LEVEL.
     *
     * If PWM or servo pulses are active on the GPIO they are switched off.
     *
     * Arduino style: digitalWrite
     *
     * Example
     *
     *   gpioWrite(24, 1); // Set GPIO24 high.
     */
    public static native void gpioWrite(int gpio, int level) throws PiGPIOException;


    /* Starts PWM on the GPIO, dutycycle between 0 (off) and range (fully on).
     * Range defaults to 255.
     *
     * user_gpio: 0-31
     * dutycycle: 0-range
     *
     * Returns 0 if OK, otherwise PI_BAD_USER_GPIO or PI_BAD_DUTYCYCLE.
     *
     * Arduino style: analogWrite
     *
     * This and the servo functionality use the DMA and PWM or PCM peripherals
     * to control and schedule the pulse lengths and dutycycles.
     *
     * The gpioSetPWMrange function may be used to change the default range of
     * 255.
     *
     * Example
     *
     *   gpioPWM(17, 255); // Sets GPIO17 full on.
     *   gpioPWM(18, 128); // Sets GPIO18 half on.
     *   gpioPWM(23, 0);   // Sets GPIO23 full off.
     */
    public static native void gpioPWM(int gpio, int dutycycle) throws PiGPIOException;


    /* Returns the PWM dutycycle setting for the GPIO.
     *
     * user_gpio: 0-31
     *
     * Returns between 0 (off) and range (fully on) if OK, otherwise
     * PI_BAD_USER_GPIO or PI_NOT_PWM_GPIO.
     *
     * For normal PWM the dutycycle will be out of the defined range for the
     * GPIO (see gpioGetPWMrange).
     *
     * If a hardware clock is active on the GPIO the reported dutycycle will
     * be 500000 (500k) out of 1000000 (1M).
     *
     * If hardware PWM is active on the GPIO the reported dutycycle will be
     * out of a 1000000 (1M).
     *
     * Normal PWM range defaults to 255.
     */
    public static native int gpioGetPWMdutycycle(int gpio) throws PiGPIOException;


    /* Returns the dutycycle range used for the GPIO if OK, otherwise
     * PI_BAD_USER_GPIO.
     *
     * user_gpio: 0-31
     *
     * If a hardware clock or hardware PWM is active on the GPIO the reported
     * range will be 1000000 (1M).
     *
     * Example
     *
     *   r = gpioGetPWMrange(23);
     */
    public static native int gpioGetPWMrange(int gpio) throws PiGPIOException;


    /* Returns the real range used for the GPIO if OK, otherwise
     * PI_BAD_USER_GPIO.
     *
     * user_gpio: 0-31
     *
     * If a hardware clock is active on the GPIO the reported real range will
     * be 1000000 (1M).
     *
     * If hardware PWM is active on the GPIO the reported real range will be
     * approximately 250M divided by the set PWM frequency.
     *
     * Example
     *
     *   rr = gpioGetPWMrealRange(17);
     */
    public static native int gpioGetPWMrealRange(int gpio) throws PiGPIOException;


    /* Sets the frequency in hertz to be used for the GPIO.
     *
     * user_gpio: 0-31
     * frequency: >=0
     *
     * Returns the numerically closest frequency if OK, otherwise
     * PI_BAD_USER_GPIO.
     *
     * If PWM is currently active on the GPIO it will be switched off and then
     * back on at the new frequency.
     *
     * Each GPIO can be independently set to one of 18 different PWM
     * frequencies.
     *
     * The selectable frequencies depend upon the sample rate which may be 1,
     * 2, 4, 5, 8, or 10 microseconds (default 5).
     *
     * The frequencies for each sample rate are:
     *
     * sample
     * rate (us)             Hertz
     *
     *  1: 40000 20000 10000 8000 5000 4000 2500 2000 1600
     *      1250  1000   800  500  400  250  200  100   50
     *
     *  2: 20000 10000  5000 4000 2500 2000 1250 1000  800
     *       625   500   400  250  200  125  100   50   25
     *
     *  4: 10000  5000  2500 2000 1250 1000  625  500  400
     *       313   250   200  125  100   63   50   25   13
     *
     *  5:  8000  4000  2000 1600 1000  800  500  400  320
     *       250   200   160  100   80   50   40   20   10
     *
     *  8:  5000  2500  1250 1000  625  500  313  250  200
     *       156   125   100   63   50   31   25   13    6
     *
     * 10:  4000  2000  1000  800  500  400  250  200  160
     *       125   100    80   50   40   25   20   10    5
     *
     * Example
     *
     *   gpioSetPWMfrequency(23, 0);      // Set GPIO23 to lowest frequency.
     *   gpioSetPWMfrequency(24, 500);    // Set GPIO24 to 500Hz.
     *   gpioSetPWMfrequency(25, 100000); // Set GPIO25 to highest frequency.
     */
    public static native int gpioSetPWMfrequency(int gpio, int frequency) throws PiGPIOException;


    /* Returns the frequency (in hertz) used for the GPIO if OK, otherwise
     * PI_BAD_USER_GPIO.
     *
     * user_gpio: 0-31
     *
     * For normal PWM the frequency will be that defined for the GPIO by
     * gpioSetPWMfrequency.
     *
     * If a hardware clock is active on the GPIO the reported frequency will
     * be that set by gpioHardwareClock.
     *
     * If hardware PWM is active on the GPIO the reported frequency will be
     * that set by gpioHardwarePWM.
     *
     * Example
     *
     *   f = gpioGetPWMfrequency(23); // Get frequency used for GPIO23.
     */
    public static native int gpioGetPWMfrequency(int gpio) throws PiGPIOException;


    static {
        System.loadLibrary("pigpio-java");
    }
}
