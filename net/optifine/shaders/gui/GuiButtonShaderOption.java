/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders.gui;

import net.minecraft.client.gui.GuiButton;
import net.optifine.shaders.config.ShaderOption;

public class GuiButtonShaderOption
extends GuiButton {
    private ShaderOption shaderOption = null;

    public GuiButtonShaderOption(int buttonId, int x, int y, int widthIn, int heightIn, ShaderOption shaderOption, String text) {
        super(buttonId, x, y, widthIn, heightIn, text);
        this.shaderOption = shaderOption;
    }

    public ShaderOption getShaderOption() {
        return this.shaderOption;
    }

    public void valueChanged() {
    }

    public boolean isSwitchable() {
        return true;
    }
}

