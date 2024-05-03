/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.viaversion.viaversion.libs.opennbt.conversion;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.opennbt.conversion.ConversionException;
import com.viaversion.viaversion.libs.opennbt.conversion.TagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.ByteArrayTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.ByteTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.CompoundTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.DoubleTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.FloatTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.IntArrayTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.IntTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.ListTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.LongArrayTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.LongTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.ShortTagConverter;
import com.viaversion.viaversion.libs.opennbt.conversion.converter.StringTagConverter;
import com.viaversion.viaversion.libs.opennbt.tag.TagRegistry;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.DoubleTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.FloatTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ShortTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class ConverterRegistry {
    private static final Int2ObjectMap<TagConverter<? extends Tag, ?>> TAG_TO_CONVERTER = new Int2ObjectOpenHashMap();
    private static final Map<Class<?>, TagConverter<? extends Tag, ?>> TYPE_TO_CONVERTER = new HashMap();

    public static <T extends Tag, V> void register(Class<T> tag, Class<? extends V> type, TagConverter<T, V> converter) {
        int tagId = TagRegistry.getIdFor(tag);
        if (tagId == -1) {
            throw new IllegalArgumentException("Tag " + tag.getName() + " is not a registered tag.");
        }
        if (TAG_TO_CONVERTER.containsKey(tagId)) {
            throw new IllegalArgumentException("Type conversion to tag " + tag.getName() + " is already registered.");
        }
        if (TYPE_TO_CONVERTER.containsKey(type)) {
            throw new IllegalArgumentException("Tag conversion to type " + type.getName() + " is already registered.");
        }
        TAG_TO_CONVERTER.put(tagId, (TagConverter<Tag, ?>)converter);
        TYPE_TO_CONVERTER.put(type, converter);
    }

    public static <T extends Tag, V> void unregister(Class<T> tag, Class<V> type) {
        TAG_TO_CONVERTER.remove(TagRegistry.getIdFor(tag));
        TYPE_TO_CONVERTER.remove(type);
    }

    @Nullable
    public static <T extends Tag, V> V convertToValue(@Nullable T tag) throws ConversionException {
        if (tag == null || tag.getValue() == null) {
            return null;
        }
        TagConverter converter = (TagConverter)TAG_TO_CONVERTER.get(tag.getTagId());
        if (converter == null) {
            throw new ConversionException("Tag type " + tag.getClass().getName() + " has no converter.");
        }
        return (V)converter.convert(tag);
    }

    @Nullable
    public static <V, T extends Tag> T convertToTag(@Nullable V value) throws ConversionException {
        if (value == null) {
            return null;
        }
        Class<?> valueClass = value.getClass();
        TagConverter<Tag, ?> converter = TYPE_TO_CONVERTER.get(valueClass);
        if (converter == null) {
            Class<?> interfaceClass;
            Class<?>[] classArray = valueClass.getInterfaces();
            int n = classArray.length;
            for (int i = 0; i < n && (converter = TYPE_TO_CONVERTER.get(interfaceClass = classArray[i])) == null; ++i) {
            }
            if (converter == null) {
                throw new ConversionException("Value type " + valueClass.getName() + " has no converter.");
            }
        }
        return (T)converter.convert(value);
    }

    static {
        ConverterRegistry.register(ByteTag.class, Byte.class, new ByteTagConverter());
        ConverterRegistry.register(ShortTag.class, Short.class, new ShortTagConverter());
        ConverterRegistry.register(IntTag.class, Integer.class, new IntTagConverter());
        ConverterRegistry.register(LongTag.class, Long.class, new LongTagConverter());
        ConverterRegistry.register(FloatTag.class, Float.class, new FloatTagConverter());
        ConverterRegistry.register(DoubleTag.class, Double.class, new DoubleTagConverter());
        ConverterRegistry.register(ByteArrayTag.class, byte[].class, new ByteArrayTagConverter());
        ConverterRegistry.register(StringTag.class, String.class, new StringTagConverter());
        ConverterRegistry.register(ListTag.class, List.class, new ListTagConverter());
        ConverterRegistry.register(CompoundTag.class, Map.class, new CompoundTagConverter());
        ConverterRegistry.register(IntArrayTag.class, int[].class, new IntArrayTagConverter());
        ConverterRegistry.register(LongArrayTag.class, long[].class, new LongArrayTagConverter());
    }
}

