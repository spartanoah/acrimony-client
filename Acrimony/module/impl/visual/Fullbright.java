/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.event.Listener;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;

public class Fullbright
extends Module {
    public Fullbright() {
        super("Fullbright", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        Fullbright.mc.gameSettings.gammaSetting = 100.0f;
    }

    @Override
    public void onDisable() {
        Fullbright.mc.gameSettings.gammaSetting = 1.0f;
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        Fullbright.mc.gameSettings.gammaSetting = 100.0f;
    }
}

