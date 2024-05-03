/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.event.Listener;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;

public class Timer
extends Module {
    private final DoubleSetting speed = new DoubleSetting("Speed", 1.1, 0.1, 5.0, 0.1);

    public Timer() {
        super("Timer", Category.PLAYER);
        this.addSettings(this.speed);
    }

    @Override
    public void onDisable() {
        Timer.mc.timer.timerSpeed = 1.0f;
    }

    @Listener
    public void onPostMotion(PostMotionEvent event) {
        Timer.mc.timer.timerSpeed = (float)this.speed.getValue();
    }
}

