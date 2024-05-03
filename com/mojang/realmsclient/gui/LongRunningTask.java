/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.GuiCallback;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraft.realms.RealmsButton;

public abstract class LongRunningTask
implements Runnable,
ErrorCallback,
GuiCallback {
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

    public void setScreen(RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen) {
        this.longRunningMcoTaskScreen = longRunningMcoTaskScreen;
    }

    @Override
    public void error(String errorMessage) {
        this.longRunningMcoTaskScreen.error(errorMessage);
    }

    public void setTitle(String title) {
        this.longRunningMcoTaskScreen.setTitle(title);
    }

    public boolean aborted() {
        return this.longRunningMcoTaskScreen.aborted();
    }

    @Override
    public void tick() {
    }

    @Override
    public void buttonClicked(RealmsButton button) {
    }

    public void init() {
    }

    public void abortTask() {
    }
}

