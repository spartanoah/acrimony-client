/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.event.Listener;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.PostStepEvent;
import Acrimony.event.impl.PreStepEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.network.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Step
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "NCP");
    private final DoubleSetting height = new DoubleSetting("Height", () -> this.mode.is("Vanilla"), 1.0, 1.0, 9.0, 0.5);
    private final DoubleSetting timer = new DoubleSetting("Timer", 1.0, 0.1, 1.0, 0.05);
    private boolean prevOffGround;
    private boolean timerTick;

    public Step() {
        super("Step", Category.MOVEMENT);
        this.addSettings(this.mode, this.height, this.timer);
    }

    @Override
    public void onDisable() {
        this.prevOffGround = false;
        Step.mc.timer.timerSpeed = 1.0f;
        Step.mc.thePlayer.stepHeight = 0.6f;
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        switch (this.mode.getMode()) {
            case "Vanilla": {
                Step.mc.thePlayer.stepHeight = (float)this.height.getValue();
                break;
            }
            case "NCP": {
                Step.mc.thePlayer.stepHeight = 1.0f;
            }
        }
        if (this.timerTick) {
            Step.mc.timer.timerSpeed = 1.0f;
            this.timerTick = false;
        }
    }

    @Listener
    public void onPreStep(PreStepEvent event) {
        if (!this.mode.is("Vanilla") && Step.mc.thePlayer.onGround && this.prevOffGround && (double)event.getHeight() > 0.6) {
            event.setHeight(0.6f);
        }
    }

    @Listener
    public void onPostStep(PostStepEvent event) {
        if (event.getHeight() > 0.6f) {
            if (this.timer.getValue() < 1.0) {
                Step.mc.timer.timerSpeed = (float)this.timer.getValue();
                this.timerTick = true;
            }
            switch (this.mode.getMode()) {
                case "NCP": {
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Step.mc.thePlayer.posX, Step.mc.thePlayer.posY + 0.42, Step.mc.thePlayer.posZ, false));
                    PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Step.mc.thePlayer.posX, Step.mc.thePlayer.posY + 0.75, Step.mc.thePlayer.posZ, false));
                }
            }
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        this.prevOffGround = !event.isOnGround();
    }
}

