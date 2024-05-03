/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.village;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

public class Village {
    private World worldObj;
    private final List<VillageDoorInfo> villageDoorInfoList = Lists.newArrayList();
    private BlockPos centerHelper = BlockPos.ORIGIN;
    private BlockPos center = BlockPos.ORIGIN;
    private int villageRadius;
    private int lastAddDoorTimestamp;
    private int tickCounter;
    private int numVillagers;
    private int noBreedTicks;
    private TreeMap<String, Integer> playerReputation = new TreeMap();
    private List<VillageAggressor> villageAgressors = Lists.newArrayList();
    private int numIronGolems;

    public Village() {
    }

    public Village(World worldIn) {
        this.worldObj = worldIn;
    }

    public void setWorld(World worldIn) {
        this.worldObj = worldIn;
    }

    public void tick(int p_75560_1_) {
        Vec3 vec3;
        int i;
        this.tickCounter = p_75560_1_;
        this.removeDeadAndOutOfRangeDoors();
        this.removeDeadAndOldAgressors();
        if (p_75560_1_ % 20 == 0) {
            this.updateNumVillagers();
        }
        if (p_75560_1_ % 30 == 0) {
            this.updateNumIronGolems();
        }
        if (this.numIronGolems < (i = this.numVillagers / 10) && this.villageDoorInfoList.size() > 20 && this.worldObj.rand.nextInt(7000) == 0 && (vec3 = this.func_179862_a(this.center, 2, 4, 2)) != null) {
            EntityIronGolem entityirongolem = new EntityIronGolem(this.worldObj);
            entityirongolem.setPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
            this.worldObj.spawnEntityInWorld(entityirongolem);
            ++this.numIronGolems;
        }
    }

