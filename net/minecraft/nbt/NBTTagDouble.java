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

public class NBTTagDouble
extends NBTBase.NBTPrimitive {
    private double data;

    NBTTagDouble() {
    }

    public NBTTagDouble(double data) {
        this.data = data;
    }

    @Override
    void write(DataOutput output) throws IOException {
        output.writeDouble(this.data);
    }

    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        sizeTracker.read(128L);
        this.data = input.readDouble();
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public String toString() {
        return "" + this.data + "d";
    }

    @Override
    public NBTBase copy() {
        return new NBTTagDouble(this.data);
    }

    @Override
    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagDouble nbttagdouble = (NBTTagDouble)p_equals_1_;
            return this.data == nbttagdouble.data;
        }
        return false;
    }

    @Override
    public int hashCode() {
        long i = Double.doubleToLongBits(this.data);
        return super.hashCode() ^ (int)(i ^ i >>> 32);
    }

    @Override
    public long getLong() {
        return (long)Math.floor(this.data);
    }

    @Override
    public int getInt() {
        return MathHelper.floor_double(this.data);
    }

    @Override
    public short getShort() {
        return (short)(MathHelper.floor_double(this.data) & 0xFFFF);
    }

    @Override
    public byte getByte() {
        return (byte)(MathHelper.floor_double(this.data) & 0xFF);
    }

    @Override
    public double getDouble() {
        return this.data;
    }

    @Override
    public float getFloat() {
        return (float)this.data;
    }
}

