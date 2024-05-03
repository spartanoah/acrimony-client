/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.Module;
import org.lwjgl.input.Keyboard;

public class Freelook
extends Module {
    private boolean wasFreelooking;

    public Freelook() {
        super("Freelook", Category.VISUAL);
        this.listenType = EventListenType.MANUAL;
        this.startListening();
    }

    @Listener
    public void onTick(TickEvent event) {
        if (Freelook.mc.thePlayer.ticksExisted < 10) {
            this.stop();
        }
        if (Keyboard.isKeyDown(this.getKey())) {
            this.wasFreelooking = true;
            Acrimony.instance.getCameraHandler().setFreelooking(true);
            Freelook.mc.gameSettings.thirdPersonView = 1;
        } else if (this.wasFreelooking) {
            this.stop();
        }
    }

    private void stop() {
        this.setEnabled(false);
        Acrimony.instance.getCameraHandler().setFreelooking(false);
        this.wasFreelooking = false;
        Freelook.mc.gameSettings.thirdPersonView = 0;
    }
}

