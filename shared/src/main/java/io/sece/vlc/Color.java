package io.sece.vlc;


/**
 * This class represents a color composed of three primary channels: red,
 * green, and blue. It also provides named constants for well-known colors.
 */
public class Color {
    public int red, green, blue;


    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }



    public static int HSBtoRGB(float var0, float var1, float var2) {
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        if (var1 == 0.0F) {
        } else {
            float var8 = var2 * (1.0F - var1);
            float var9 = var2 * (1.0F - var1 * var7);
            float var10 = var2 * (1.0F - var1 * (1.0F - var7));
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
            }
        }

        return -16777216 | var3 << 16 | var4 << 8 | var5 << 0;
    }

        float hueT = hue / 360;
        float saturationT = saturation / 100;
        float brightnessT = brightness / 100;
        int rgb = HSBtoRGB(hueT, saturationT, brightnessT);

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

        float var5;
        if (var7 != 0) {
        } else {
            var5 = 0.0F;
        }

        float var4;
        if (var5 == 0.0F) {
            var4 = 0.0F;
        } else {
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

        return var3;
    }
}
