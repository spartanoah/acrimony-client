/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.event.Listener;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.util.network.ServerUtil;

public class Sprint
extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT);
        this.setEnabledSilently(true);
    }

    @Override
    public void onDisable() {
        Sprint.mc.gameSettings.keyBindSprint.pressed = false;
    }

    @Listener(value=1)
    public void onTick(TickEvent event) {
        Sprint.mc.gameSettings.keyBindSprint.pressed = true;
        if (Sprint.mc.thePlayer.moveForward <= 0.0f && ServerUtil.isOnHypixel()) {
            Sprint.mc.thePlayer.setSprinting(false);
        }
    }
}

