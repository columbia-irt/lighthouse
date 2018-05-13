package io.sece.vlc;


/**
 * This class represents a color composed of three primary channels: red,
 * green, and blue. It also provides named constants for well-known colors.
 */
public class Color {
    public int red, green, blue;

    public static final Color RED       = new Color(255,   0,   0);
    public static final Color GREEN     = new Color(0,   255,   0);
    public static final Color BLUE      = new Color(0,     0, 255);
    public static final Color PURPLE    = new Color(255,   0, 190);
    public static final Color YELLOW    = new Color(255, 185,   0);
    public static final Color TURQUIOSE = new Color(0,   255, 150);
    public static final Color BLACK     = new Color(0,     0,   0);
    public static final Color WHITE     = new Color(255, 255, 255);

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRed()   { return red;   }
    public int getGreen() { return green; }
    public int getBlue()  { return blue;  }
}
