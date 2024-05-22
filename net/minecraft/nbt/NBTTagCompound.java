/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ReportedException;

public class NBTTagCompound
extends NBTBase {
    private Map<String, NBTBase> tagMap = Maps.newHashMap();

    @Override
    void write(DataOutput output) throws IOException {
        for (String s : this.tagMap.keySet()) {
            NBTBase nbtbase = this.tagMap.get(s);
            NBTTagCompound.writeEntry(s, nbtbase, output);
        }
        output.writeByte(0);
    }

    @Override
    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        byte b0;
        sizeTracker.read(384L);
        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        this.tagMap.clear();
        while ((b0 = NBTTagCompound.readType(input, sizeTracker)) != 0) {
            String s = NBTTagCompound.readKey(input, sizeTracker);
            sizeTracker.read(224 + 16 * s.length());
            NBTBase nbtbase = NBTTagCompound.readNBT(b0, s, input, depth + 1, sizeTracker);
            if (this.tagMap.put(s, nbtbase) == null) continue;
            sizeTracker.read(288L);
        }
    }

    public Set<String> getKeySet() {
        return this.tagMap.keySet();
    }

    @Override
    public byte getId() {
        return 10;
    }

    public void setTag(String key, NBTBase value) {
        this.tagMap.put(key, value);
    }

    public void setByte(String key, byte value) {
        this.tagMap.put(key, new NBTTagByte(value));
    }

    public void setShort(String key, short value) {
        this.tagMap.put(key, new NBTTagShort(value));
    }

    public void setInteger(String key, int value) {
        this.tagMap.put(key, new NBTTagInt(value));
    }

    public void setLong(String key, long value) {
        this.tagMap.put(key, new NBTTagLong(value));
    }

    public void setFloat(String key, float value) {
        this.tagMap.put(key, new NBTTagFloat(value));
    }

    public void setDouble(String key, double value) {
        this.tagMap.put(key, new NBTTagDouble(value));
    }

    public void setString(String key, String value) {
        this.tagMap.put(key, new NBTTagString(value));
    }

    public void setByteArray(String key, byte[] value) {
        this.tagMap.put(key, new NBTTagByteArray(value));
    }

    public void setIntArray(String key, int[] value) {
        this.tagMap.put(key, new NBTTagIntArray(value));
    }

    public void setBoolean(String key, boolean value) {
        this.setByte(key, (byte)(value ? (char)'\u0001' : '\u0000'));
    }

    public NBTBase getTag(String key) {
        return this.tagMap.get(key);
    }

    public byte getTagId(String key) {
        NBTBase nbtbase = this.tagMap.get(key);
        return nbtbase != null ? nbtbase.getId() : (byte)0;
    }

    public boolean hasKey(String key) {
        return this.tagMap.containsKey(key);
    }

    public boolean hasKey(String key, int type) {
        byte i = this.getTagId(key);
        if (i == type) {
            return true;
        }
        if (type != 99) {
            if (i > 0) {
                // empty if block
            }
            return false;
        }
        return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
    }

    public byte getByte(String key) {
        try {
            return !this.hasKey(key, 99) ? (byte)0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getByte();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public short getShort(String key) {
        try {
            return !this.hasKey(key, 99) ? (short)0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getShort();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public int getInteger(String key) {
        try {
            return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getInt();
        } catch (ClassCastException var3) {
            return 0;
        }
    }

    public long getLong(String key) {
        try {
            return !this.hasKey(key, 99) ? 0L : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getLong();
        } catch (ClassCastException var3) {
            return 0L;
        }
    }

    public float getFloat(String key) {
        try {
            return !this.hasKey(key, 99) ? 0.0f : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getFloat();
        } catch (ClassCastException var3) {
            return 0.0f;
        }
    }

    public double getDouble(String key) {
        try {
            return !this.hasKey(key, 99) ? 0.0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getDouble();
        } catch (ClassCastException var3) {
            return 0.0;
        }
    }

    public String getString(String key) {
        try {
            return !this.hasKey(key, 8) ? "" : this.tagMap.get(key).getString();
        } catch (ClassCastException var3) {
            return "";
        }
    }

    public byte[] getByteArray(String key) {
        try {
            return !this.hasKey(key, 7) ? new byte[]{} : ((NBTTagByteArray)this.tagMap.get(key)).getByteArray();
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 7, classcastexception));
        }
    }

    public int[] getIntArray(String key) {
        try {
            return !this.hasKey(key, 11) ? new int[]{} : ((NBTTagIntArray)this.tagMap.get(key)).getIntArray();
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 11, classcastexception));
        }
    }

    public NBTTagCompound getCompoundTag(String key) {
        try {
            return !this.hasKey(key, 10) ? new NBTTagCompound() : (NBTTagCompound)this.tagMap.get(key);
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 10, classcastexception));
        }
    }

    public NBTTagList getTagList(String key, int type) {
        try {
            if (this.getTagId(key) != 9) {
                return new NBTTagList();
            }
            NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);
            return nbttaglist.tagCount() > 0 && nbttaglist.getTagType() != type ? new NBTTagList() : nbttaglist;
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.createCrashReport(key, 9, classcastexception));
        }
    }

    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }

    public void removeTag(String key) {
        this.tagMap.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");
        for (Map.Entry<String, NBTBase> entry : this.tagMap.entrySet()) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }
            stringbuilder.append(entry.getKey()).append(':').append(entry.getValue());
        }
        return stringbuilder.append('}').toString();
    }

    @Override
    public boolean hasNoTags() {
        return this.tagMap.isEmpty();
    }

    private CrashReport createCrashReport(final String key, final int expectedType, ClassCastException ex) {
        CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addCrashSectionCallable("Tag type found", new Callable<String>(){

            @Override
            public String call() throws Exception {
                return NBTBase.NBT_TYPES[((NBTBase)NBTTagCompound.this.tagMap.get(key)).getId()];
            }
        });
        crashreportcategory.addCrashSectionCallable("Tag type expected", new Callable<String>(){

            @Override
            public String call() throws Exception {
                return NBTBase.NBT_TYPES[expectedType];
            }
        });
        crashreportcategory.addCrashSection("Tag name", key);
        return crashreport;
    }

    @Override
    public NBTBase copy() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        for (String s : this.tagMap.keySet()) {
            nbttagcompound.setTag(s, this.tagMap.get(s).copy());
        }
        return nbttagcompound;
    }

    @Override
    public boolean equals(Object p_equals_1_) {
        if (super.equals(p_equals_1_)) {
            NBTTagCompound nbttagcompound = (NBTTagCompound)p_equals_1_;
            return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.tagMap.hashCode();
    }

    private static void writeEntry(String name, NBTBase data, DataOutput output) throws IOException {
        output.writeByte(data.getId());
        if (data.getId() != 0) {
            output.writeUTF(name);
            data.write(output);
        }
    }

    private static byte readType(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        return input.readByte();
    }

    private static String readKey(DataInput input, NBTSizeTracker sizeTracker) throws IOException {
        return input.readUTF();
    }

    static NBTBase readNBT(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException {
        NBTBase nbtbase = NBTBase.createNewByType(id);
        try {
            nbtbase.read(input, depth, sizeTracker);
            return nbtbase;
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", id);
            throw new ReportedException(crashreport);
        }
    }

    public void merge(NBTTagCompound other) {
        for (String s : other.tagMap.keySet()) {
            NBTBase nbtbase = other.tagMap.get(s);
            if (nbtbase.getId() == 10) {
                if (this.hasKey(s, 10)) {
                    NBTTagCompound nbttagcompound = this.getCompoundTag(s);
                    nbttagcompound.merge((NBTTagCompound)nbtbase);
                    continue;
                }
                this.setTag(s, nbtbase.copy());
                continue;
            }
            this.setTag(s, nbtbase.copy());
        }
    }
}

