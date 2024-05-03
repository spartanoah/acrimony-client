/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.network;

import Acrimony.util.IMinecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PacketUtil
implements IMinecraft {
    public static void sendPacket(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendPacketFinal(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacketFinal(packet);
    }

    public static void sendPacketNoEvent(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
    }

    public static void sendBlocking(boolean callEvent, boolean place) {
        C08PacketPlayerBlockPlacement packet;
        C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement = packet = place ? new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, PacketUtil.mc.thePlayer.getHeldItem(), 0.0f, 0.0f, 0.0f) : new C08PacketPlayerBlockPlacement(PacketUtil.mc.thePlayer.getHeldItem());
        if (callEvent) {
            PacketUtil.sendPacket(packet);
        } else {
            PacketUtil.sendPacketNoEvent(packet);
        }
    }

    public static void releaseUseItem(boolean callEvent) {
        C07PacketPlayerDigging packet = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        if (callEvent) {
            PacketUtil.sendPacket(packet);
        } else {
            PacketUtil.sendPacketNoEvent(packet);
        }
    }

    public static boolean shouldIgnorePacket(Packet packet) {
        return packet instanceof C00PacketLoginStart || packet instanceof C01PacketEncryptionResponse || packet instanceof C00Handshake || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing;
    }
}

