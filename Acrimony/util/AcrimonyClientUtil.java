/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.event.impl.VelocityEvent;
import Acrimony.util.IMinecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;

public class AcrimonyClientUtil
implements IMinecraft {
    private int velocityTicks;
    private boolean resetted;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double velocityDist;
    private int groundTicks;
    private int offGroundTicks;
    private boolean pendingSlotSwitch;
    private int ticksExisted;
    private int pendingSlot;
    private boolean lastServerSprinting;
    private boolean lastServerSneaking;
    private boolean invOpened;
    private int ticksSinceInvOpened;
    private boolean chestOpened;
    private int ticksSinceChestOpened;

    public AcrimonyClientUtil() {
        Acrimony.instance.getEventManager().register(this);
        this.reset();
    }

    private void reset() {
        this.velocityTicks = 1000;
        this.ticksSinceInvOpened = 1000;
    }

    @Listener(value=-128)
    public void onTick(TickEvent event) {
        boolean invOpened;
        ++this.velocityTicks;
        if (AcrimonyClientUtil.mc.thePlayer.onGround) {
            ++this.groundTicks;
            this.offGroundTicks = 0;
        } else {
            ++this.offGroundTicks;
            this.groundTicks = 0;
        }
        if (this.pendingSlotSwitch && AcrimonyClientUtil.mc.thePlayer.ticksExisted > this.ticksExisted) {
            AcrimonyClientUtil.mc.thePlayer.inventory.currentItem = this.pendingSlot;
            AcrimonyClientUtil.mc.playerController.syncCurrentPlayItem();
            this.pendingSlotSwitch = false;
        }
        if ((invOpened = AcrimonyClientUtil.mc.currentScreen instanceof GuiInventory) != this.invOpened) {
            this.invOpened = invOpened;
            this.ticksSinceInvOpened = 0;
        } else {
            ++this.ticksSinceInvOpened;
        }
        boolean chestOpened = AcrimonyClientUtil.mc.currentScreen instanceof GuiChest;
        if (chestOpened != this.chestOpened) {
            this.chestOpened = chestOpened;
            this.ticksSinceChestOpened = 0;
        } else {
            ++this.ticksSinceChestOpened;
        }
    }

    @Listener(value=-128)
    public void onVelocity(VelocityEvent event) {
        this.velocityTicks = 0;
        this.velocityX = event.getX();
        this.velocityY = event.getY();
        this.velocityZ = event.getZ();
        this.velocityDist = Math.hypot(this.velocityX, this.velocityZ);
    }

    @Listener(value=127)
    public void onEntityAction(EntityActionEvent event) {
        this.lastServerSprinting = event.isSprinting();
        this.lastServerSneaking = event.isSneaking();
    }

    @Listener
    public void onPacketSend(PacketSendEvent event) {
        if (AcrimonyClientUtil.mc.thePlayer == null || AcrimonyClientUtil.mc.thePlayer.ticksExisted < 10) {
            if (!this.resetted) {
                this.reset();
                this.resetted = true;
            }
        } else {
            this.resetted = false;
        }
    }

    public void switchSlotNextTick(int slot) {
        this.pendingSlotSwitch = true;
        this.ticksExisted = AcrimonyClientUtil.mc.thePlayer.ticksExisted;
        this.pendingSlot = slot;
    }

    public int getVelocityTicks() {
        return this.velocityTicks;
    }

    public boolean isResetted() {
        return this.resetted;
    }

    public double getVelocityX() {
        return this.velocityX;
    }

    public double getVelocityY() {
        return this.velocityY;
    }

    public double getVelocityZ() {
        return this.velocityZ;
    }

    public double getVelocityDist() {
        return this.velocityDist;
    }

    public int getGroundTicks() {
        return this.groundTicks;
    }

    public int getOffGroundTicks() {
        return this.offGroundTicks;
    }

    public boolean isPendingSlotSwitch() {
        return this.pendingSlotSwitch;
    }

    public int getTicksExisted() {
        return this.ticksExisted;
    }

    public int getPendingSlot() {
        return this.pendingSlot;
    }

    public boolean isLastServerSprinting() {
        return this.lastServerSprinting;
    }

    public boolean isLastServerSneaking() {
        return this.lastServerSneaking;
    }

    public boolean isInvOpened() {
        return this.invOpened;
    }

    public int getTicksSinceInvOpened() {
        return this.ticksSinceInvOpened;
    }

    public boolean isChestOpened() {
        return this.chestOpened;
    }

    public int getTicksSinceChestOpened() {
        return this.ticksSinceChestOpened;
    }
}

