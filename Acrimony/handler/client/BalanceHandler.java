/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.client;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.FinalPacketSendEvent;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.util.IMinecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class BalanceHandler
implements IMinecraft {
    private long balance;
    private long lastNanoTime;

    public BalanceHandler() {
        Acrimony.instance.getEventManager().register(this);
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.balance -= 50000000L;
        }
    }

    @Listener
    public void onFinalSend(FinalPacketSendEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer && !event.isCancelled()) {
            long nanoTime = System.nanoTime();
            if (!BalanceHandler.mc.getNetHandler().doneLoadingTerrain || BalanceHandler.mc.thePlayer.ticksExisted < 30) {
                this.balance = 0L;
                this.lastNanoTime = nanoTime - 50000000L;
            }
            this.balance += 50000000L;
            this.balance -= nanoTime - this.lastNanoTime;
            this.lastNanoTime = nanoTime;
        }
    }

    public long getBalanceInMS() {
        return (long)((double)this.balance / 1000000.0);
    }

    public long getBalance() {
        return this.balance;
    }

    public long getLastNanoTime() {
        return this.lastNanoTime;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}

