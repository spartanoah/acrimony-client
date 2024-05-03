/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.packet;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.handler.packet.DelayedPacket;
import Acrimony.util.IMinecraft;
import Acrimony.util.network.PacketUtil;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

public class PacketDelayHandler
implements IMinecraft {
    private long pingDelay;
    private long otherDelay;
    private boolean delayingPing;
    private boolean delayingOther;
    private boolean clearedPackets;
    private final CopyOnWriteArrayList<DelayedPacket> delayedPackets = new CopyOnWriteArrayList();

    public PacketDelayHandler() {
        Acrimony.instance.getEventManager().register(this);
    }

    @Listener(value=10)
    public void onSend(PacketSendEvent event) {
        if (PacketDelayHandler.mc.thePlayer == null || PacketDelayHandler.mc.thePlayer.ticksExisted < 5) {
            if (!this.clearedPackets) {
                this.delayedPackets.clear();
                this.stopAll();
                this.clearedPackets = true;
            }
        } else {
            this.clearedPackets = false;
        }
        if (!event.isCancelled()) {
            if (this.isPing((Packet)event.getPacket())) {
                if (this.delayingPing) {
                    event.setCancelled(true);
                    this.delayedPackets.add(new DelayedPacket((Packet)event.getPacket()));
                }
            } else if (this.delayingOther) {
                event.setCancelled(true);
                this.delayedPackets.add(new DelayedPacket((Packet)event.getPacket()));
            }
        }
    }

    @Listener(value=10)
    public void onMotion(MotionEvent event) {
        if (!this.delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<DelayedPacket>();
            for (DelayedPacket p : this.delayedPackets) {
                if (this.isPing((Packet)p.getPacket())) {
                    if (p.getTimer().getTimeElapsed() < this.pingDelay) continue;
                    toRemove.add(p);
                    PacketUtil.sendPacketFinal(p.getPacket());
                    continue;
                }
                if (p.getTimer().getTimeElapsed() < this.otherDelay) continue;
                toRemove.add(p);
                PacketUtil.sendPacketFinal(p.getPacket());
            }
            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    this.delayedPackets.remove(p);
                }
            }
            toRemove.clear();
        }
    }

    public void startDelayingPing(long delay) {
        this.delayingPing = true;
        this.pingDelay = delay;
    }

    public void stopDelayingPing() {
        if (!this.delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<DelayedPacket>();
            for (DelayedPacket p : this.delayedPackets) {
                if (!this.isPing((Packet)p.getPacket())) continue;
                PacketUtil.sendPacketFinal(p.getPacket());
                toRemove.add(p);
            }
            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    this.delayedPackets.remove(p);
                }
            }
            toRemove.clear();
        }
        this.delayingPing = false;
        this.pingDelay = 0L;
    }

    public void clearPing() {
        if (!this.delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<DelayedPacket>();
            for (DelayedPacket p : this.delayedPackets) {
                if (!this.isPing((Packet)p.getPacket())) continue;
                toRemove.add(p);
            }
            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    this.delayedPackets.remove(p);
                }
            }
            toRemove.clear();
        }
    }

    public void startDelayingOther(long delay) {
        this.delayingOther = true;
        this.otherDelay = delay;
    }

    public void stopDelayingOther() {
        if (!this.delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<DelayedPacket>();
            for (DelayedPacket p : this.delayedPackets) {
                if (this.isPing((Packet)p.getPacket())) continue;
                PacketUtil.sendPacketFinal(p.getPacket());
                toRemove.add(p);
            }
            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    this.delayedPackets.remove(p);
                }
            }
            toRemove.clear();
        }
        this.delayingOther = false;
        this.otherDelay = 0L;
    }

    public void clearOther() {
        if (!this.delayedPackets.isEmpty()) {
            ArrayList<DelayedPacket> toRemove = new ArrayList<DelayedPacket>();
            for (DelayedPacket p : this.delayedPackets) {
                if (this.isPing((Packet)p.getPacket())) continue;
                toRemove.add(p);
            }
            if (!toRemove.isEmpty()) {
                for (DelayedPacket p : toRemove) {
                    this.delayedPackets.remove(p);
                }
            }
            toRemove.clear();
        }
    }

    public void releaseAll() {
        if (!this.delayedPackets.isEmpty()) {
            for (DelayedPacket p : this.delayedPackets) {
                PacketUtil.sendPacketFinal(p.getPacket());
            }
            this.delayedPackets.clear();
        }
    }

    public void stopAll() {
        this.releaseAll();
        this.delayingPing = false;
        this.delayingOther = false;
    }

    public boolean isPing(Packet p) {
        return p instanceof C0FPacketConfirmTransaction || p instanceof C00PacketKeepAlive;
    }

    public long getPingDelay() {
        return this.pingDelay;
    }

    public long getOtherDelay() {
        return this.otherDelay;
    }

    public boolean isDelayingPing() {
        return this.delayingPing;
    }

    public boolean isDelayingOther() {
        return this.delayingOther;
    }

    public boolean isClearedPackets() {
        return this.clearedPackets;
    }

    public CopyOnWriteArrayList<DelayedPacket> getDelayedPackets() {
        return this.delayedPackets;
    }
}

