/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.ghost;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.handler.packet.DelayedPacket;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.util.player.PendingVelocity;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.MovingObjectPosition;

public class Backtrack
extends Module {
    private final IntegerSetting delay = new IntegerSetting("Delay", 500, 100, 2000, 50);
    private final DoubleSetting minRange = new DoubleSetting("Min range", 2.8, 1.0, 6.0, 0.1);
    private final BooleanSetting delayPing = new BooleanSetting("Delay ping", true);
    private final BooleanSetting delayVelocity = new BooleanSetting("Delay velocity", () -> this.delayPing.isEnabled(), true);
    private final CopyOnWriteArrayList<DelayedPacket> delayedPackets = new CopyOnWriteArrayList();
    private Killaura killauraModule;
    private EntityLivingBase lastTarget;
    private EntityLivingBase lastCursorTarget;
    private int cursorTargetTicks;
    private PendingVelocity lastVelocity;

    public Backtrack() {
        super("Backtrack", Category.GHOST);
        this.addSettings(this.delay, this.minRange, this.delayPing, this.delayVelocity);
    }

    @Override
    public void onClientStarted() {
        this.killauraModule = Acrimony.instance.getModuleManager().getModule(Killaura.class);
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        S12PacketEntityVelocity packet;
        EntityLivingBase currentTarget;
        if (!(Backtrack.mc.thePlayer != null && Backtrack.mc.thePlayer.ticksExisted >= 5 || this.delayedPackets.isEmpty())) {
            this.delayedPackets.clear();
        }
        if ((currentTarget = this.getCurrentTarget()) != this.lastTarget) {
            this.clearPackets();
        }
        if (currentTarget == null) {
            this.clearPackets();
        } else if (event.getPacket() instanceof S14PacketEntity) {
            int z;
            double posZ;
            int y;
            double posY;
            int x;
            double posX;
            S14PacketEntity packet2 = (S14PacketEntity)event.getPacket();
            if (packet2.getEntity(Backtrack.mc.getNetHandler().clientWorldController) == currentTarget && this.killauraModule.getDistanceCustomPosition(posX = (double)(x = currentTarget.serverPosX + packet2.getX()) / 32.0, posY = (double)(y = currentTarget.serverPosY + packet2.getY()) / 32.0, posZ = (double)(z = currentTarget.serverPosZ + packet2.getZ()) / 32.0, currentTarget.getEyeHeight()) >= this.minRange.getValue()) {
                event.setCancelled(true);
                this.delayedPackets.add(new DelayedPacket(packet2));
            }
        } else if (event.getPacket() instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport packet3 = (S18PacketEntityTeleport)event.getPacket();
            if (packet3.getEntityId() == currentTarget.getEntityId()) {
                double z;
                double y;
                double x;
                double serverX = packet3.getX();
                double serverY = packet3.getY();
                double serverZ = packet3.getZ();
                double d0 = serverX / 32.0;
                double d1 = serverY / 32.0;
                double d2 = serverZ / 32.0;
                if (Math.abs(serverX - d0) < 0.03125 && Math.abs(serverY - d1) < 0.015625 && Math.abs(serverZ - d2) < 0.03125) {
                    x = currentTarget.posX;
                    y = currentTarget.posY;
                    z = currentTarget.posZ;
                } else {
                    x = d0;
                    y = d1;
                    z = d2;
                }
                if (this.killauraModule.getDistanceCustomPosition(x, y, z, currentTarget.getEyeHeight()) >= this.minRange.getValue()) {
                    event.setCancelled(true);
                    this.delayedPackets.add(new DelayedPacket(packet3));
                }
            }
        } else if (event.getPacket() instanceof S32PacketConfirmTransaction || event.getPacket() instanceof S00PacketKeepAlive) {
            if (!this.delayedPackets.isEmpty() && this.delayPing.isEnabled()) {
                event.setCancelled(true);
                this.delayedPackets.add(new DelayedPacket((Packet)event.getPacket()));
            }
        } else if (event.getPacket() instanceof S12PacketEntityVelocity && (packet = (S12PacketEntityVelocity)event.getPacket()).getEntityID() == Backtrack.mc.thePlayer.getEntityId() && !this.delayedPackets.isEmpty() && this.delayPing.isEnabled() && this.delayVelocity.isEnabled()) {
            event.setCancelled(true);
            this.lastVelocity = new PendingVelocity((double)packet.getMotionX() / 8000.0, (double)packet.getMotionY() / 8000.0, (double)packet.getMotionZ() / 8000.0);
        }
        this.lastTarget = currentTarget;
    }

    @Listener
    public void onPostMotion(PostMotionEvent event) {
        this.updatePackets();
    }

    public EntityLivingBase getCurrentTarget() {
        if (this.killauraModule == null) {
            this.killauraModule = Acrimony.instance.getModuleManager().getModule(Killaura.class);
        }
        if (this.killauraModule.isEnabled() && this.killauraModule.getTarget() != null) {
            return this.killauraModule.getTarget();
        }
        if (Backtrack.mc.objectMouseOver != null && Backtrack.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && Backtrack.mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            this.lastCursorTarget = (EntityLivingBase)Backtrack.mc.objectMouseOver.entityHit;
            return (EntityLivingBase)Backtrack.mc.objectMouseOver.entityHit;
        }
        if (this.lastCursorTarget != null) {
            if (++this.cursorTargetTicks > 10) {
                this.lastCursorTarget = null;
            } else {
                return this.lastCursorTarget;
            }
        }
        return null;
    }

    public void updatePackets() {
        if (!this.delayedPackets.isEmpty()) {
            for (DelayedPacket p : this.delayedPackets) {
                if (p.getTimer().getTimeElapsed() < (long)this.delay.getValue()) continue;
                this.clearPackets();
                if (this.lastVelocity != null) {
                    Backtrack.mc.thePlayer.motionX = this.lastVelocity.getX();
                    Backtrack.mc.thePlayer.motionY = this.lastVelocity.getY();
                    Backtrack.mc.thePlayer.motionZ = this.lastVelocity.getZ();
                    this.lastVelocity = null;
                }
                return;
            }
        }
    }

    public void clearPackets() {
        if (this.lastVelocity != null) {
            Backtrack.mc.thePlayer.motionX = this.lastVelocity.getX();
            Backtrack.mc.thePlayer.motionY = this.lastVelocity.getY();
            Backtrack.mc.thePlayer.motionZ = this.lastVelocity.getZ();
            this.lastVelocity = null;
        }
        if (!this.delayedPackets.isEmpty()) {
            for (DelayedPacket p : this.delayedPackets) {
                this.handlePacket((Packet)p.getPacket());
            }
            this.delayedPackets.clear();
        }
    }

    public void handlePacket(Packet packet) {
        if (packet instanceof S14PacketEntity) {
            this.handleEntityMovement((S14PacketEntity)packet);
        } else if (packet instanceof S18PacketEntityTeleport) {
            this.handleEntityTeleport((S18PacketEntityTeleport)packet);
        } else if (packet instanceof S32PacketConfirmTransaction) {
            this.handleConfirmTransaction((S32PacketConfirmTransaction)packet);
        } else if (packet instanceof S00PacketKeepAlive) {
            mc.getNetHandler().handleKeepAlive((S00PacketKeepAlive)packet);
        }
    }

    public void handleEntityMovement(S14PacketEntity packetIn) {
        Entity entity = packetIn.getEntity(Backtrack.mc.getNetHandler().clientWorldController);
        if (entity != null) {
            entity.serverPosX += packetIn.getX();
            entity.serverPosY += packetIn.getY();
            entity.serverPosZ += packetIn.getZ();
            double d0 = (double)entity.serverPosX / 32.0;
            double d1 = (double)entity.serverPosY / 32.0;
            double d2 = (double)entity.serverPosZ / 32.0;
            float f = packetIn.func_149060_h() ? (float)(packetIn.getYaw() * 360) / 256.0f : entity.rotationYaw;
            float f1 = packetIn.func_149060_h() ? (float)(packetIn.getPitch() * 360) / 256.0f : entity.rotationPitch;
            entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, false);
            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleEntityTeleport(S18PacketEntityTeleport packetIn) {
        Entity entity = Backtrack.mc.getNetHandler().clientWorldController.getEntityByID(packetIn.getEntityId());
        if (entity != null) {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            double d0 = (double)entity.serverPosX / 32.0;
            double d1 = (double)entity.serverPosY / 32.0;
            double d2 = (double)entity.serverPosZ / 32.0;
            float f = (float)(packetIn.getYaw() * 360) / 256.0f;
            float f1 = (float)(packetIn.getPitch() * 360) / 256.0f;
            if (Math.abs(entity.posX - d0) < 0.03125 && Math.abs(entity.posY - d1) < 0.015625 && Math.abs(entity.posZ - d2) < 0.03125) {
                entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, f1, 3, true);
            } else {
                entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true);
            }
            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn) {
        Container container = null;
        EntityPlayerSP entityplayer = Backtrack.mc.thePlayer;
        if (packetIn.getWindowId() == 0) {
            container = entityplayer.inventoryContainer;
        } else if (packetIn.getWindowId() == entityplayer.openContainer.windowId) {
            container = entityplayer.openContainer;
        }
        if (container != null && !packetIn.func_148888_e()) {
            mc.getNetHandler().addToSendQueue(new C0FPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
        }
    }

    public boolean isDelaying() {
        return this.isEnabled() && !this.delayedPackets.isEmpty();
    }
}

