/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityMinecartCommandBlock
extends EntityMinecart {
    private final CommandBlockLogic commandBlockLogic = new CommandBlockLogic(){

        @Override
        public void updateCommand() {
            EntityMinecartCommandBlock.this.getDataWatcher().updateObject(23, this.getCommand());
            EntityMinecartCommandBlock.this.getDataWatcher().updateObject(24, IChatComponent.Serializer.componentToJson(this.getLastOutput()));
        }

        @Override
        public int func_145751_f() {
            return 1;
        }

        @Override
        public void func_145757_a(ByteBuf p_145757_1_) {
            p_145757_1_.writeInt(EntityMinecartCommandBlock.this.getEntityId());
        }

        @Override
        public BlockPos getPosition() {
            return new BlockPos(EntityMinecartCommandBlock.this.posX, EntityMinecartCommandBlock.this.posY + 0.5, EntityMinecartCommandBlock.this.posZ);
        }

        @Override
        public Vec3 getPositionVector() {
            return new Vec3(EntityMinecartCommandBlock.this.posX, EntityMinecartCommandBlock.this.posY, EntityMinecartCommandBlock.this.posZ);
        }

        @Override
        public World getEntityWorld() {
            return EntityMinecartCommandBlock.this.worldObj;
        }

        @Override
        public Entity getCommandSenderEntity() {
            return EntityMinecartCommandBlock.this;
        }
    };
    private int activatorRailCooldown = 0;

    public EntityMinecartCommandBlock(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartCommandBlock(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataWatcher().addObject(23, "");
        this.getDataWatcher().addObject(24, "");
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.commandBlockLogic.readDataFromNBT(tagCompund);
        this.getDataWatcher().updateObject(23, this.getCommandBlockLogic().getCommand());
        this.getDataWatcher().updateObject(24, IChatComponent.Serializer.componentToJson(this.getCommandBlockLogic().getLastOutput()));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        this.commandBlockLogic.writeDataToNBT(tagCompound);
    }

    @Override
    public EntityMinecart.EnumMinecartType getMinecartType() {
        return EntityMinecart.EnumMinecartType.COMMAND_BLOCK;
    }

    @Override
    public IBlockState getDefaultDisplayTile() {
        return Blocks.command_block.getDefaultState();
    }

    public CommandBlockLogic getCommandBlockLogic() {
        return this.commandBlockLogic;
    }

    @Override
    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        if (receivingPower && this.ticksExisted - this.activatorRailCooldown >= 4) {
            this.getCommandBlockLogic().trigger(this.worldObj);
            this.activatorRailCooldown = this.ticksExisted;
        }
    }

    @Override
    public boolean interactFirst(EntityPlayer playerIn) {
        this.commandBlockLogic.tryOpenEditCommandBlock(playerIn);
        return false;
    }

    @Override
    public void onDataWatcherUpdate(int dataID) {
        super.onDataWatcherUpdate(dataID);
        if (dataID == 24) {
            try {
                this.commandBlockLogic.setLastOutput(IChatComponent.Serializer.jsonToComponent(this.getDataWatcher().getWatchableObjectString(24)));
            } catch (Throwable throwable) {}
        } else if (dataID == 23) {
            this.commandBlockLogic.setCommand(this.getDataWatcher().getWatchableObjectString(23));
        }
    }
}

