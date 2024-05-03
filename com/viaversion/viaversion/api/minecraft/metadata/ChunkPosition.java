/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.metadata;

import java.util.Objects;

public final class ChunkPosition {
    private final int chunkX;
    private final int chunkZ;

    public ChunkPosition(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public ChunkPosition(long chunkKey) {
        this.chunkX = (int)chunkKey;
        this.chunkZ = (int)(chunkKey >> 32);
    }

    public int chunkX() {
        return this.chunkX;
    }

    public int chunkZ() {
        return this.chunkZ;
    }

    public long chunkKey() {
        return ChunkPosition.chunkKey(this.chunkX, this.chunkZ);
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return (long)chunkX & 0xFFFFFFFFL | ((long)chunkZ & 0xFFFFFFFFL) << 32;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ChunkPosition that = (ChunkPosition)o;
        return this.chunkX == that.chunkX && this.chunkZ == that.chunkZ;
    }

    public int hashCode() {
        return Objects.hash(this.chunkX, this.chunkZ);
    }

    public String toString() {
        return "ChunkPosition{chunkX=" + this.chunkX + ", chunkZ=" + this.chunkZ + '}';
    }
}

