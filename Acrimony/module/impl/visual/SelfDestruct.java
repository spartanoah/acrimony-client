/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.module.Category;
import Acrimony.module.Module;

public class SelfDestruct
extends Module {
    public SelfDestruct() {
        super("Self Destruct", Category.EXPLOIT);
    }

    @Override
    public void onEnable() {
        Acrimony.instance.getModuleManager().modules.forEach(m -> m.setEnabled(false));
        Acrimony.instance.getPacketDelayHandler().stopAll();
        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
        Acrimony.instance.getCameraHandler().setFreelooking(false);
        Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
        mc.displayGuiScreen(null);
        SelfDestruct.mc.timer.timerSpeed = 1.0f;
        Acrimony.instance.setDestructed(true);
    }
}

