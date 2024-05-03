/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.event.Listener;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.RenderEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class TimeChanger
extends Module {
    private final DoubleSetting customTime = new DoubleSetting("Custom time", 18000.0, 0.0, 24000.0, 500.0);

    public TimeChanger() {
        super("Time Changer", Category.VISUAL);
        this.addSettings(this.customTime);
    }

    @Listener
    public void onRender(RenderEvent event) {
        TimeChanger.mc.theWorld.setWorldTime((long)this.customTime.getValue());
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    }
}

