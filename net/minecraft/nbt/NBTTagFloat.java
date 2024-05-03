/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.MathHelper;

public class NBTTagFloat
extends NBTBase.NBTPrimitive {
    private float data;

    NBTTagFloat() {
    }

    public NBTTagFloat(float data) {
        this.data = data;
    }

    @Override
    void write(DataOutput output) throws IOException {
        output.writeFloat(this.data);
    }

    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(96L);
        this.data = input.readFloat();
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public String toString() {
        return "" + this.data + "f";
    }

    @Override
    public NBTBase copy() {
        return new NBTTagFloat(this.data);
    }

    @Override
    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagFloat nbttagfloat = (NBTTagFloat)p_equals_1_;
            return this.data == nbttagfloat.data;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Float.floatToIntBits(this.data);
    }

    @Override
    public long getLong() {
        return (long)this.data;
    }

    @Override
    public int getInt() {
        return MathHelper.floor_float(this.data);
    }

    @Override
    public short getShort() {
        return (short)(MathHelper.floor_float(this.data) & 0xFFFF);
    }

    @Override
    public byte getByte() {
        return (byte)(MathHelper.floor_float(this.data) & 0xFF);
    }

    @Override
    public double getDouble() {
        return this.data;
    }

    @Override
    public float getFloat() {
        return this.data;
    }
}

