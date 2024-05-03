/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;

public class MapGenStronghold
extends MapGenStructure {
    private List<BiomeGenBase> field_151546_e;
    private boolean ranBiomeCheck;
    private ChunkCoordIntPair[] structureCoords = new ChunkCoordIntPair[3];
    private double field_82671_h = 32.0;
    private int field_82672_i = 3;

    public MapGenStronghold() {
        this.field_151546_e = Lists.newArrayList();
        for (BiomeGenBase biomegenbase : BiomeGenBase.getBiomeGenArray()) {
            if (biomegenbase == null || !(biomegenbase.minHeight > 0.0f)) continue;
            this.field_151546_e.add(biomegenbase);
        }
    }

    public MapGenStronghold(Map<String, String> p_i2068_1_) {
        this();
        for (Map.Entry<String, String> entry : p_i2068_1_.entrySet()) {
            if (entry.getKey().equals("distance")) {
                this.field_82671_h = MathHelper.parseDoubleWithDefaultAndMax(entry.getValue(), this.field_82671_h, 1.0);
                continue;
            }
            if (entry.getKey().equals("count")) {
                this.structureCoords = new ChunkCoordIntPair[MathHelper.parseIntWithDefaultAndMax(entry.getValue(), this.structureCoords.length, 1)];
                continue;
            }
            if (!entry.getKey().equals("spread")) continue;
            this.field_82672_i = MathHelper.parseIntWithDefaultAndMax(entry.getValue(), this.field_82672_i, 1);
        }
    }

    @Override
    public String getStructureName() {
        return "Stronghold";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (!this.ranBiomeCheck) {
            Random random = new Random();
            random.setSeed(this.worldObj.getSeed());
            double d0 = random.nextDouble() * Math.PI * 2.0;
            int i = 1;
            for (int j = 0; j < this.structureCoords.length; ++j) {
                double d1 = (1.25 * (double)i + random.nextDouble()) * this.field_82671_h * (double)i;
                int k = (int)Math.round(Math.cos(d0) * d1);
                int l = (int)Math.round(Math.sin(d0) * d1);
                BlockPos blockpos = this.worldObj.getWorldChunkManager().findBiomePosition((k << 4) + 8, (l << 4) + 8, 112, this.field_151546_e, random);
                if (blockpos != null) {
                    k = blockpos.getX() >> 4;
                    l = blockpos.getZ() >> 4;
                }
                this.structureCoords[j] = new ChunkCoordIntPair(k, l);
                d0 += Math.PI * 2 * (double)i / (double)this.field_82672_i;
                if (j != this.field_82672_i) continue;
                i += 2 + random.nextInt(5);
                this.field_82672_i += 1 + random.nextInt(2);
            }
            this.ranBiomeCheck = true;
        }
        for (ChunkCoordIntPair chunkcoordintpair : this.structureCoords) {
            if (chunkX != chunkcoordintpair.chunkXPos || chunkZ != chunkcoordintpair.chunkZPos) continue;
            return true;
        }
        return false;
    }

    @Override
    protected List<BlockPos> getCoordList() {
        ArrayList<BlockPos> list = Lists.newArrayList();
        for (ChunkCoordIntPair chunkcoordintpair : this.structureCoords) {
            if (chunkcoordintpair == null) continue;
            list.add(chunkcoordintpair.getCenterBlock(64));
        }
        return list;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Start mapgenstronghold$start = new Start(this.worldObj, this.rand, chunkX, chunkZ);
        while (mapgenstronghold$start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2)mapgenstronghold$start.getComponents().get((int)0)).strongholdPortalRoom == null) {
            mapgenstronghold$start = new Start(this.worldObj, this.rand, chunkX, chunkZ);
        }
        return mapgenstronghold$start;
    }

    public static class Start
    extends StructureStart {
        public Start() {
        }

        public Start(World worldIn, Random p_i2067_2_, int p_i2067_3_, int p_i2067_4_) {
            super(p_i2067_3_, p_i2067_4_);
            StructureStrongholdPieces.prepareStructurePieces();
            StructureStrongholdPieces.Stairs2 structurestrongholdpieces$stairs2 = new StructureStrongholdPieces.Stairs2(0, p_i2067_2_, (p_i2067_3_ << 4) + 2, (p_i2067_4_ << 4) + 2);
            this.components.add(structurestrongholdpieces$stairs2);
            structurestrongholdpieces$stairs2.buildComponent(structurestrongholdpieces$stairs2, this.components, p_i2067_2_);
            List<StructureComponent> list = structurestrongholdpieces$stairs2.field_75026_c;
            while (!list.isEmpty()) {
                int i = p_i2067_2_.nextInt(list.size());
                StructureComponent structurecomponent = list.remove(i);
                structurecomponent.buildComponent(structurestrongholdpieces$stairs2, this.components, p_i2067_2_);
            }
            this.updateBoundingBox();
            this.markAvailableHeight(worldIn, p_i2067_2_, 10);
        }
    }
}

