/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.render;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.IMinecraft;
import java.util.function.Supplier;
import net.minecraft.client.gui.FontRenderer;

public class FontUtil
implements IMinecraft {
    private static FontRenderer mcFont;
    private static AcrimonyFont productSans;
    private static AcrimonyFont sfpro;
    private static AcrimonyFont sfprobold;
    private static AcrimonyFont geistbold;

    public static ModeSetting getFontSetting() {
        return new ModeSetting("Font", "Minecraft", "Minecraft", "Product sans", "Sfpro", "Sfprobold", "Geist-Bold");
    }

    public static ModeSetting getFontSetting(Supplier<Boolean> visibility) {
        return new ModeSetting("Font", visibility, "Minecraft", "Minecraft", "Product sans", "Sfpro", "Sfprobold", "Geist-Bold");
    }

    public static void initFonts() {
        mcFont = FontUtil.mc.fontRendererObj;
        productSans = Acrimony.instance.getFontManager().getProductSans();
        sfpro = Acrimony.instance.getFontManager().getSfpro();
        sfprobold = Acrimony.instance.getFontManager().getSfprobold();
        geistbold = Acrimony.instance.getFontManager().getGeistbold();
    }

    public static void drawString(String font, String text, float x, float y, int color) {
        switch (font) {
            case "Minecraft": {
                mcFont.drawString(text, x, y, color);
                break;
            }
            case "Product sans": {
                productSans.drawString(text, x, y, color);
                break;
            }
            case "Sfpro": {
                sfpro.drawString(text, x, y, color);
                break;
            }
            case "Sfprobold": {
                sfprobold.drawString(text, x, y, color);
                break;
            }
            case "Geist-Bold": {
                geistbold.drawString(text, x, y, color);
            }
        }
    }

    public static void drawStringWithShadow(String font, String text, float x, float y, int color) {
        switch (font) {
            case "Minecraft": {
                mcFont.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Product sans": {
                productSans.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Sfpro": {
                sfpro.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Sfprobold": {
                sfprobold.drawStringWithShadow(text, x, y, color);
                break;
            }
            case "Geist-Bold": {
                geistbold.drawCenteredStringWithShadow(text, x, y, color);
            }
        }
    }

    public static double getStringWidth(String font, String s) {
        switch (font) {
            case "Product sans": {
                return productSans.getStringWidth(s);
            }
            case "Sfpro": {
                return sfpro.getStringWidth(s);
            }
            case "Sfprobold": {
                return sfprobold.getStringWidth(s);
            }
            case "Geist-Bold": {
                return geistbold.getStringWidth(s);
            }
        }
        return FontUtil.mc.fontRendererObj.getStringWidth(s);
    }

    public static int getFontHeight(String font) {
        switch (font) {
            case "Product sans": {
                return productSans.getHeight();
            }
            case "Sfpro": {
                return sfpro.getHeight();
            }
            case "Sfprobold": {
                return sfprobold.getHeight();
            }
            case "Geist-Bold": {
                return geistbold.getHeight();
            }
        }
        return FontUtil.mc.fontRendererObj.FONT_HEIGHT;
    }
}

