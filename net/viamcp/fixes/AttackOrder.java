/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.viamcp.fixes;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MovingObjectPosition;
import net.vialoadingbase.ViaLoadingBase;

public class AttackOrder {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void sendConditionalSwing(MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            AttackOrder.mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttackByPacket(EntityPlayer entityIn, Entity target) {
        if (ViaLoadingBase.getInstance().getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_8)) {
            AttackOrder.mc.thePlayer.swingItem();
            mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
        } else {
            mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
            AttackOrder.mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(EntityPlayer entityIn, Entity target) {
        if (ViaLoadingBase.getInstance().getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_8)) {
            AttackOrder.mc.thePlayer.swingItem();
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
        } else {
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
            AttackOrder.mc.thePlayer.swingItem();
        }
    }
}

