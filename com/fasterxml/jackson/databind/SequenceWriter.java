/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.TypeWrappedSerializer;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;

public class SequenceWriter
implements Versioned,
Closeable,
Flushable {
    protected final DefaultSerializerProvider _provider;
    protected final SerializationConfig _config;
    protected final JsonGenerator _generator;
    protected final JsonSerializer<Object> _rootSerializer;
    protected final TypeSerializer _typeSerializer;
    protected final boolean _closeGenerator;
    protected final boolean _cfgFlush;
    protected final boolean _cfgCloseCloseable;
    protected PropertySerializerMap _dynamicSerializers;
    protected boolean _openArray;
    protected boolean _closed;

    public SequenceWriter(DefaultSerializerProvider prov, JsonGenerator gen, boolean closeGenerator, ObjectWriter.Prefetch prefetch) throws IOException {
        this._provider = prov;
        this._generator = gen;
        this._closeGenerator = closeGenerator;
        this._rootSerializer = prefetch.getValueSerializer();
        this._typeSerializer = prefetch.getTypeSerializer();
        this._config = prov.getConfig();
        this._cfgFlush = this._config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        this._cfgCloseCloseable = this._config.isEnabled(SerializationFeature.CLOSE_CLOSEABLE);
        this._dynamicSerializers = PropertySerializerMap.emptyForRootValues();
    }

    public SequenceWriter init(boolean wrapInArray) throws IOException {
        if (wrapInArray) {
            this._generator.writeStartArray();
            this._openArray = true;
        }
        return this;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    public SequenceWriter write(Object value) throws IOException {
        Class<?> type;
        if (value == null) {
            this._provider.serializeValue(this._generator, null);
            return this;
        }
        if (this._cfgCloseCloseable && value instanceof Closeable) {
            return this._writeCloseableValue(value);
        }
        JsonSerializer<Object> ser = this._rootSerializer;
        if (ser == null && (ser = this._dynamicSerializers.serializerFor(type = value.getClass())) == null) {
            ser = this._findAndAddDynamic(type);
        }
        this._provider.serializeValue(this._generator, value, null, ser);
        if (this._cfgFlush) {
            this._generator.flush();
        }
        return this;
    }

    public SequenceWriter write(Object value, JavaType type) throws IOException {
        if (value == null) {
            this._provider.serializeValue(this._generator, null);
            return this;
        }
        if (this._cfgCloseCloseable && value instanceof Closeable) {
            return this._writeCloseableValue(value, type);
        }
        JsonSerializer<Object> ser = this._dynamicSerializers.serializerFor(type.getRawClass());
        if (ser == null) {
            ser = this._findAndAddDynamic(type);
        }
        this._provider.serializeValue(this._generator, value, type, ser);
        if (this._cfgFlush) {
            this._generator.flush();
        }
        return this;
    }

    public SequenceWriter writeAll(Object[] value) throws IOException {
        int len = value.length;
        for (int i = 0; i < len; ++i) {
            this.write(value[i]);
        }
        return this;
    }

    public <C extends Collection<?>> SequenceWriter writeAll(C container) throws IOException {
        for (Object value : container) {
            this.write(value);
        }
        return this;
    }

    public SequenceWriter writeAll(Iterable<?> iterable) throws IOException {
        for (Object value : iterable) {
            this.write(value);
        }
        return this;
    }

    @Override
    public void flush() throws IOException {
        if (!this._closed) {
            this._generator.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (!this._closed) {
            this._closed = true;
            if (this._openArray) {
                this._openArray = false;
                this._generator.writeEndArray();
            }
            if (this._closeGenerator) {
                this._generator.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected SequenceWriter _writeCloseableValue(Object value) throws IOException {
        Closeable toClose = (Closeable)value;
        try {
            Class<?> type;
            JsonSerializer<Object> ser = this._rootSerializer;
            if (ser == null && (ser = this._dynamicSerializers.serializerFor(type = value.getClass())) == null) {
                ser = this._findAndAddDynamic(type);
            }
            this._provider.serializeValue(this._generator, value, null, ser);
            if (this._cfgFlush) {
                this._generator.flush();
            }
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } finally {
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException iOException) {}
            }
        }
        return this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected SequenceWriter _writeCloseableValue(Object value, JavaType type) throws IOException {
        Closeable toClose = (Closeable)value;
        try {
            JsonSerializer<Object> ser = this._dynamicSerializers.serializerFor(type.getRawClass());
            if (ser == null) {
                ser = this._findAndAddDynamic(type);
            }
            this._provider.serializeValue(this._generator, value, type, ser);
            if (this._cfgFlush) {
                this._generator.flush();
            }
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } finally {
            if (toClose != null) {
                try {
                    toClose.close();
                } catch (IOException iOException) {}
            }
        }
        return this;
    }

    private final JsonSerializer<Object> _findAndAddDynamic(Class<?> type) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = this._typeSerializer == null ? this._dynamicSerializers.findAndAddRootValueSerializer(type, (SerializerProvider)this._provider) : this._dynamicSerializers.addSerializer(type, (JsonSerializer<Object>)new TypeWrappedSerializer(this._typeSerializer, this._provider.findValueSerializer(type, null)));
        this._dynamicSerializers = result.map;
        return result.serializer;
    }

    private final JsonSerializer<Object> _findAndAddDynamic(JavaType type) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = this._typeSerializer == null ? this._dynamicSerializers.findAndAddRootValueSerializer(type, (SerializerProvider)this._provider) : this._dynamicSerializers.addSerializer(type, (JsonSerializer<Object>)new TypeWrappedSerializer(this._typeSerializer, this._provider.findValueSerializer(type, null)));
        this._dynamicSerializers = result.map;
        return result.serializer;
    }
}

