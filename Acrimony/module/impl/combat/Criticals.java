/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.combat;

import Acrimony.event.Listener;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.ModuleUtil;
import Acrimony.util.misc.LogUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MovingObjectPosition;

public class Criticals
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Edit", "Norules", "Noground");
    private final IntegerSetting maxGroundTicks = new IntegerSetting("Max ground ticks", () -> this.mode.is("Legit"), 4, 1, 10, 1);
    private final IntegerSetting minHurtTime = new IntegerSetting("Min hurt time", () -> this.mode.is("Edit"), 1, 1, 10, 1);
    private final IntegerSetting maxHurtTime = new IntegerSetting("Max hurt time", () -> this.mode.is("Edit"), 4, 2, 10, 1);
    private final DoubleSetting yOffset = new DoubleSetting("Y offset", () -> this.mode.is("Packet") || this.mode.is("Edit"), 0.01, 5.0E-4, 0.1, 5.0E-4);
    private final BooleanSetting applyOffsetTwoTicks = new BooleanSetting("Apply offset 2 ticks", () -> this.mode.is("Edit"), false);
    private final BooleanSetting randomisation = new BooleanSetting("Randomisation", () -> this.mode.is("Packet") || this.mode.is("Edit"), true);
    private final BooleanSetting backtrackCursorTarget = new BooleanSetting("Backtrack cursor target", () -> this.mode.is("Norules") || this.mode.is("Edit") || this.mode.is("Noground"), true);
    private final IntegerSetting keepCursorTargetTicks = new IntegerSetting("Keep cursor target ticks", () -> (this.mode.is("Norules") || this.mode.is("Edit") || this.mode.is("Noground")) && this.backtrackCursorTarget.isEnabled(), 10, 0, 50, 1);
    private EntityLivingBase lastCursorTarget;
    private int cursorTargetTicks;
    private boolean falling;

    public Criticals() {
        super("Criticals", Category.COMBAT);
        this.addSettings(this.mode, this.minHurtTime, this.maxHurtTime, this.yOffset, this.applyOffsetTwoTicks, this.randomisation, this.maxGroundTicks, this.backtrackCursorTarget, this.keepCursorTargetTicks);
    }

    @Override
    public void onEnable() {
        this.lastCursorTarget = null;
        this.cursorTargetTicks = 0;
    }

    @Override
    public void onDisable() {
    }

    @Listener
    public void onSend(PacketSendEvent event) {
        C02PacketUseEntity packet;
        if (event.getPacket() instanceof C02PacketUseEntity && (packet = (C02PacketUseEntity)event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK && this.mode.is("Packet")) {
            double offset = this.yOffset.getValue();
            if (this.randomisation.isEnabled()) {
                offset -= Math.random() * 1.0E-4;
            }
            mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Criticals.mc.thePlayer.posX, Criticals.mc.thePlayer.posY + offset, Criticals.mc.thePlayer.posZ, false));
            mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Criticals.mc.thePlayer.posX, Criticals.mc.thePlayer.posY, Criticals.mc.thePlayer.posZ, false));
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        if (Criticals.mc.thePlayer.onGround) {
            this.falling = false;
        } else if (Criticals.mc.thePlayer.motionY < 0.0) {
            this.falling = true;
        }
        Killaura killauraModule = ModuleUtil.getKillaura();
        if (!killauraModule.isEnabled() || killauraModule.getTarget() == null) {
            this.cursorTargetTicks = Criticals.mc.objectMouseOver != null && Criticals.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && Criticals.mc.objectMouseOver.entityHit instanceof EntityLivingBase ? 0 : (this.lastCursorTarget != null ? ++this.cursorTargetTicks : 0);
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        EntityLivingBase target = this.getCurrentTarget();
        if (target != null) {
            switch (this.mode.getMode()) {
                case "Norules": {
                    if (!Criticals.mc.thePlayer.onGround || target.hurtTime >= 4 || target.hurtTime <= 0) break;
                    if (target.hurtTime == 3) {
                        event.setY(event.getY() + 0.0784000015258789);
                    }
                    event.setOnGround(false);
                    break;
                }
                case "Edit": {
                    if (!Criticals.mc.thePlayer.onGround || target.hurtTime > this.maxHurtTime.getValue() || target.hurtTime < this.minHurtTime.getValue()) break;
                    if (target.hurtTime == this.maxHurtTime.getValue() || target.hurtTime == this.maxHurtTime.getValue() - 1 && this.applyOffsetTwoTicks.isEnabled()) {
                        double offset = this.yOffset.getValue();
                        if (this.randomisation.isEnabled()) {
                            offset -= Math.random() * 1.0E-4;
                        }
                        LogUtil.addChatMessage("Crit1");
                        event.setY(event.getY() + offset);
                    } else {
                        LogUtil.addChatMessage("Crit2");
                    }
                    event.setOnGround(false);
                    break;
                }
                case "Noground": {
                    event.setOnGround(false);
                }
            }
        }
    }

    private EntityLivingBase getCurrentTarget() {
        Killaura killauraModule = ModuleUtil.getKillaura();
        if (killauraModule.isEnabled() && killauraModule.getTarget() != null) {
            return killauraModule.getTarget();
        }
        if (Criticals.mc.objectMouseOver != null && Criticals.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && Criticals.mc.objectMouseOver.entityHit instanceof EntityLivingBase && this.backtrackCursorTarget.isEnabled()) {
            this.lastCursorTarget = (EntityLivingBase)Criticals.mc.objectMouseOver.entityHit;
            return (EntityLivingBase)Criticals.mc.objectMouseOver.entityHit;
        }
        if (this.lastCursorTarget != null && this.backtrackCursorTarget.isEnabled()) {
            if (this.cursorTargetTicks > this.keepCursorTargetTicks.getValue()) {
                this.lastCursorTarget = null;
            } else {
                return this.lastCursorTarget;
            }
        }
        return null;
    }
}

