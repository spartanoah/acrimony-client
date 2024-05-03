/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.movement.Fly;
import Acrimony.module.impl.movement.Longjump;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.ModuleUtil;
import Acrimony.util.world.WorldUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

public class Antivoid
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Flag", "Flag", "Collision flag", "Blink", "Bounce");
    private final BooleanSetting stopHorizontalMove = new BooleanSetting("Stop horizontal move", () -> this.mode.is("Blink"), false);
    private final DoubleSetting bounceMotion = new DoubleSetting("Bounce motion", () -> this.mode.is("Bounce"), 1.5, 0.4, 3.0, 0.1);
    private final DoubleSetting minFallDist = new DoubleSetting("Min fall dist", 3.5, 2.0, 10.0, 0.25);
    private PlayerInfo lastSafePos;
    private BlockPos collisionBlock;
    private boolean blinking;
    private Fly flyModule;
    private Longjump longjumpModule;
    private boolean receivedLagback;

    public Antivoid() {
        super("Antivoid", Category.PLAYER);
        this.addSettings(this.mode, this.stopHorizontalMove, this.bounceMotion, this.minFallDist);
    }

    @Override
    public void onEnable() {
        this.collisionBlock = null;
        this.lastSafePos = new PlayerInfo(Antivoid.mc.thePlayer.posX, Antivoid.mc.thePlayer.posY, Antivoid.mc.thePlayer.posZ, Antivoid.mc.thePlayer.motionX, Antivoid.mc.thePlayer.motionY, Antivoid.mc.thePlayer.motionZ, Antivoid.mc.thePlayer.rotationYaw, Antivoid.mc.thePlayer.rotationPitch, Antivoid.mc.thePlayer.onGround, Antivoid.mc.thePlayer.fallDistance, Antivoid.mc.thePlayer.inventory.currentItem);
    }

    @Override
    public void onDisable() {
        if (this.blinking) {
            ModuleUtil.getBlink().stopBlinking();
            this.blinking = false;
        }
        this.receivedLagback = false;
    }

    @Override
    public void onClientStarted() {
        this.flyModule = ModuleUtil.getFly();
        this.longjumpModule = ModuleUtil.getLongjump();
    }

    @Listener(value=0)
    public void onTick(TickEvent event) {
        if (Antivoid.mc.thePlayer.ticksExisted < 10) {
            this.collisionBlock = null;
            return;
        }
        switch (this.mode.getMode()) {
            case "Bounce": {
                if (!this.shouldSetback() || !(Antivoid.mc.thePlayer.motionY < -0.1)) break;
                Antivoid.mc.thePlayer.motionY = this.bounceMotion.getValue();
                break;
            }
            case "Collision flag": {
                if (this.shouldSetback()) {
                    if (this.collisionBlock != null) {
                        Antivoid.mc.theWorld.setBlockToAir(this.collisionBlock);
                    }
                    this.collisionBlock = new BlockPos(Antivoid.mc.thePlayer.posX, Antivoid.mc.thePlayer.posY - 1.0, Antivoid.mc.thePlayer.posZ);
                    Antivoid.mc.theWorld.setBlockState(this.collisionBlock, Blocks.barrier.getDefaultState());
                    break;
                }
                if (this.collisionBlock == null) break;
                Antivoid.mc.theWorld.setBlockToAir(this.collisionBlock);
                this.collisionBlock = null;
                break;
            }
            case "Blink": {
                if (this.isSafe()) {
                    this.lastSafePos = new PlayerInfo(Antivoid.mc.thePlayer.posX, Antivoid.mc.thePlayer.posY, Antivoid.mc.thePlayer.posZ, Antivoid.mc.thePlayer.motionX, Antivoid.mc.thePlayer.motionY, Antivoid.mc.thePlayer.motionZ, Antivoid.mc.thePlayer.rotationYaw, Antivoid.mc.thePlayer.rotationPitch, Antivoid.mc.thePlayer.onGround, Antivoid.mc.thePlayer.fallDistance, Antivoid.mc.thePlayer.inventory.currentItem);
                    this.receivedLagback = false;
                    if (!this.blinking) break;
                    ModuleUtil.stopBlinking();
                    this.blinking = false;
                    break;
                }
                if (this.receivedLagback) break;
                if (this.shouldSetback()) {
                    if (!this.blinking) break;
                    Antivoid.mc.thePlayer.setPosition(this.lastSafePos.x, this.lastSafePos.y, this.lastSafePos.z);
                    if (this.stopHorizontalMove.isEnabled()) {
                        Antivoid.mc.thePlayer.motionX = 0.0;
                        Antivoid.mc.thePlayer.motionZ = 0.0;
                    } else {
                        Antivoid.mc.thePlayer.motionX = this.lastSafePos.motionX;
                        Antivoid.mc.thePlayer.motionZ = this.lastSafePos.motionZ;
                    }
                    Antivoid.mc.thePlayer.motionY = this.lastSafePos.motionY;
                    Antivoid.mc.thePlayer.rotationYaw = this.lastSafePos.yaw;
                    Antivoid.mc.thePlayer.rotationPitch = this.lastSafePos.pitch;
                    Antivoid.mc.thePlayer.onGround = this.lastSafePos.onGround;
                    Antivoid.mc.thePlayer.fallDistance = this.lastSafePos.fallDist;
                    Antivoid.mc.thePlayer.inventory.currentItem = this.lastSafePos.itemSlot;
                    Antivoid.mc.playerController.currentPlayerItem = this.lastSafePos.itemSlot;
                    Acrimony.instance.getPacketBlinkHandler().releasePingPackets();
                    Acrimony.instance.getPacketBlinkHandler().clearPackets();
                    break;
                }
                if (this.blinking) break;
                ModuleUtil.startBlinking(this, false);
                this.blinking = true;
            }
        }
    }

    @Listener(value=4)
    public void onMotion(MotionEvent event) {
        switch (this.mode.getMode()) {
            case "Flag": {
                if (!this.shouldSetback()) break;
                event.setY(event.getY() + 8.0 + Math.random());
            }
        }
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook)event.getPacket();
            if (this.mode.is("Blink") && this.blinking) {
                Antivoid.mc.thePlayer.onGround = false;
                Antivoid.mc.thePlayer.fallDistance = this.lastSafePos.fallDist;
                Antivoid.mc.thePlayer.inventory.currentItem = this.lastSafePos.itemSlot;
                Antivoid.mc.playerController.currentPlayerItem = this.lastSafePos.itemSlot;
                Acrimony.instance.getPacketBlinkHandler().releasePingPackets();
                Acrimony.instance.getPacketBlinkHandler().clearPackets();
                ModuleUtil.stopBlinking();
                this.lastSafePos = new PlayerInfo(packet.getX(), packet.getY(), packet.getZ(), 0.0, 0.0, 0.0, packet.getYaw(), packet.getPitch(), false, Antivoid.mc.thePlayer.fallDistance, Antivoid.mc.thePlayer.inventory.currentItem);
                this.blinking = false;
                this.receivedLagback = true;
            }
        }
    }

    private boolean shouldSetback() {
        return (double)Antivoid.mc.thePlayer.fallDistance >= this.minFallDist.getValue() && !WorldUtil.isBlockUnder() && Antivoid.mc.thePlayer.ticksExisted >= 100;
    }

    private boolean isSafe() {
        return WorldUtil.isBlockUnder() || !Antivoid.mc.getNetHandler().doneLoadingTerrain || Antivoid.mc.thePlayer.ticksExisted < 100 || this.flyModule.isEnabled() || this.longjumpModule.isEnabled();
    }

    public boolean isBlinking() {
        return this.blinking;
    }

    private class PlayerInfo {
        private final double x;
        private final double y;
        private final double z;
        private final double motionX;
        private final double motionY;
        private final double motionZ;
        private final float yaw;
        private final float pitch;
        private final boolean onGround;
        private final float fallDist;
        private final int itemSlot;

        private PlayerInfo(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float pitch, boolean onGround, float fallDist, int itemSlot) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.yaw = yaw;
            this.pitch = pitch;
            this.onGround = onGround;
            this.fallDist = fallDist;
            this.itemSlot = itemSlot;
        }
    }
}

