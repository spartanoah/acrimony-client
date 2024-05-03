/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.util.Arrays;

public abstract class PropertySerializerMap {
    protected final boolean _resetWhenFull;

    protected PropertySerializerMap(boolean resetWhenFull) {
        this._resetWhenFull = resetWhenFull;
    }

    protected PropertySerializerMap(PropertySerializerMap base) {
        this._resetWhenFull = base._resetWhenFull;
    }

    public abstract JsonSerializer<Object> serializerFor(Class<?> var1);

    public final SerializerAndMapResult findAndAddPrimarySerializer(Class<?> type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findPrimaryPropertySerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }

    public final SerializerAndMapResult findAndAddPrimarySerializer(JavaType type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findPrimaryPropertySerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
    }

    public final SerializerAndMapResult findAndAddSecondarySerializer(Class<?> type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findContentValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }

    public final SerializerAndMapResult findAndAddSecondarySerializer(JavaType type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findContentValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
    }

    public final SerializerAndMapResult findAndAddRootValueSerializer(Class<?> type, SerializerProvider provider) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findTypedValueSerializer(type, false, null);
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }

    public final SerializerAndMapResult findAndAddRootValueSerializer(JavaType type, SerializerProvider provider) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findTypedValueSerializer(type, false, null);
        return new SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
    }

    public final SerializerAndMapResult findAndAddKeySerializer(Class<?> type, SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> serializer = provider.findKeySerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }

    public final SerializerAndMapResult addSerializer(Class<?> type, JsonSerializer<Object> serializer) {
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }

    public final SerializerAndMapResult addSerializer(JavaType type, JsonSerializer<Object> serializer) {
        return new SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
    }

    public abstract PropertySerializerMap newWith(Class<?> var1, JsonSerializer<Object> var2);

    @Deprecated
    public static PropertySerializerMap emptyMap() {
        return PropertySerializerMap.emptyForProperties();
    }

    public static PropertySerializerMap emptyForProperties() {
        return Empty.FOR_PROPERTIES;
    }

    public static PropertySerializerMap emptyForRootValues() {
        return Empty.FOR_ROOT_VALUES;
    }

    private static final class Multi
    extends PropertySerializerMap {
        private static final int MAX_ENTRIES = 8;
        private final TypeAndSerializer[] _entries;

        public Multi(PropertySerializerMap base, TypeAndSerializer[] entries) {
            super(base);
            this._entries = entries;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type) {
            TypeAndSerializer entry = this._entries[0];
            if (entry.type == type) {
                return entry.serializer;
            }
            entry = this._entries[1];
            if (entry.type == type) {
                return entry.serializer;
            }
            entry = this._entries[2];
            if (entry.type == type) {
                return entry.serializer;
            }
            switch (this._entries.length) {
                case 8: {
                    entry = this._entries[7];
                    if (entry.type == type) {
                        return entry.serializer;
                    }
                }
                case 7: {
                    entry = this._entries[6];
                    if (entry.type == type) {
                        return entry.serializer;
                    }
                }
                case 6: {
                    entry = this._entries[5];
                    if (entry.type == type) {
                        return entry.serializer;
                    }
                }
                case 5: {
                    entry = this._entries[4];
                    if (entry.type == type) {
                        return entry.serializer;
                    }
                }
                case 4: {
                    entry = this._entries[3];
                    if (entry.type != type) break;
                    return entry.serializer;
                }
            }
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            int len = this._entries.length;
            if (len == 8) {
                if (this._resetWhenFull) {
                    return new Single(this, type, serializer);
                }
                return this;
            }
            TypeAndSerializer[] entries = Arrays.copyOf(this._entries, len + 1);
            entries[len] = new TypeAndSerializer(type, serializer);
            return new Multi(this, entries);
        }
    }

    private static final class Double
    extends PropertySerializerMap {
        private final Class<?> _type1;
        private final Class<?> _type2;
        private final JsonSerializer<Object> _serializer1;
        private final JsonSerializer<Object> _serializer2;

        public Double(PropertySerializerMap base, Class<?> type1, JsonSerializer<Object> serializer1, Class<?> type2, JsonSerializer<Object> serializer2) {
            super(base);
            this._type1 = type1;
            this._serializer1 = serializer1;
            this._type2 = type2;
            this._serializer2 = serializer2;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type) {
            if (type == this._type1) {
                return this._serializer1;
            }
            if (type == this._type2) {
                return this._serializer2;
            }
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            TypeAndSerializer[] ts = new TypeAndSerializer[]{new TypeAndSerializer(this._type1, this._serializer1), new TypeAndSerializer(this._type2, this._serializer2), new TypeAndSerializer(type, serializer)};
            return new Multi(this, ts);
        }
    }

    private static final class Single
    extends PropertySerializerMap {
        private final Class<?> _type;
        private final JsonSerializer<Object> _serializer;

        public Single(PropertySerializerMap base, Class<?> type, JsonSerializer<Object> serializer) {
            super(base);
            this._type = type;
            this._serializer = serializer;
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type) {
            if (type == this._type) {
                return this._serializer;
            }
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            return new Double(this, this._type, this._serializer, type, serializer);
        }
    }

    private static final class Empty
    extends PropertySerializerMap {
        public static final Empty FOR_PROPERTIES = new Empty(false);
        public static final Empty FOR_ROOT_VALUES = new Empty(true);

        protected Empty(boolean resetWhenFull) {
            super(resetWhenFull);
        }

        @Override
        public JsonSerializer<Object> serializerFor(Class<?> type) {
            return null;
        }

        @Override
        public PropertySerializerMap newWith(Class<?> type, JsonSerializer<Object> serializer) {
            return new Single(this, type, serializer);
        }
    }

    private static final class TypeAndSerializer {
        public final Class<?> type;
        public final JsonSerializer<Object> serializer;

        public TypeAndSerializer(Class<?> type, JsonSerializer<Object> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static final class SerializerAndMapResult {
        public final JsonSerializer<Object> serializer;
        public final PropertySerializerMap map;

        public SerializerAndMapResult(JsonSerializer<Object> serializer, PropertySerializerMap map) {
            this.serializer = serializer;
            this.map = map;
        }
    }
}

