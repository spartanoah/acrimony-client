/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class RealmsClientOutdatedScreen
extends RealmsScreen {
    private static final int BUTTON_BACK_ID = 0;
    private final RealmsScreen lastScreen;
    private final boolean outdated;

    public RealmsClientOutdatedScreen(RealmsScreen lastScreen, boolean outdated) {
        this.lastScreen = lastScreen;
        this.outdated = outdated;
    }

    @Override
    public void init() {
        this.buttonsClear();
        this.buttonsAdd(RealmsClientOutdatedScreen.newButton(0, this.width() / 2 - 100, RealmsConstants.row(12), "Back"));
    }

    @Override
    public void render(int xm, int ym, float a) {
        this.renderBackground();
        String title = this.outdated ? RealmsClientOutdatedScreen.getLocalizedString("mco.client.outdated.title") : RealmsClientOutdatedScreen.getLocalizedString("mco.client.incompatible.title");
        String msg = this.outdated ? RealmsClientOutdatedScreen.getLocalizedString("mco.client.outdated.msg") : RealmsClientOutdatedScreen.getLocalizedString("mco.client.incompatible.msg");
        this.drawCenteredString(title, this.width() / 2, RealmsConstants.row(3), 0xFF0000);
        String[] messages = msg.split("\\\\n");
        int index = 0;
        for (String message : messages) {
            this.drawCenteredString(message, this.width() / 2, RealmsConstants.row(5) + index * 12, 0xFFFFFF);
            ++index;
        }
        super.render(xm, ym, a);
    }

    @Override
    public void buttonClicked(RealmsButton button) {
        if (button.id() == 0) {
            Realms.setScreen(this.lastScreen);
        }
    }

    @Override
    public void keyPressed(char eventCharacter, int eventKey) {
        if (eventKey == 28 || eventKey == 156 || eventKey == 1) {
            Realms.setScreen(this.lastScreen);
        }
    }
}

