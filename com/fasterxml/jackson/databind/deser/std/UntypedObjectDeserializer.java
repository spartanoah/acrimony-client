/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.ObjectBuffer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JacksonStdImpl
public class UntypedObjectDeserializer
extends StdDeserializer<Object>
implements ResolvableDeserializer,
ContextualDeserializer {
    private static final long serialVersionUID = 1L;
    protected static final Object[] NO_OBJECTS = new Object[0];
    protected JsonDeserializer<Object> _mapDeserializer;
    protected JsonDeserializer<Object> _listDeserializer;
    protected JsonDeserializer<Object> _stringDeserializer;
    protected JsonDeserializer<Object> _numberDeserializer;
    protected JavaType _listType;
    protected JavaType _mapType;
    protected final boolean _nonMerging;

    @Deprecated
    public UntypedObjectDeserializer() {
        this(null, null);
    }

    public UntypedObjectDeserializer(JavaType listType, JavaType mapType) {
        super(Object.class);
        this._listType = listType;
        this._mapType = mapType;
        this._nonMerging = false;
    }

    public UntypedObjectDeserializer(UntypedObjectDeserializer base, JsonDeserializer<?> mapDeser, JsonDeserializer<?> listDeser, JsonDeserializer<?> stringDeser, JsonDeserializer<?> numberDeser) {
        super(Object.class);
        this._mapDeserializer = mapDeser;
        this._listDeserializer = listDeser;
        this._stringDeserializer = stringDeser;
        this._numberDeserializer = numberDeser;
        this._listType = base._listType;
        this._mapType = base._mapType;
        this._nonMerging = base._nonMerging;
    }

    protected UntypedObjectDeserializer(UntypedObjectDeserializer base, boolean nonMerging) {
        super(Object.class);
        this._mapDeserializer = base._mapDeserializer;
        this._listDeserializer = base._listDeserializer;
        this._stringDeserializer = base._stringDeserializer;
        this._numberDeserializer = base._numberDeserializer;
        this._listType = base._listType;
        this._mapType = base._mapType;
        this._nonMerging = nonMerging;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        JavaType obType = ctxt.constructType(Object.class);
        JavaType stringType = ctxt.constructType(String.class);
        TypeFactory tf = ctxt.getTypeFactory();
        this._listDeserializer = this._listType == null ? this._clearIfStdImpl(this._findCustomDeser(ctxt, tf.constructCollectionType(List.class, obType))) : this._findCustomDeser(ctxt, this._listType);
        this._mapDeserializer = this._mapType == null ? this._clearIfStdImpl(this._findCustomDeser(ctxt, tf.constructMapType(Map.class, stringType, obType))) : this._findCustomDeser(ctxt, this._mapType);
        this._stringDeserializer = this._clearIfStdImpl(this._findCustomDeser(ctxt, stringType));
        this._numberDeserializer = this._clearIfStdImpl(this._findCustomDeser(ctxt, tf.constructType((Type)((Object)Number.class))));
        JavaType unknown = TypeFactory.unknownType();
        this._mapDeserializer = ctxt.handleSecondaryContextualization(this._mapDeserializer, null, unknown);
        this._listDeserializer = ctxt.handleSecondaryContextualization(this._listDeserializer, null, unknown);
        this._stringDeserializer = ctxt.handleSecondaryContextualization(this._stringDeserializer, null, unknown);
        this._numberDeserializer = ctxt.handleSecondaryContextualization(this._numberDeserializer, null, unknown);
    }

    protected JsonDeserializer<Object> _findCustomDeser(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        return ctxt.findNonContextualValueDeserializer(type);
    }

    protected JsonDeserializer<Object> _clearIfStdImpl(JsonDeserializer<Object> deser) {
        return ClassUtil.isJacksonStdImpl(deser) ? null : deser;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        boolean preventMerge;
        boolean bl = preventMerge = property == null && Boolean.FALSE.equals(ctxt.getConfig().getDefaultMergeable(Object.class));
        if (this._stringDeserializer == null && this._numberDeserializer == null && this._mapDeserializer == null && this._listDeserializer == null && this.getClass() == UntypedObjectDeserializer.class) {
            return Vanilla.instance(preventMerge);
        }
        if (preventMerge != this._nonMerging) {
            return new UntypedObjectDeserializer(this, preventMerge);
        }
        return this;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return null;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.getCurrentTokenId()) {
            case 1: 
            case 2: 
            case 5: {
                if (this._mapDeserializer != null) {
                    return this._mapDeserializer.deserialize(p, ctxt);
                }
                return this.mapObject(p, ctxt);
            }
            case 3: {
                if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                    return this.mapArrayToArray(p, ctxt);
                }
                if (this._listDeserializer != null) {
                    return this._listDeserializer.deserialize(p, ctxt);
                }
                return this.mapArray(p, ctxt);
            }
            case 12: {
                return p.getEmbeddedObject();
            }
            case 6: {
                if (this._stringDeserializer != null) {
                    return this._stringDeserializer.deserialize(p, ctxt);
                }
                return p.getText();
            }
            case 7: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt);
                }
                if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                    return this._coerceIntegral(p, ctxt);
                }
                return p.getNumberValue();
            }
            case 8: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt);
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return p.getDecimalValue();
                }
                return p.getNumberValue();
            }
            case 9: {
                return Boolean.TRUE;
            }
            case 10: {
                return Boolean.FALSE;
            }
            case 11: {
                return null;
            }
        }
        return ctxt.handleUnexpectedToken(Object.class, p);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        switch (p.getCurrentTokenId()) {
            case 1: 
            case 3: 
            case 5: {
                return typeDeserializer.deserializeTypedFromAny(p, ctxt);
            }
            case 12: {
                return p.getEmbeddedObject();
            }
            case 6: {
                if (this._stringDeserializer != null) {
                    return this._stringDeserializer.deserialize(p, ctxt);
                }
                return p.getText();
            }
            case 7: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt);
                }
                if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                    return this._coerceIntegral(p, ctxt);
                }
                return p.getNumberValue();
            }
            case 8: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt);
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return p.getDecimalValue();
                }
                return p.getNumberValue();
            }
            case 9: {
                return Boolean.TRUE;
            }
            case 10: {
                return Boolean.FALSE;
            }
            case 11: {
                return null;
            }
        }
        return ctxt.handleUnexpectedToken(Object.class, p);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        if (this._nonMerging) {
            return this.deserialize(p, ctxt);
        }
        switch (p.getCurrentTokenId()) {
            case 1: 
            case 2: 
            case 5: {
                if (this._mapDeserializer != null) {
                    return this._mapDeserializer.deserialize(p, ctxt, intoValue);
                }
                if (intoValue instanceof Map) {
                    return this.mapObject(p, ctxt, (Map)intoValue);
                }
                return this.mapObject(p, ctxt);
            }
            case 3: {
                if (this._listDeserializer != null) {
                    return this._listDeserializer.deserialize(p, ctxt, intoValue);
                }
                if (intoValue instanceof Collection) {
                    return this.mapArray(p, ctxt, (Collection)intoValue);
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                    return this.mapArrayToArray(p, ctxt);
                }
                return this.mapArray(p, ctxt);
            }
            case 12: {
                return p.getEmbeddedObject();
            }
            case 6: {
                if (this._stringDeserializer != null) {
                    return this._stringDeserializer.deserialize(p, ctxt, intoValue);
                }
                return p.getText();
            }
            case 7: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt, intoValue);
                }
                if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                    return this._coerceIntegral(p, ctxt);
                }
                return p.getNumberValue();
            }
            case 8: {
                if (this._numberDeserializer != null) {
                    return this._numberDeserializer.deserialize(p, ctxt, intoValue);
                }
                if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return p.getDecimalValue();
                }
                return p.getNumberValue();
            }
            case 9: {
                return Boolean.TRUE;
            }
            case 10: {
                return Boolean.FALSE;
            }
            case 11: {
                return null;
            }
        }
        return this.deserialize(p, ctxt);
    }

    protected Object mapArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return new ArrayList(2);
        }
        Object value = this.deserialize(p, ctxt);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            ArrayList<Object> l = new ArrayList<Object>(2);
            l.add(value);
            return l;
        }
        Object value2 = this.deserialize(p, ctxt);
        if (p.nextToken() == JsonToken.END_ARRAY) {
            ArrayList<Object> l = new ArrayList<Object>(2);
            l.add(value);
            l.add(value2);
            return l;
        }
        ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] values = buffer.resetAndStart();
        int ptr = 0;
        values[ptr++] = value;
        values[ptr++] = value2;
        int totalSize = ptr;
        do {
            value = this.deserialize(p, ctxt);
            ++totalSize;
            if (ptr >= values.length) {
                values = buffer.appendCompletedChunk(values);
                ptr = 0;
            }
            values[ptr++] = value;
        } while (p.nextToken() != JsonToken.END_ARRAY);
        ArrayList<Object> result = new ArrayList<Object>(totalSize);
        buffer.completeAndClearBuffer(values, ptr, result);
        return result;
    }

    protected Object mapArray(JsonParser p, DeserializationContext ctxt, Collection<Object> result) throws IOException {
        while (p.nextToken() != JsonToken.END_ARRAY) {
            result.add(this.deserialize(p, ctxt));
        }
        return result;
    }

    protected Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        String key1;
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            key1 = p.nextFieldName();
        } else if (t == JsonToken.FIELD_NAME) {
            key1 = p.getCurrentName();
        } else {
            if (t != JsonToken.END_OBJECT) {
                return ctxt.handleUnexpectedToken(this.handledType(), p);
            }
            key1 = null;
        }
        if (key1 == null) {
            return new LinkedHashMap(2);
        }
        p.nextToken();
        Object value1 = this.deserialize(p, ctxt);
        String key2 = p.nextFieldName();
        if (key2 == null) {
            LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(2);
            result.put(key1, value1);
            return result;
        }
        p.nextToken();
        Object value2 = this.deserialize(p, ctxt);
        String key = p.nextFieldName();
        if (key == null) {
            LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
            result.put(key1, value1);
            result.put(key2, value2);
            return result;
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key1, value1);
        result.put(key2, value2);
        do {
            p.nextToken();
            result.put(key, this.deserialize(p, ctxt));
        } while ((key = p.nextFieldName()) != null);
        return result;
    }

    protected Object[] mapArrayToArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.nextToken() == JsonToken.END_ARRAY) {
            return NO_OBJECTS;
        }
        ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] values = buffer.resetAndStart();
        int ptr = 0;
        do {
            Object value = this.deserialize(p, ctxt);
            if (ptr >= values.length) {
                values = buffer.appendCompletedChunk(values);
                ptr = 0;
            }
            values[ptr++] = value;
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return buffer.completeAndClearBuffer(values, ptr);
    }

    protected Object mapObject(JsonParser p, DeserializationContext ctxt, Map<Object, Object> m) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = p.nextToken();
        }
        if (t == JsonToken.END_OBJECT) {
            return m;
        }
        String key = p.getCurrentName();
        do {
            p.nextToken();
            Object old = m.get(key);
            Object newV = old != null ? this.deserialize(p, ctxt, old) : this.deserialize(p, ctxt);
            if (newV == old) continue;
            m.put(key, newV);
        } while ((key = p.nextFieldName()) != null);
        return m;
    }

    @JacksonStdImpl
    public static class Vanilla
    extends StdDeserializer<Object> {
        private static final long serialVersionUID = 1L;
        public static final Vanilla std = new Vanilla();
        protected final boolean _nonMerging;

        public Vanilla() {
            this(false);
        }

        protected Vanilla(boolean nonMerging) {
            super(Object.class);
            this._nonMerging = nonMerging;
        }

        public static Vanilla instance(boolean nonMerging) {
            if (nonMerging) {
                return new Vanilla(true);
            }
            return std;
        }

        @Override
        public Boolean supportsUpdate(DeserializationConfig config) {
            return this._nonMerging ? Boolean.FALSE : null;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            switch (p.getCurrentTokenId()) {
                case 1: {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_OBJECT) {
                        return new LinkedHashMap(2);
                    }
                }
                case 5: {
                    return this.mapObject(p, ctxt);
                }
                case 3: {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                            return NO_OBJECTS;
                        }
                        return new ArrayList(2);
                    }
                    if (ctxt.isEnabled(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
                        return this.mapArrayToArray(p, ctxt);
                    }
                    return this.mapArray(p, ctxt);
                }
                case 12: {
                    return p.getEmbeddedObject();
                }
                case 6: {
                    return p.getText();
                }
                case 7: {
                    if (ctxt.hasSomeOfFeatures(F_MASK_INT_COERCIONS)) {
                        return this._coerceIntegral(p, ctxt);
                    }
                    return p.getNumberValue();
                }
                case 8: {
                    if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                        return p.getDecimalValue();
                    }
                    return p.getNumberValue();
                }
                case 9: {
                    return Boolean.TRUE;
                }
                case 10: {
                    return Boolean.FALSE;
                }
                case 2: {
                    return new LinkedHashMap(2);
                }
                case 11: {
                    return null;
                }
            }
            return ctxt.handleUnexpectedToken(Object.class, p);
        }

        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
            switch (p.getCurrentTokenId()) {
                case 1: 
                case 3: 
                case 5: {
                    return typeDeserializer.deserializeTypedFromAny(p, ctxt);
                }
                case 6: {
                    return p.getText();
                }
                case 7: {
                    if (ctxt.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)) {
                        return p.getBigIntegerValue();
                    }
                    return p.getNumberValue();
                }
                case 8: {
                    if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                        return p.getDecimalValue();
                    }
                    return p.getNumberValue();
                }
                case 9: {
                    return Boolean.TRUE;
                }
                case 10: {
                    return Boolean.FALSE;
                }
                case 12: {
                    return p.getEmbeddedObject();
                }
                case 11: {
                    return null;
                }
            }
            return ctxt.handleUnexpectedToken(Object.class, p);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
            if (this._nonMerging) {
                return this.deserialize(p, ctxt);
            }
            switch (p.getCurrentTokenId()) {
                case 2: 
                case 4: {
                    return intoValue;
                }
                case 1: {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_OBJECT) {
                        return intoValue;
                    }
                }
                case 5: {
                    if (!(intoValue instanceof Map)) break;
                    Map m = (Map)intoValue;
                    String key = p.getCurrentName();
                    do {
                        p.nextToken();
                        Object old = m.get(key);
                        Object newV = old != null ? this.deserialize(p, ctxt, old) : this.deserialize(p, ctxt);
                        if (newV == old) continue;
                        m.put(key, newV);
                    } while ((key = p.nextFieldName()) != null);
                    return intoValue;
                }
                case 3: {
                    JsonToken t = p.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        return intoValue;
                    }
                    if (!(intoValue instanceof Collection)) break;
                    Collection c = (Collection)intoValue;
                    do {
                        c.add(this.deserialize(p, ctxt));
                    } while (p.nextToken() != JsonToken.END_ARRAY);
                    return intoValue;
                }
            }
            return this.deserialize(p, ctxt);
        }

        protected Object mapArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object value = this.deserialize(p, ctxt);
            if (p.nextToken() == JsonToken.END_ARRAY) {
                ArrayList<Object> l = new ArrayList<Object>(2);
                l.add(value);
                return l;
            }
            Object value2 = this.deserialize(p, ctxt);
            if (p.nextToken() == JsonToken.END_ARRAY) {
                ArrayList<Object> l = new ArrayList<Object>(2);
                l.add(value);
                l.add(value2);
                return l;
            }
            ObjectBuffer buffer = ctxt.leaseObjectBuffer();
            Object[] values = buffer.resetAndStart();
            int ptr = 0;
            values[ptr++] = value;
            values[ptr++] = value2;
            int totalSize = ptr;
            do {
                value = this.deserialize(p, ctxt);
                ++totalSize;
                if (ptr >= values.length) {
                    values = buffer.appendCompletedChunk(values);
                    ptr = 0;
                }
                values[ptr++] = value;
            } while (p.nextToken() != JsonToken.END_ARRAY);
            ArrayList<Object> result = new ArrayList<Object>(totalSize);
            buffer.completeAndClearBuffer(values, ptr, result);
            return result;
        }

        protected Object[] mapArrayToArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectBuffer buffer = ctxt.leaseObjectBuffer();
            Object[] values = buffer.resetAndStart();
            int ptr = 0;
            do {
                Object value = this.deserialize(p, ctxt);
                if (ptr >= values.length) {
                    values = buffer.appendCompletedChunk(values);
                    ptr = 0;
                }
                values[ptr++] = value;
            } while (p.nextToken() != JsonToken.END_ARRAY);
            return buffer.completeAndClearBuffer(values, ptr);
        }

        protected Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            String key1 = p.getText();
            p.nextToken();
            Object value1 = this.deserialize(p, ctxt);
            String key2 = p.nextFieldName();
            if (key2 == null) {
                LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(2);
                result.put(key1, value1);
                return result;
            }
            p.nextToken();
            Object value2 = this.deserialize(p, ctxt);
            String key = p.nextFieldName();
            if (key == null) {
                LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
                result.put(key1, value1);
                result.put(key2, value2);
                return result;
            }
            LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
            result.put(key1, value1);
            result.put(key2, value2);
            do {
                p.nextToken();
                result.put(key, this.deserialize(p, ctxt));
            } while ((key = p.nextFieldName()) != null);
            return result;
        }
    }
}

