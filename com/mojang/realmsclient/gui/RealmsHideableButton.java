/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui;

import net.minecraft.realms.RealmsButton;

public class RealmsHideableButton
extends RealmsButton {
    boolean visible = true;

    public RealmsHideableButton(int id, int x, int y, int width, int height, String msg) {
        super(id, x, y, width, height, msg);
    }

    @Override
    public void render(int xm, int ym) {
        if (!this.visible) {
            return;
        }
        super.render(xm, ym);
    }

    @Override
    public void clicked(int mx, int my) {
        if (!this.visible) {
            return;
        }
        super.clicked(mx, my);
    }

    @Override
    public void released(int mx, int my) {
        if (!this.visible) {
            return;
        }
        super.released(mx, my);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return this.visible;
    }
}

