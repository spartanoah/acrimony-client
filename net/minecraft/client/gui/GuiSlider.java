/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiSlider
extends GuiButton {
    private float sliderPosition = 1.0f;
    public boolean isMouseDown;
    private String name;
    private final float min;
    private final float max;
    private final GuiPageButtonList.GuiResponder responder;
    private FormatHelper formatHelper;

    public GuiSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String name, float min, float max, float defaultValue, FormatHelper formatter) {
        super(idIn, x, y, 150, 20, "");
        this.name = name;
        this.min = min;
        this.max = max;
        this.sliderPosition = (defaultValue - min) / (max - min);
        this.formatHelper = formatter;
        this.responder = guiResponder;
        this.displayString = this.getDisplayString();
    }

    public float func_175220_c() {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    public void func_175218_a(float p_175218_1_, boolean p_175218_2_) {
        this.sliderPosition = (p_175218_1_ - this.min) / (this.max - this.min);
        this.displayString = this.getDisplayString();
        if (p_175218_2_) {
            this.responder.onTick(this.id, this.func_175220_c());
        }
    }

    public float func_175217_d() {
        return this.sliderPosition;
    }

    private String getDisplayString() {
        return this.formatHelper == null ? I18n.format(this.name, new Object[0]) + ": " + this.func_175220_c() : this.formatHelper.getText(this.id, I18n.format(this.name, new Object[0]), this.func_175220_c());
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.isMouseDown) {
                this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                if (this.sliderPosition < 0.0f) {
                    this.sliderPosition = 0.0f;
                }
                if (this.sliderPosition > 1.0f) {
                    this.sliderPosition = 1.0f;
                }
                this.displayString = this.getDisplayString();
                this.responder.onTick(this.id, this.func_175220_c());
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    public void func_175219_a(float p_175219_1_) {
        this.sliderPosition = p_175219_1_;
        this.displayString = this.getDisplayString();
        this.responder.onTick(this.id, this.func_175220_c());
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            if (this.sliderPosition < 0.0f) {
                this.sliderPosition = 0.0f;
            }
            if (this.sliderPosition > 1.0f) {
                this.sliderPosition = 1.0f;
            }
            this.displayString = this.getDisplayString();
            this.responder.onTick(this.id, this.func_175220_c());
            this.isMouseDown = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.isMouseDown = false;
    }

    public static interface FormatHelper {
        public String getText(int var1, String var2, float var3);
    }
}

