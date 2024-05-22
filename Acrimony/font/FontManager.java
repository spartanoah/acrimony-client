/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.font;

import Acrimony.font.AcrimonyFont;
import java.awt.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class FontManager {
    private AcrimonyFont productSans = new AcrimonyFont(this.getFontFromTTF("product_sans", 20.0f, 0), true, true);
    private AcrimonyFont sfpro23;
    private AcrimonyFont sfproTitle;
    private AcrimonyFont sfprosmall = new AcrimonyFont(this.getFontFromTTF("sfpro", 18.0f, 0), true, true);
    private AcrimonyFont sfproverysmall = new AcrimonyFont(this.getFontFromTTF("sfpro", 10.0f, 0), true, true);
    private AcrimonyFont sfpro = new AcrimonyFont(this.getFontFromTTF("sfpro", 20.0f, 0), true, true);
    private AcrimonyFont sfprobold;
    private AcrimonyFont sfprobold22;
    private AcrimonyFont geistbold;
    private AcrimonyFont icon;
    private AcrimonyFont iconregular;
    private AcrimonyFont iconlarge;
    private AcrimonyFont icon1;
    private AcrimonyFont icon1regular;
    private AcrimonyFont icon1large;
    private AcrimonyFont jellolight;
    private AcrimonyFont jellomedium;
    private AcrimonyFont jellosemibold;
    private AcrimonyFont jellosemiboldTitle;
    private AcrimonyFont jelloregularTitle;
    private AcrimonyFont jelloregularSubTitle;
    private AcrimonyFont jelloregular;

    public FontManager() {
        this.sfpro23 = new AcrimonyFont(this.getFontFromTTF("sfpro", 23.0f, 0), true, true);
        this.sfproTitle = new AcrimonyFont(this.getFontFromTTF("sfpro", 30.0f, 0), true, true);
        this.sfprobold = new AcrimonyFont(this.getFontFromTTF("sfprobold", 20.0f, 0), true, true);
        this.sfprobold22 = new AcrimonyFont(this.getFontFromTTF("sfprobold", 22.0f, 0), true, true);
        this.icon = new AcrimonyFont(this.getFontFromTTF("icon", 36.0f, 0), true, true);
        this.iconregular = new AcrimonyFont(this.getFontFromTTF("icon", 22.0f, 0), true, true);
        this.iconlarge = new AcrimonyFont(this.getFontFromTTF("icon", 26.0f, 0), true, true);
        this.icon1 = new AcrimonyFont(this.getFontFromTTF("icon1", 36.0f, 0), true, true);
        this.icon1regular = new AcrimonyFont(this.getFontFromTTF("icon1", 22.0f, 0), true, true);
        this.icon1large = new AcrimonyFont(this.getFontFromTTF("icon1", 26.0f, 0), true, true);
        this.jellolight = new AcrimonyFont(this.getFontFromTTF("Urbanist-Light", 20.0f, 0), true, true);
        this.jellomedium = new AcrimonyFont(this.getFontFromTTF("Urbanist-Medium", 20.0f, 0), true, true);
        this.jelloregular = new AcrimonyFont(this.getFontFromTTF("Urbanist-Regular", 20.0f, 0), true, true);
        this.jelloregularSubTitle = new AcrimonyFont(this.getFontFromTTF("Urbanist-Regular", 26.0f, 0), true, true);
        this.jelloregularTitle = new AcrimonyFont(this.getFontFromTTF("Urbanist-Regular", 52.0f, 0), true, true);
        this.jellosemibold = new AcrimonyFont(this.getFontFromTTF("Urbanist-SemiBold", 20.0f, 0), true, true);
        this.jellosemiboldTitle = new AcrimonyFont(this.getFontFromTTF("Urbanist-SemiBold", 32.0f, 0), true, true);
        this.geistbold = new AcrimonyFont(this.getFontFromTTF("Geist-Bold", 32.0f, 0), true, true);
    }

    public Font getFontFromTTF(String fontName, float fontSize, int fontType) {
        Font output = null;
        ResourceLocation fontLocation = new ResourceLocation("acrimony/fonts/" + fontName + ".ttf");
        try {
            output = Font.createFont(fontType, Minecraft.getMinecraft().getResourceManager().getResource(fontLocation).getInputStream());
            output = output.deriveFont(fontSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public AcrimonyFont getProductSans() {
        return this.productSans;
    }

    public AcrimonyFont getSfpro23() {
        return this.sfpro23;
    }

    public AcrimonyFont getSfproTitle() {
        return this.sfproTitle;
    }

    public AcrimonyFont getSfprosmall() {
        return this.sfprosmall;
    }

    public AcrimonyFont getSfproverysmall() {
        return this.sfproverysmall;
    }

    public AcrimonyFont getSfpro() {
        return this.sfpro;
    }

    public AcrimonyFont getSfprobold() {
        return this.sfprobold;
    }

    public AcrimonyFont getSfprobold22() {
        return this.sfprobold22;
    }

    public AcrimonyFont getGeistbold() {
        return this.geistbold;
    }

    public AcrimonyFont getIcon() {
        return this.icon;
    }

    public AcrimonyFont getIconregular() {
        return this.iconregular;
    }

    public AcrimonyFont getIconlarge() {
        return this.iconlarge;
    }

    public AcrimonyFont getIcon1() {
        return this.icon1;
    }

    public AcrimonyFont getIcon1regular() {
        return this.icon1regular;
    }

    public AcrimonyFont getIcon1large() {
        return this.icon1large;
    }

    public AcrimonyFont getJellolight() {
        return this.jellolight;
    }

    public AcrimonyFont getJellomedium() {
        return this.jellomedium;
    }

    public AcrimonyFont getJellosemibold() {
        return this.jellosemibold;
    }

    public AcrimonyFont getJellosemiboldTitle() {
        return this.jellosemiboldTitle;
    }

    public AcrimonyFont getJelloregularTitle() {
        return this.jelloregularTitle;
    }

    public AcrimonyFont getJelloregularSubTitle() {
        return this.jelloregularSubTitle;
    }

    public AcrimonyFont getJelloregular() {
        return this.jelloregular;
    }
}

