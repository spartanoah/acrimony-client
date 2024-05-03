/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import Acrimony.Acrimony;
import Acrimony.font.AcrimonyFont;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.render.DrawUtil;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButton
extends Gui {
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    protected int width = 200;
    protected int height = 20;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled = true;
    public boolean visible = true;
    protected boolean hovered;
    public float alp;

    public GuiButton(int buttonId, int x, int y, String buttonText) {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    protected int getHoverState(boolean mouseOver) {
        int i = 1;
        if (!this.enabled) {
            i = 0;
        } else if (mouseOver) {
            i = 2;
        }
        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        AcrimonyFont sfpro = Acrimony.instance.getFontManager().getSfpro();
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.mouseDragged(mc, mouseX, mouseY);
            int xpos = this.xPosition;
            int ypos = this.yPosition;
            int width = this.width + this.xPosition;
            int height = this.height + this.yPosition;
            int j = 0xE0E0E0;
            if (!this.enabled) {
                j = 0xA0A0A0;
            } else if (this.hovered) {
                j = 0xFFFFA0;
            }
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.alp = this.hovered ? TimerUtil.moveUD(this.alp, 160.0f, (float)(5.0 * TimerUtil.deltaTime()), (float)(5.0 * TimerUtil.deltaTime())) : TimerUtil.moveUD(this.alp, 80.0f, (float)(3.0 * TimerUtil.deltaTime()), (float)(5.0 * TimerUtil.deltaTime()));
            DrawUtil.drawRoundedRect(xpos, ypos, width, height, 4.0, new Color(10, 10, 10, (int)this.alp).getRGB());
            sfpro.drawCenteredString(this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    }

    public void mouseReleased(int mouseX, int mouseY) {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver() {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY) {
    }

    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0f));
    }

    public int getButtonWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

