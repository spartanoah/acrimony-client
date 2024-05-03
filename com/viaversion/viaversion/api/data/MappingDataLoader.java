/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.data;

import com.google.common.annotations.Beta;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.FullMappingsBase;
import com.viaversion.viaversion.api.data.IdentityMappings;
import com.viaversion.viaversion.api.data.IntArrayMappings;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonIOException;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.io.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.io.TagReader;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MappingDataLoader {
    private static final Map<String, CompoundTag> MAPPINGS_CACHE = new HashMap<String, CompoundTag>();
    private static final TagReader<CompoundTag> MAPPINGS_READER = NBTIO.reader(CompoundTag.class).named();
    private static final byte DIRECT_ID = 0;
    private static final byte SHIFTS_ID = 1;
    private static final byte CHANGES_ID = 2;
    private static final byte IDENTITY_ID = 3;
    private static boolean cacheValid = true;

    public static void clearCache() {
        MAPPINGS_CACHE.clear();
        cacheValid = false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable JsonObject loadFromDataDir(String name) {
        File file = new File(Via.getPlatform().getDataFolder(), name);
        if (!file.exists()) {
            return MappingDataLoader.loadData(name);
        }
        try (FileReader reader = new FileReader(file);){
            JsonObject jsonObject = GsonUtil.getGson().fromJson((Reader)reader, JsonObject.class);
            return jsonObject;
        } catch (JsonSyntaxException e) {
            Via.getPlatform().getLogger().warning(name + " is badly formatted!");
            throw new RuntimeException(e);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable JsonObject loadData(String name) {
        InputStream stream = MappingDataLoader.getResource(name);
        if (stream == null) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(stream);){
            JsonObject jsonObject = GsonUtil.getGson().fromJson((Reader)reader, JsonObject.class);
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable CompoundTag loadNBT(String name, boolean cache) {
        if (!cacheValid) {
            return MappingDataLoader.loadNBTFromFile(name);
        }
        CompoundTag data = MAPPINGS_CACHE.get(name);
        if (data != null) {
            return data;
        }
        data = MappingDataLoader.loadNBTFromFile(name);
        if (cache && data != null) {
            MAPPINGS_CACHE.put(name, data);
        }
        return data;
    }

    public static @Nullable CompoundTag loadNBT(String name) {
        return MappingDataLoader.loadNBT(name, false);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable CompoundTag loadNBTFromFile(String name) {
        InputStream resource = MappingDataLoader.getResource(name);
        if (resource == null) {
            return null;
        }
        try (InputStream stream = resource;){
            CompoundTag compoundTag = MAPPINGS_READER.read(stream);
            return compoundTag;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable Mappings loadMappings(CompoundTag mappingsTag, String key) {
        return MappingDataLoader.loadMappings(mappingsTag, key, size -> {
            int[] array = new int[size];
            Arrays.fill(array, -1);
            return array;
        }, (array, id, mappedId) -> {
            array[id] = mappedId;
        }, IntArrayMappings::of);
    }

    @Beta
    public static <M extends Mappings, V> @Nullable Mappings loadMappings(CompoundTag mappingsTag, String key, MappingHolderSupplier<V> holderSupplier, AddConsumer<V> addConsumer, MappingsSupplier<M, V> mappingsSupplier) {
        V mappings;
        CompoundTag tag = (CompoundTag)mappingsTag.get(key);
        if (tag == null) {
            return null;
        }
        ByteTag serializationStragetyTag = (ByteTag)tag.get("id");
        IntTag mappedSizeTag = (IntTag)tag.get("mappedSize");
        byte strategy = serializationStragetyTag.asByte();
        if (strategy == 0) {
            IntArrayTag valuesTag = (IntArrayTag)tag.get("val");
            return IntArrayMappings.of(valuesTag.getValue(), mappedSizeTag.asInt());
        }
        if (strategy == 1) {
            IntArrayTag shiftsAtTag = (IntArrayTag)tag.get("at");
            IntArrayTag shiftsTag = (IntArrayTag)tag.get("to");
            IntTag sizeTag = (IntTag)tag.get("size");
            int[] shiftsAt = shiftsAtTag.getValue();
            int[] shiftsTo = shiftsTag.getValue();
            int size = sizeTag.asInt();
            mappings = holderSupplier.get(size);
            if (shiftsAt[0] != 0) {
                int to = shiftsAt[0];
                for (int id = 0; id < to; ++id) {
                    addConsumer.addTo(mappings, id, id);
                }
            }
            for (int i = 0; i < shiftsAt.length; ++i) {
                int from = shiftsAt[i];
                int to = i == shiftsAt.length - 1 ? size : shiftsAt[i + 1];
                int mappedId = shiftsTo[i];
                for (int id = from; id < to; ++id) {
                    addConsumer.addTo(mappings, id, mappedId++);
                }
            }
        } else if (strategy == 2) {
            IntArrayTag changesAtTag = (IntArrayTag)tag.get("at");
            IntArrayTag valuesTag = (IntArrayTag)tag.get("val");
            IntTag sizeTag = (IntTag)tag.get("size");
            boolean fillBetween = tag.get("nofill") == null;
            int[] changesAt = changesAtTag.getValue();
            int[] values = valuesTag.getValue();
            mappings = holderSupplier.get(sizeTag.asInt());
            for (int i = 0; i < changesAt.length; ++i) {
                int id = changesAt[i];
                if (fillBetween) {
                    int previousId;
                    for (int identity = previousId = i != 0 ? changesAt[i - 1] + 1 : 0; identity < id; ++identity) {
                        addConsumer.addTo(mappings, identity, identity);
                    }
                }
                addConsumer.addTo(mappings, id, values[i]);
            }
        } else {
            if (strategy == 3) {
                IntTag sizeTag = (IntTag)tag.get("size");
                return new IdentityMappings(sizeTag.asInt(), mappedSizeTag.asInt());
            }
            throw new IllegalArgumentException("Unknown serialization strategy: " + strategy);
        }
        return mappingsSupplier.create(mappings, mappedSizeTag.asInt());
    }

    public static FullMappings loadFullMappings(CompoundTag mappingsTag, CompoundTag unmappedIdentifiers, CompoundTag mappedIdentifiers, String key) {
        ListTag unmappedElements = (ListTag)unmappedIdentifiers.get(key);
        ListTag mappedElements = (ListTag)mappedIdentifiers.get(key);
        if (unmappedElements == null || mappedElements == null) {
            return null;
        }
        Mappings mappings = MappingDataLoader.loadMappings(mappingsTag, key);
        if (mappings == null) {
            mappings = new IdentityMappings(unmappedElements.size(), mappedElements.size());
        }
        return new FullMappingsBase(unmappedElements.getValue().stream().map(t -> (String)t.getValue()).collect(Collectors.toList()), mappedElements.getValue().stream().map(t -> (String)t.getValue()).collect(Collectors.toList()), mappings);
    }

    public static Object2IntMap<String> indexedObjectToMap(JsonObject object) {
        Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<String>(object.size(), 0.99f);
        map.defaultReturnValue(-1);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }
        return map;
    }

    public static Object2IntMap<String> arrayToMap(JsonArray array) {
        Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<String>(array.size(), 0.99f);
        map.defaultReturnValue(-1);
        for (int i = 0; i < array.size(); ++i) {
            map.put(array.get(i).getAsString(), i);
        }
        return map;
    }

    public static @Nullable InputStream getResource(String name) {
        return MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    }

    @FunctionalInterface
    public static interface MappingsSupplier<T extends Mappings, V> {
        public T create(V var1, int var2);
    }

    @FunctionalInterface
    public static interface MappingHolderSupplier<T> {
        public T get(int var1);
    }

    @FunctionalInterface
    public static interface AddConsumer<T> {
        public void addTo(T var1, int var2, int var3);
    }
}

