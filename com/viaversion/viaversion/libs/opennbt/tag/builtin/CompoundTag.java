/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.TagRegistry;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.DoubleTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.FloatTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ShortTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class CompoundTag
extends Tag
implements Iterable<Map.Entry<String, Tag>> {
    public static final int ID = 10;
    private Map<String, Tag> value;

    public CompoundTag() {
        this(new LinkedHashMap<String, Tag>());
    }

    public CompoundTag(Map<String, Tag> value) {
        this.value = new LinkedHashMap<String, Tag>(value);
    }

    public CompoundTag(LinkedHashMap<String, Tag> value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = value;
    }

    public static CompoundTag read(DataInput in, TagLimiter tagLimiter, int nestingLevel) throws IOException {
        tagLimiter.checkLevel(nestingLevel);
        int newNestingLevel = nestingLevel + 1;
        CompoundTag compoundTag = new CompoundTag();
        while (true) {
            Tag tag;
            tagLimiter.countByte();
            byte id = in.readByte();
            if (id == 0) break;
            String name = in.readUTF();
            tagLimiter.countBytes(2 * name.length());
            try {
                tag = TagRegistry.read(id, in, tagLimiter, newNestingLevel);
            } catch (IllegalArgumentException e) {
                throw new IOException("Failed to create tag.", e);
            }
            compoundTag.value.put(name, tag);
        }
        return compoundTag;
    }

    @Override
    public Map<String, Tag> getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return this.value.toString();
    }

    public void setValue(Map<String, Tag> value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = new LinkedHashMap<String, Tag>(value);
    }

    public void setValue(LinkedHashMap<String, Tag> value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = value;
    }

    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public boolean contains(String tagName) {
        return this.value.containsKey(tagName);
    }

    @Nullable
    public <T extends Tag> T get(String tagName) {
        return (T)this.value.get(tagName);
    }

    @Nullable
    public StringTag getStringTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof StringTag ? (StringTag)tag : null;
    }

    @Nullable
    public CompoundTag getCompoundTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof CompoundTag ? (CompoundTag)tag : null;
    }

    @Nullable
    public ListTag getListTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof ListTag ? (ListTag)tag : null;
    }

    @Nullable
    public NumberTag getNumberTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof NumberTag ? (NumberTag)tag : null;
    }

    @Nullable
    public ByteArrayTag getByteArrayTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof ByteArrayTag ? (ByteArrayTag)tag : null;
    }

    @Nullable
    public IntArrayTag getIntArrayTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof IntArrayTag ? (IntArrayTag)tag : null;
    }

    @Nullable
    public LongArrayTag getLongArrayTag(String tagName) {
        Tag tag = this.value.get(tagName);
        return tag instanceof LongArrayTag ? (LongArrayTag)tag : null;
    }

    @Nullable
    public <T extends Tag> T put(String tagName, T tag) {
        return (T)this.value.put(tagName, tag);
    }

    public void putString(String tagName, String value) {
        this.value.put(tagName, new StringTag(value));
    }

    public void putByte(String tagName, byte value) {
        this.value.put(tagName, new ByteTag(value));
    }

    public void putInt(String tagName, int value) {
        this.value.put(tagName, new IntTag(value));
    }

    public void putShort(String tagName, short value) {
        this.value.put(tagName, new ShortTag(value));
    }

    public void putLong(String tagName, long value) {
        this.value.put(tagName, new LongTag(value));
    }

    public void putFloat(String tagName, float value) {
        this.value.put(tagName, new FloatTag(value));
    }

    public void putDouble(String tagName, double value) {
        this.value.put(tagName, new DoubleTag(value));
    }

    public void putBoolean(String tagName, boolean value) {
        this.value.put(tagName, new ByteTag((byte)(value ? 1 : 0)));
    }

    public void putAll(CompoundTag compoundTag) {
        this.value.putAll(compoundTag.value);
    }

    @Nullable
    public <T extends Tag> T remove(String tagName) {
        return (T)this.value.remove(tagName);
    }

    public Set<String> keySet() {
        return this.value.keySet();
    }

    public Collection<Tag> values() {
        return this.value.values();
    }

    public Set<Map.Entry<String, Tag>> entrySet() {
        return this.value.entrySet();
    }

    public int size() {
        return this.value.size();
    }

    public void clear() {
        this.value.clear();
    }

    @Override
    public Iterator<Map.Entry<String, Tag>> iterator() {
        return this.value.entrySet().iterator();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        for (Map.Entry<String, Tag> entry : this.value.entrySet()) {
            Tag tag = entry.getValue();
            out.writeByte(tag.getTagId());
            out.writeUTF(entry.getKey());
            tag.write(out);
        }
        out.writeByte(0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        CompoundTag tags = (CompoundTag)o;
        return this.value.equals(tags.value);
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public CompoundTag copy() {
        LinkedHashMap<String, Tag> newMap = new LinkedHashMap<String, Tag>();
        for (Map.Entry<String, Tag> entry : this.value.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().copy());
        }
        return new CompoundTag(newMap);
    }

    @Override
    public int getTagId() {
        return 10;
    }
}

