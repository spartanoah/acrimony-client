/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

import Acrimony.util.misc.MathUtils;
import java.awt.Color;

public class ColorUtil {
    public static final int buttonHoveredColor = new Color(18, 10, 168).getRGB();

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return ColorUtil.applyOpacity(old, opacity).getRGB();
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity));
    }

    public static int getColor(Color color1, Color color2, long ms, int offset) {
        double scale = (double)((System.currentTimeMillis() + (long)offset) % ms) / (double)ms * 2.0;
        double finalScale = scale > 1.0 ? 2.0 - scale : scale;
        return ColorUtil.getGradient(color1, color2, finalScale).getRGB();
    }

    public static int getColor(Color color1, Color color2, Color color3, long ms, int offset) {
        double scale = (double)((System.currentTimeMillis() + (long)offset) % ms) / (double)ms * 3.0;
        if (scale > 2.0) {
            return ColorUtil.getGradient(color3, color1, scale - 2.0).getRGB();
        }
        if (scale > 1.0) {
            return ColorUtil.getGradient(color2, color3, scale - 1.0).getRGB();
        }
        return ColorUtil.getGradient(color1, color2, scale).getRGB();
    }

    public static Color getGradient(Color color1, Color color2, double scale) {
        scale = Math.max(0.0, Math.min(1.0, scale));
        return new Color((int)((double)color1.getRed() + (double)(color2.getRed() - color1.getRed()) * scale), (int)((double)color1.getGreen() + (double)(color2.getGreen() - color1.getGreen()) * scale), (int)((double)color1.getBlue() + (double)(color2.getBlue() - color1.getBlue()) * scale));
    }

    public static int getRainbow(long ms, int offset, float saturation, float brightness) {
        float scale = (float)((System.currentTimeMillis() + (long)offset) % ms) / (float)ms;
        return Color.HSBtoRGB(scale, saturation, brightness);
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, int start, int end, boolean trueColor) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        Color startColor = new Color(start);
        Color endColor = new Color(end);
        return trueColor ? ColorUtil.interpolateColorHue(startColor, endColor, (float)angle / 360.0f) : ColorUtil.interpolateColorC(startColor, endColor, (float)angle / 360.0f);
    }

    public static int interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return ColorUtil.interpolateColorC(color1, color2, amount).getRGB();
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return ColorUtil.interpolateColorC(cColor1, cColor2, amount).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return new Color(MathUtils.interpolateInt(color1.getRed(), color2.getRed(), amount), MathUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount), MathUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount), MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color resultColor = Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount), MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));
        return ColorUtil.applyOpacity(resultColor, (float)MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount) / 255.0f);
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        float hue = (float)angle / 360.0f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(opacity * 255.0f))));
    }

    public static Color darker(Color color, float FACTOR) {
        return new Color(Math.max((int)((float)color.getRed() * FACTOR), 0), Math.max((int)((float)color.getGreen() * FACTOR), 0), Math.max((int)((float)color.getBlue() * FACTOR), 0), color.getAlpha());
    }

    public static Color brighter(Color color, float FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        int i = (int)(1.0 / (1.0 - (double)FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }
        return new Color(Math.min((int)((float)r / FACTOR), 255), Math.min((int)((float)g / FACTOR), 255), Math.min((int)((float)b / FACTOR), 255), alpha);
    }
}

