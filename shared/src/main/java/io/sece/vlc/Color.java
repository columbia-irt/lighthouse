package io.sece.vlc;


public class Color {
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE = new Color(0, 0, 255);

    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color CYAN = new Color(0, 255, 255);
    public static final Color MAGENTA = new Color(255, 0, 255);

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);

    public static final int MAX_HUE = 360;
    public static final int MAX_SATURATION = 100;
    public static final int MAX_BRIGHTNESS = 100;

    public final int red, green, blue;
    public final int hue, saturation, brightness;


    public Color(int red, int green, int blue) {
        this(red, green, blue, null);
    }


    public Color(int hue) {
        this(new int[] {hue, MAX_SATURATION, MAX_BRIGHTNESS});
    }


    protected Color(int red, int green, int blue, int[] hsv) {
        this.red = red;
        this.green = green;
        this.blue = blue;

        if (hsv == null)
            hsv = RGBtoHSB(null, red, green, blue);

        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.brightness = hsv[2];
    }


    public Color(int[] hsv) {
        int[] rgb = HSBtoRGB(null, hsv[0], hsv[1], hsv[2]);

        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];

        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.brightness = hsv[2];
    }


    public static int[] HSBtoRGB(int[] rgb, int hue, int saturation, int brightness) {
        if (rgb == null)
            rgb = new int[3];

        float h = hue / (float)MAX_HUE;
        float s = saturation / (float)MAX_SATURATION;
        float b = brightness / (float)MAX_BRIGHTNESS;

        if (s == 0) {
            rgb[0] = rgb[1] = rgb[2] = (int) (b * 255.0f + 0.5f);
        } else {
            float hh = (h - (float)Math.floor(h)) * 6.0f;
            float f = hh - (float)java.lang.Math.floor(hh);
            float p = b * (1.0f - s);
            float q = b * (1.0f - s * f);
            float t = b * (1.0f - (s * (1.0f - f)));
            switch ((int) hh) {
                case 0:
                    rgb[0] = (int) (b * 255.0f + 0.5f);
                    rgb[1] = (int) (t * 255.0f + 0.5f);
                    rgb[2] = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    rgb[0] = (int) (q * 255.0f + 0.5f);
                    rgb[1] = (int) (b * 255.0f + 0.5f);
                    rgb[2] = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    rgb[0] = (int) (p * 255.0f + 0.5f);
                    rgb[1] = (int) (b * 255.0f + 0.5f);
                    rgb[2] = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    rgb[0] = (int) (p * 255.0f + 0.5f);
                    rgb[1] = (int) (q * 255.0f + 0.5f);
                    rgb[2] = (int) (b * 255.0f + 0.5f);
                    break;
                case 4:
                    rgb[0] = (int) (t * 255.0f + 0.5f);
                    rgb[1] = (int) (p * 255.0f + 0.5f);
                    rgb[2] = (int) (b * 255.0f + 0.5f);
                    break;
                case 5:
                    rgb[0] = (int) (b * 255.0f + 0.5f);
                    rgb[1] = (int) (p * 255.0f + 0.5f);
                    rgb[2] = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return rgb;
    }


    public static int[] RGBtoHSB(int hsb[], int red, int green, int blue) {
        if (hsb == null)
            hsb = new int[3];

        float h, s, b;
        int min, max;

        if (red < green) {
            min = red;
            max = green;
        } else {
            min = green;
            max = red;
        }
        if (blue > max)
            max = blue;
        else if (blue < min)
            min = blue;
        b = (float)max / 255f;

        if (max == 0)
            s = 0;
        else
            s = ((float) (max - min)) / ((float) max);

        if (s == 0)
            h = 0;
        else {
            float delta = (max - min) * 6;
            if (red == max)
                h = (float) (green - blue) / delta;
            else if (green == max)
                h = 1f / 3 + (blue - red) / delta;
            else
                h = 2f / 3 + (red - green) / delta;
            if (h < 0)
                h++;
        }

        hsb[0] = Math.round(h * (float)MAX_HUE);
        hsb[1] = Math.round(s * (float)MAX_SATURATION);
        hsb[2] = Math.round(b * (float)MAX_BRIGHTNESS);
        return hsb;
    }
}
