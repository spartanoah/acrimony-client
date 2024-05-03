/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.MoveEvent;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.player.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class Longjump
extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Hycraft", "Self damage");
    private final DoubleSetting motionY = new DoubleSetting("Motion Y", () -> this.mode.is("Vanilla"), 0.4, 0.1, 9.0, 0.1);
    private final DoubleSetting speed = new DoubleSetting("Speed", () -> this.mode.is("Vanilla"), 1.0, 0.1, 9.0, 0.1);
    private final BooleanSetting stopMovement = new BooleanSetting("Stop movement", () -> this.mode.is("Self damage"), false);
    private final IntegerSetting waitingTicks = new IntegerSetting("Waiting ticks", () -> this.mode.is("Self damage"), 10, 4, 20, 1);
    private final ModeSetting horizontalMove = new ModeSetting("Horizontal move", () -> this.mode.is("Self damage"), "Ignore", "Legit", "Ignore", "Boost", "Verus");
    private final DoubleSetting horizontalBoostAmount = new DoubleSetting("Horizontal boost amount", () -> this.mode.is("Self damage") && this.horizontalMove.is("Boost"), 0.2, 0.02, 1.0, 0.02);
    private final DoubleSetting afterVelocityYBoost = new DoubleSetting("After velocity Y boost", () -> this.mode.is("Self damage"), 0.0, 0.0, 0.08, 0.002);
    private boolean started;
    private int counter;
    private int ticks;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public Longjump() {
        super("Longjump", Category.MOVEMENT);
        this.addSettings(this.mode, this.motionY, this.speed, this.stopMovement, this.waitingTicks, this.horizontalMove, this.horizontalBoostAmount, this.afterVelocityYBoost);
    }

    @Override
    public void onEnable() {
        this.ticks = 0;
        this.counter = 0;
        this.started = false;
        this.velocityY = -1.0;
        switch (this.mode.getMode()) {
            case "Self damage": {
                if (Longjump.mc.thePlayer.onGround) break;
                this.counter = 3;
            }
        }
    }

    @Override
    public void onDisable() {
        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
        Longjump.mc.timer.timerSpeed = 1.0f;
        switch (this.mode.getMode()) {
            case "Vanilla": {
                MovementUtil.strafe(0.1);
            }
        }
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        switch (this.mode.getMode()) {
            case "Self damage": {
                if (this.started) {
                    ++this.ticks;
                    if (this.ticks == this.waitingTicks.getValue() && this.velocityY != -1.0) {
                        switch (this.horizontalMove.getMode()) {
                            case "Legit": {
                                Longjump.mc.thePlayer.motionX = this.velocityX;
                                Longjump.mc.thePlayer.motionZ = this.velocityZ;
                                break;
                            }
                            case "Ignore": {
                                break;
                            }
                            case "Boost": {
                                Longjump.mc.thePlayer.motionX -= (double)MathHelper.sin((float)Math.toRadians(Longjump.mc.thePlayer.rotationYaw)) * this.horizontalBoostAmount.getValue();
                                Longjump.mc.thePlayer.motionZ += (double)MathHelper.cos((float)Math.toRadians(Longjump.mc.thePlayer.rotationYaw)) * this.horizontalBoostAmount.getValue();
                            }
                        }
                        Longjump.mc.thePlayer.motionY = this.velocityY;
                    }
                    if (this.ticks > this.waitingTicks.getValue() && this.velocityY != -1.0) {
                        Longjump.mc.thePlayer.motionY += this.afterVelocityYBoost.getValue();
                    }
                    if (this.ticks > 2 && this.ticks < this.waitingTicks.getValue()) {
                        Acrimony.instance.getPacketBlinkHandler().startBlinking();
                    } else {
                        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
                    }
                    if (!Longjump.mc.thePlayer.onGround) break;
                    this.setEnabled(false);
                    break;
                }
                if (Longjump.mc.thePlayer.onGround) {
                    Longjump.mc.thePlayer.jump();
                    ++this.counter;
                    if (this.counter > 3) {
                        this.started = true;
                        Longjump.mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(Longjump.mc.gameSettings.keyBindForward.getKeyCode());
                        return;
                    }
                }
                if (Longjump.mc.thePlayer.motionY > 0.3) {
                    Acrimony.instance.getPacketBlinkHandler().stopBlinking();
                    break;
                }
                Acrimony.instance.getPacketBlinkHandler().startBlinking();
                break;
            }
            case "Hycraft": {
                if (Longjump.mc.thePlayer.onGround) {
                    if (this.started) {
                        this.setEnabled(false);
                        return;
                    }
                    if (Longjump.mc.gameSettings.keyBindJump.isKeyDown()) break;
                    Longjump.mc.thePlayer.jump();
                    this.started = true;
                    break;
                }
                if (this.ticks >= 2 && this.ticks <= 8) {
                    Longjump.mc.thePlayer.motionY += 0.07;
                }
                ++this.ticks;
            }
        }
    }

    @Listener
    public void onEntityAction(EntityActionEvent event) {
        if (this.mode.is("Self damage") && this.stopMovement.isEnabled() && this.counter < 4) {
            event.setSprinting(false);
        }
    }

    @Listener
    public void onMove(MoveEvent event) {
        switch (this.mode.getMode()) {
            case "Vanilla": {
                if (Longjump.mc.thePlayer.onGround) {
                    if (this.started) {
                        this.setEnabled(false);
                        return;
                    }
                    if (MovementUtil.isMoving()) {
                        this.started = true;
                        if (this.motionY.getValue() == 0.4) {
                            event.setY(0.42f);
                        } else {
                            Longjump.mc.thePlayer.motionY = this.motionY.getValue();
                            event.setY(Longjump.mc.thePlayer.motionY);
                        }
                    }
                }
                MovementUtil.strafe(event, this.speed.getValue());
                break;
            }
            case "Self damage": {
                if (this.stopMovement.isEnabled()) {
                    if (this.counter <= 3) {
                        MovementUtil.strafe(event, 0.0);
                    } else if (this.counter == 4 && Longjump.mc.thePlayer.onGround && event.getY() > 0.4) {
                        if (Longjump.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtil.strafe(event, 0.6 + (double)MovementUtil.getSpeedAmplifier() * 0.07);
                        } else {
                            MovementUtil.strafe(event, 0.6);
                        }
                    }
                }
                if (!this.started || !this.horizontalMove.is("Verus")) break;
                if (this.ticks >= this.waitingTicks.getValue() + 14) {
                    MovementUtil.strafe(event, 0.28);
                    break;
                }
                if (this.ticks < this.waitingTicks.getValue()) break;
                MovementUtil.strafe(event, 9.0);
            }
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        switch (this.mode.getMode()) {
            case "Self damage": {
                if (this.started || this.counter >= 3) break;
                event.setOnGround(false);
            }
        }
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        switch (this.mode.getMode()) {
            case "Self damage": {
                S12PacketEntityVelocity packet;
                if (!(event.getPacket() instanceof S12PacketEntityVelocity) || (packet = (S12PacketEntityVelocity)event.getPacket()).getEntityID() != Longjump.mc.thePlayer.getEntityId()) break;
                event.setCancelled(true);
                this.velocityX = (double)packet.getMotionX() / 8000.0;
                this.velocityY = (double)packet.getMotionY() / 8000.0;
                this.velocityZ = (double)packet.getMotionZ() / 8000.0;
            }
        }
    }

    @Override
    public String getSuffix() {
        return this.mode.getMode();
    }
}