    private Vec3 func_179862_a(BlockPos p_179862_1_, int p_179862_2_, int p_179862_3_, int p_179862_4_) {
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos = p_179862_1_.add(this.worldObj.rand.nextInt(16) - 8, this.worldObj.rand.nextInt(6) - 3, this.worldObj.rand.nextInt(16) - 8);
            if (!this.func_179866_a(blockpos) || !this.func_179861_a(new BlockPos(p_179862_2_, p_179862_3_, p_179862_4_), blockpos)) continue;
            return new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        }
        return null;
    }

    private boolean func_179861_a(BlockPos p_179861_1_, BlockPos p_179861_2_) {
        if (!World.doesBlockHaveSolidTopSurface(this.worldObj, p_179861_2_.down())) {
            return false;
        }
        int i = p_179861_2_.getX() - p_179861_1_.getX() / 2;
        int j = p_179861_2_.getZ() - p_179861_1_.getZ() / 2;
        for (int k = i; k < i + p_179861_1_.getX(); ++k) {
            for (int l = p_179861_2_.getY(); l < p_179861_2_.getY() + p_179861_1_.getY(); ++l) {
                for (int i1 = j; i1 < j + p_179861_1_.getZ(); ++i1) {
                    if (!this.worldObj.getBlockState(new BlockPos(k, l, i1)).getBlock().isNormalCube()) continue;
                    return false;
                }
            }
        }
        return true;
    }

    private void updateNumIronGolems() {
        List<EntityIronGolem> list = this.worldObj.getEntitiesWithinAABB(EntityIronGolem.class, new AxisAlignedBB(this.center.getX() - this.villageRadius, this.center.getY() - 4, this.center.getZ() - this.villageRadius, this.center.getX() + this.villageRadius, this.center.getY() + 4, this.center.getZ() + this.villageRadius));
        this.numIronGolems = list.size();
    }

    private void updateNumVillagers() {
        List<EntityVillager> list = this.worldObj.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(this.center.getX() - this.villageRadius, this.center.getY() - 4, this.center.getZ() - this.villageRadius, this.center.getX() + this.villageRadius, this.center.getY() + 4, this.center.getZ() + this.villageRadius));
        this.numVillagers = list.size();
        if (this.numVillagers == 0) {
            this.playerReputation.clear();
        }
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public int getVillageRadius() {
        return this.villageRadius;
    }

    public int getNumVillageDoors() {
        return this.villageDoorInfoList.size();
    }

    public int getTicksSinceLastDoorAdding() {
        return this.tickCounter - this.lastAddDoorTimestamp;
    }

    public int getNumVillagers() {
        return this.numVillagers;
    }

    public boolean func_179866_a(BlockPos pos) {
        return this.center.distanceSq(pos) < (double)(this.villageRadius * this.villageRadius);
    }

    public List<VillageDoorInfo> getVillageDoorInfoList() {
        return this.villageDoorInfoList;
    }

    public VillageDoorInfo getNearestDoor(BlockPos pos) {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;
        for (VillageDoorInfo villagedoorinfo1 : this.villageDoorInfoList) {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);
            if (j >= i) continue;
            villagedoorinfo = villagedoorinfo1;
            i = j;
        }
        return villagedoorinfo;
    }

    public VillageDoorInfo getDoorInfo(BlockPos pos) {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;
        for (VillageDoorInfo villagedoorinfo1 : this.villageDoorInfoList) {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);
            j = j > 256 ? (j *= 1000) : villagedoorinfo1.getDoorOpeningRestrictionCounter();
            if (j >= i) continue;
            villagedoorinfo = villagedoorinfo1;
            i = j;
        }
        return villagedoorinfo;
    }

    public VillageDoorInfo getExistedDoor(BlockPos doorBlock) {
        if (this.center.distanceSq(doorBlock) > (double)(this.villageRadius * this.villageRadius)) {
            return null;
        }
        for (VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
            if (villagedoorinfo.getDoorBlockPos().getX() != doorBlock.getX() || villagedoorinfo.getDoorBlockPos().getZ() != doorBlock.getZ() || Math.abs(villagedoorinfo.getDoorBlockPos().getY() - doorBlock.getY()) > 1) continue;
            return villagedoorinfo;
        }
        return null;
    }

    public void addVillageDoorInfo(VillageDoorInfo doorInfo) {
        this.villageDoorInfoList.add(doorInfo);
        this.centerHelper = this.centerHelper.add(doorInfo.getDoorBlockPos());
        this.updateVillageRadiusAndCenter();
        this.lastAddDoorTimestamp = doorInfo.getInsidePosY();
    }

    public boolean isAnnihilated() {
        return this.villageDoorInfoList.isEmpty();
    }

    public void addOrRenewAgressor(EntityLivingBase entitylivingbaseIn) {
        for (VillageAggressor village$villageaggressor : this.villageAgressors) {
            if (village$villageaggressor.agressor != entitylivingbaseIn) continue;
            village$villageaggressor.agressionTime = this.tickCounter;
            return;
        }
        this.villageAgressors.add(new VillageAggressor(entitylivingbaseIn, this.tickCounter));
    }

    public EntityLivingBase findNearestVillageAggressor(EntityLivingBase entitylivingbaseIn) {
        double d0 = Double.MAX_VALUE;
        VillageAggressor village$villageaggressor = null;
        for (int i = 0; i < this.villageAgressors.size(); ++i) {
            VillageAggressor village$villageaggressor1 = this.villageAgressors.get(i);
            double d1 = village$villageaggressor1.agressor.getDistanceSqToEntity(entitylivingbaseIn);
            if (!(d1 <= d0)) continue;
            village$villageaggressor = village$villageaggressor1;
            d0 = d1;
        }
        return village$villageaggressor != null ? village$villageaggressor.agressor : null;
    }

    public EntityPlayer getNearestTargetPlayer(EntityLivingBase villageDefender) {
        double d0 = Double.MAX_VALUE;
        EntityPlayer entityplayer = null;
        for (String s : this.playerReputation.keySet()) {
            double d1;
            EntityPlayer entityplayer1;
            if (!this.isPlayerReputationTooLow(s) || (entityplayer1 = this.worldObj.getPlayerEntityByName(s)) == null || !((d1 = entityplayer1.getDistanceSqToEntity(villageDefender)) <= d0)) continue;
            entityplayer = entityplayer1;
            d0 = d1;
        }
        return entityplayer;
    }

    private void removeDeadAndOldAgressors() {
        Iterator<VillageAggressor> iterator = this.villageAgressors.iterator();
        while (iterator.hasNext()) {
            VillageAggressor village$villageaggressor = iterator.next();
            if (village$villageaggressor.agressor.isEntityAlive() && Math.abs(this.tickCounter - village$villageaggressor.agressionTime) <= 300) continue;
            iterator.remove();
        }
    }

    private void removeDeadAndOutOfRangeDoors() {
        boolean flag = false;
        boolean flag1 = this.worldObj.rand.nextInt(50) == 0;
        Iterator<VillageDoorInfo> iterator = this.villageDoorInfoList.iterator();
        while (iterator.hasNext()) {
            VillageDoorInfo villagedoorinfo = iterator.next();
            if (flag1) {
                villagedoorinfo.resetDoorOpeningRestrictionCounter();
            }
            if (this.isWoodDoor(villagedoorinfo.getDoorBlockPos()) && Math.abs(this.tickCounter - villagedoorinfo.getInsidePosY()) <= 1200) continue;
            this.centerHelper = this.centerHelper.subtract(villagedoorinfo.getDoorBlockPos());
            flag = true;
            villagedoorinfo.setIsDetachedFromVillageFlag(true);
            iterator.remove();
        }
        if (flag) {
            this.updateVillageRadiusAndCenter();
        }
    }

    private boolean isWoodDoor(BlockPos pos) {
        Block block = this.worldObj.getBlockState(pos).getBlock();
        return block instanceof BlockDoor ? block.getMaterial() == Material.wood : false;
    }

    private void updateVillageRadiusAndCenter() {
        int i = this.villageDoorInfoList.size();
        if (i == 0) {
            this.center = new BlockPos(0, 0, 0);
            this.villageRadius = 0;
        } else {
            this.center = new BlockPos(this.centerHelper.getX() / i, this.centerHelper.getY() / i, this.centerHelper.getZ() / i);
            int j = 0;
            for (VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
                j = Math.max(villagedoorinfo.getDistanceToDoorBlockSq(this.center), j);
            }
            this.villageRadius = Math.max(32, (int)Math.sqrt(j) + 1);
        }
    }

    public int getReputationForPlayer(String p_82684_1_) {
        Integer integer = this.playerReputation.get(p_82684_1_);
        return integer != null ? integer : 0;
    }

    public int setReputationForPlayer(String p_82688_1_, int p_82688_2_) {
        int i = this.getReputationForPlayer(p_82688_1_);
        int j = MathHelper.clamp_int(i + p_82688_2_, -30, 10);
        this.playerReputation.put(p_82688_1_, j);
        return j;
    }

    public boolean isPlayerReputationTooLow(String p_82687_1_) {
        return this.getReputationForPlayer(p_82687_1_) <= -15;
    }

    public void readVillageDataFromNBT(NBTTagCompound p_82690_1_) {
        this.numVillagers = p_82690_1_.getInteger("PopSize");
        this.villageRadius = p_82690_1_.getInteger("Radius");
        this.numIronGolems = p_82690_1_.getInteger("Golems");
        this.lastAddDoorTimestamp = p_82690_1_.getInteger("Stable");
        this.tickCounter = p_82690_1_.getInteger("Tick");
        this.noBreedTicks = p_82690_1_.getInteger("MTick");
        this.center = new BlockPos(p_82690_1_.getInteger("CX"), p_82690_1_.getInteger("CY"), p_82690_1_.getInteger("CZ"));
        this.centerHelper = new BlockPos(p_82690_1_.getInteger("ACX"), p_82690_1_.getInteger("ACY"), p_82690_1_.getInteger("ACZ"));
        NBTTagList nbttaglist = p_82690_1_.getTagList("Doors", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            VillageDoorInfo villagedoorinfo = new VillageDoorInfo(new BlockPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Y"), nbttagcompound.getInteger("Z")), nbttagcompound.getInteger("IDX"), nbttagcompound.getInteger("IDZ"), nbttagcompound.getInteger("TS"));
            this.villageDoorInfoList.add(villagedoorinfo);
        }
        NBTTagList nbttaglist1 = p_82690_1_.getTagList("Players", 10);
        for (int j = 0; j < nbttaglist1.tagCount(); ++j) {
            NBTTagCompound nbttagcompound1 = nbttaglist1.getCompoundTagAt(j);
            if (nbttagcompound1.hasKey("UUID")) {
                PlayerProfileCache playerprofilecache = MinecraftServer.getServer().getPlayerProfileCache();
                GameProfile gameprofile = playerprofilecache.getProfileByUUID(UUID.fromString(nbttagcompound1.getString("UUID")));
                if (gameprofile == null) continue;
                this.playerReputation.put(gameprofile.getName(), nbttagcompound1.getInteger("S"));
                continue;
            }
            this.playerReputation.put(nbttagcompound1.getString("Name"), nbttagcompound1.getInteger("S"));
        }
    }

    public void writeVillageDataToNBT(NBTTagCompound p_82689_1_) {
        p_82689_1_.setInteger("PopSize", this.numVillagers);
        p_82689_1_.setInteger("Radius", this.villageRadius);
        p_82689_1_.setInteger("Golems", this.numIronGolems);
        p_82689_1_.setInteger("Stable", this.lastAddDoorTimestamp);
        p_82689_1_.setInteger("Tick", this.tickCounter);
        p_82689_1_.setInteger("MTick", this.noBreedTicks);
        p_82689_1_.setInteger("CX", this.center.getX());
        p_82689_1_.setInteger("CY", this.center.getY());
        p_82689_1_.setInteger("CZ", this.center.getZ());
        p_82689_1_.setInteger("ACX", this.centerHelper.getX());
        p_82689_1_.setInteger("ACY", this.centerHelper.getY());
        p_82689_1_.setInteger("ACZ", this.centerHelper.getZ());
        NBTTagList nbttaglist = new NBTTagList();
        for (VillageDoorInfo villagedoorinfo : this.villageDoorInfoList) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("X", villagedoorinfo.getDoorBlockPos().getX());
            nbttagcompound.setInteger("Y", villagedoorinfo.getDoorBlockPos().getY());
            nbttagcompound.setInteger("Z", villagedoorinfo.getDoorBlockPos().getZ());
            nbttagcompound.setInteger("IDX", villagedoorinfo.getInsideOffsetX());
            nbttagcompound.setInteger("IDZ", villagedoorinfo.getInsideOffsetZ());
            nbttagcompound.setInteger("TS", villagedoorinfo.getInsidePosY());
            nbttaglist.appendTag(nbttagcompound);
        }
        p_82689_1_.setTag("Doors", nbttaglist);
        NBTTagList nbttaglist1 = new NBTTagList();
        for (String s : this.playerReputation.keySet()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            PlayerProfileCache playerprofilecache = MinecraftServer.getServer().getPlayerProfileCache();
            GameProfile gameprofile = playerprofilecache.getGameProfileForUsername(s);
            if (gameprofile == null) continue;
            nbttagcompound1.setString("UUID", gameprofile.getId().toString());
            nbttagcompound1.setInteger("S", this.playerReputation.get(s));
            nbttaglist1.appendTag(nbttagcompound1);
        }
        p_82689_1_.setTag("Players", nbttaglist1);
    }

    public void endMatingSeason() {
        this.noBreedTicks = this.tickCounter;
    }

    public boolean isMatingSeason() {
        return this.noBreedTicks == 0 || this.tickCounter - this.noBreedTicks >= 3600;
    }

    public void setDefaultPlayerReputation(int p_82683_1_) {
        for (String s : this.playerReputation.keySet()) {
            this.setReputationForPlayer(s, p_82683_1_);
        }
    }

    class VillageAggressor {
        public EntityLivingBase agressor;
        public int agressionTime;

        VillageAggressor(EntityLivingBase p_i1674_2_, int p_i1674_3_) {
            this.agressor = p_i1674_2_;
            this.agressionTime = p_i1674_3_;
        }
    }
}

