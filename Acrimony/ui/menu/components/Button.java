/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.menu.components;

import Acrimony.util.misc.TimerUtil;

public class Button {
    private String name;
    private boolean hovered;
    private TimerUtil animationTimer;
    private final long animInDuration = 200L;
    private final long animOutDuration = 70L;
    private boolean animationDone;

    public Button(String name) {
        this.name = name;
        this.hovered = false;
        this.animationTimer = new TimerUtil();
    }

    public void updateState(boolean state) {
        if (this.hovered != state) {
            this.animationTimer.reset();
            this.hovered = state;
            this.animationDone = false;
        }
        if (this.animationTimer.getTimeElapsed() >= (this.hovered ? 200L : 70L)) {
            this.animationDone = true;
        }
    }

    public double getMult() {
        double time = this.animationTimer.getTimeElapsed();
        return Math.min((this.hovered ? time : 70.0 - time) / (double)(this.hovered ? 200L : 70L), 1.0);
    }

    public String getName() {
        return this.name;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public TimerUtil getAnimationTimer() {
        return this.animationTimer;
    }

    public long getAnimInDuration() {
        return this.animInDuration;
    }

    public long getAnimOutDuration() {
        return this.animOutDuration;
    }

    public boolean isAnimationDone() {
        return this.animationDone;
    }

    public void setAnimationDone(boolean animationDone) {
        this.animationDone = animationDone;
    }
}

