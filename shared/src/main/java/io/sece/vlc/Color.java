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


    public static int HSBtoRGB(float var0, float var1, float var2) {
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        if (var1 == 0.0F) {
            var3 = var4 = var5 = (int)(var2 * 255.0F + 0.5F);
        } else {
            float var6 = (var0 - (float)Math.floor((double)var0)) * 6.0F;
            float var7 = var6 - (float)Math.floor((double)var6);
            float var8 = var2 * (1.0F - var1);
            float var9 = var2 * (1.0F - var1 * var7);
            float var10 = var2 * (1.0F - var1 * (1.0F - var7));
            switch((int)var6) {
                case 0:
                    var3 = (int)(var2 * 255.0F + 0.5F);
                    var4 = (int)(var10 * 255.0F + 0.5F);
                    var5 = (int)(var8 * 255.0F + 0.5F);
                    break;
                case 1:
                    var3 = (int)(var9 * 255.0F + 0.5F);
                    var4 = (int)(var2 * 255.0F + 0.5F);
                    var5 = (int)(var8 * 255.0F + 0.5F);
                    break;
                case 2:
                    var3 = (int)(var8 * 255.0F + 0.5F);
                    var4 = (int)(var2 * 255.0F + 0.5F);
                    var5 = (int)(var10 * 255.0F + 0.5F);
                    break;
                case 3:
                    var3 = (int)(var8 * 255.0F + 0.5F);
                    var4 = (int)(var9 * 255.0F + 0.5F);
                    var5 = (int)(var2 * 255.0F + 0.5F);
                    break;
                case 4:
                    var3 = (int)(var10 * 255.0F + 0.5F);
                    var4 = (int)(var8 * 255.0F + 0.5F);
                    var5 = (int)(var2 * 255.0F + 0.5F);
                    break;
                case 5:
                    var3 = (int)(var2 * 255.0F + 0.5F);
                    var4 = (int)(var8 * 255.0F + 0.5F);
                    var5 = (int)(var9 * 255.0F + 0.5F);
            }
        }

        return -16777216 | var3 << 16 | var4 << 8 | var5 << 0;
    }

    public static Color hsvToRGB(float hue, float saturation, float brightness)
    {
        float hueT = hue / 360;
        float saturationT = saturation / 100;
        float brightnessT = brightness / 100;
        int rgb = HSBtoRGB(hueT, saturationT, brightnessT);
        int red = (rgb>>16)&0xFF;
        int green = (rgb>>8)&0xFF;
        int blue = rgb&0xFF;

        return new Color(red, green, blue);
    }

    public static float[] RGBtoHSB(int var0, int var1, int var2, float[] var3) {
        if (var3 == null) {
            var3 = new float[3];
        }

        int var7 = var0 > var1 ? var0 : var1;
        if (var2 > var7) {
            var7 = var2;
        }

        int var8 = var0 < var1 ? var0 : var1;
        if (var2 < var8) {
            var8 = var2;
        }

        float var6 = (float)var7 / 255.0F;
        float var5;
        if (var7 != 0) {
            var5 = (float)(var7 - var8) / (float)var7;
        } else {
            var5 = 0.0F;
        }

        float var4;
        if (var5 == 0.0F) {
            var4 = 0.0F;
        } else {
            float var9 = (float)(var7 - var0) / (float)(var7 - var8);
            float var10 = (float)(var7 - var1) / (float)(var7 - var8);
            float var11 = (float)(var7 - var2) / (float)(var7 - var8);
            if (var0 == var7) {
                var4 = var11 - var10;
            } else if (var1 == var7) {
                var4 = 2.0F + var9 - var11;
            } else {
                var4 = 4.0F + var10 - var9;
            }

            var4 /= 6.0F;
            if (var4 < 0.0F) {
                ++var4;
            }
        }

        var3[0] = ((int)(var4 * 360)*100)/100;
        var3[1] = ((int)(var5 * 100)*100)/100;
        var3[2] = ((int)(var6 * 100)*100)/100;
        return var3;
    }
}
