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
    private AcrimonyFont sfpro = new AcrimonyFont(this.getFontFromTTF("sfpro", 20.0f, 0), true, true);
    private AcrimonyFont sfprobold;
    private AcrimonyFont sfprobold22;
    private AcrimonyFont icon;

    public FontManager() {
        this.sfpro23 = new AcrimonyFont(this.getFontFromTTF("sfpro", 23.0f, 0), true, true);
        this.sfproTitle = new AcrimonyFont(this.getFontFromTTF("sfpro", 30.0f, 0), true, true);
        this.sfprobold = new AcrimonyFont(this.getFontFromTTF("sfprobold", 20.0f, 0), true, true);
        this.sfprobold22 = new AcrimonyFont(this.getFontFromTTF("sfprobold", 22.0f, 0), true, true);
        this.icon = new AcrimonyFont(this.getFontFromTTF("icon", 36.0f, 0), true, true);
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

    public AcrimonyFont getSfpro() {
        return this.sfpro;
    }

    public AcrimonyFont getSfprobold() {
        return this.sfprobold;
    }

    public AcrimonyFont getSfprobold22() {
        return this.sfprobold22;
    }

    public AcrimonyFont getIcon() {
        return this.icon;
    }
}

