/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.event.Listener;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.util.network.PacketUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class SpeedMine
extends Module {
    private EnumFacing facing;
    private BlockPos pos;
    private boolean boost;
    private float damage;
    private final DoubleSetting speed = new DoubleSetting("speed", 1.4, 0.0, 5.0, 0.1);

    public SpeedMine() {
        super("SpeedMine", Category.PLAYER);
        this.addSettings(this.speed);
    }

    @Override
    public void onDisable() {
    }

    @Listener
    public void onUpdate(UpdateEvent e) {
        SpeedMine.mc.playerController.blockHitDelay = 0;
        if (this.pos != null && this.boost) {
            IBlockState blockState = SpeedMine.mc.theWorld.getBlockState(this.pos);
            if (blockState == null) {
                return;
            }
            try {
                this.damage = (float)((double)this.damage + (double)blockState.getBlock().getPlayerRelativeBlockHardness(SpeedMine.mc.thePlayer) * this.speed.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            if (this.damage >= 1.0f) {
                try {
                    SpeedMine.mc.theWorld.setBlockState(this.pos, Blocks.air.getDefaultState(), 11);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.pos, this.facing));
                this.damage = 0.0f;
                this.boost = false;
            }
        }
    }

    @Listener
    public void onPacketSendEvent(PacketSendEvent e) {
        if (e.getPacket() instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging packet = (C07PacketPlayerDigging)e.getPacket();
            if (packet.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                this.boost = true;
                this.pos = packet.getPosition();
                this.facing = packet.getFacing();
                this.damage = 0.0f;
            } else if (packet.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK | packet.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                this.boost = false;
                this.pos = null;
                this.facing = null;
            }
        }
    }
}

