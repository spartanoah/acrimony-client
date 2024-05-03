/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public abstract class PrimitiveArrayDeserializers<T>
extends StdDeserializer<T>
implements ContextualDeserializer {
    protected final Boolean _unwrapSingle;
    private transient Object _emptyValue;
    protected final NullValueProvider _nuller;

    protected PrimitiveArrayDeserializers(Class<T> cls) {
        super(cls);
        this._unwrapSingle = null;
        this._nuller = null;
    }

    protected PrimitiveArrayDeserializers(PrimitiveArrayDeserializers<?> base, NullValueProvider nuller, Boolean unwrapSingle) {
        super(base._valueClass);
        this._unwrapSingle = unwrapSingle;
        this._nuller = nuller;
    }

    public static JsonDeserializer<?> forType(Class<?> rawType) {
        if (rawType == Integer.TYPE) {
            return IntDeser.instance;
        }
        if (rawType == Long.TYPE) {
            return LongDeser.instance;
        }
        if (rawType == Byte.TYPE) {
            return new ByteDeser();
        }
        if (rawType == Short.TYPE) {
            return new ShortDeser();
        }
        if (rawType == Float.TYPE) {
            return new FloatDeser();
        }
        if (rawType == Double.TYPE) {
            return new DoubleDeser();
        }
        if (rawType == Boolean.TYPE) {
            return new BooleanDeser();
        }
        if (rawType == Character.TYPE) {
            return new CharDeser();
        }
        throw new IllegalStateException();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        Boolean unwrapSingle = this.findFormatFeature(ctxt, property, this._valueClass, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        NullValueProvider nuller = null;
        Nulls nullStyle = this.findContentNullStyle(ctxt, property);
        if (nullStyle == Nulls.SKIP) {
            nuller = NullsConstantProvider.skipper();
        } else if (nullStyle == Nulls.FAIL) {
            nuller = property == null ? NullsFailProvider.constructForRootValue(ctxt.constructType(this._valueClass.getComponentType())) : NullsFailProvider.constructForProperty(property, property.getType().getContentType());
        }
        if (unwrapSingle == this._unwrapSingle && nuller == this._nuller) {
            return this;
        }
        return this.withResolved(nuller, unwrapSingle);
    }

    protected abstract T _concat(T var1, T var2);

    protected abstract T handleSingleElementUnwrapped(JsonParser var1, DeserializationContext var2) throws IOException;

    protected abstract PrimitiveArrayDeserializers<?> withResolved(NullValueProvider var1, Boolean var2);

    protected abstract T _constructEmpty();

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.TRUE;
    }

    @Override
    public AccessPattern getEmptyAccessPattern() {
        return AccessPattern.CONSTANT;
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        Object empty = this._emptyValue;
        if (empty == null) {
            this._emptyValue = empty = this._constructEmpty();
        }
        return empty;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromArray(p, ctxt);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt, T existing) throws IOException {
        Object newValue = this.deserialize(p, ctxt);
        if (existing == null) {
            return newValue;
        }
        int len = Array.getLength(existing);
        if (len == 0) {
            return newValue;
        }
        return this._concat(existing, newValue);
    }

    protected T handleNonArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        boolean canWrap;
        if (p.hasToken(JsonToken.VALUE_STRING) && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && p.getText().length() == 0) {
            return null;
        }
        boolean bl = canWrap = this._unwrapSingle == Boolean.TRUE || this._unwrapSingle == null && ctxt.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        if (canWrap) {
            return this.handleSingleElementUnwrapped(p, ctxt);
        }
        return (T)ctxt.handleUnexpectedToken(this._valueClass, p);
    }

    protected void _failOnNull(DeserializationContext ctxt) throws IOException {
        throw InvalidNullException.from(ctxt, null, ctxt.constructType(this._valueClass));
    }

    @JacksonStdImpl
    static final class DoubleDeser
    extends PrimitiveArrayDeserializers<double[]> {
        private static final long serialVersionUID = 1L;

        public DoubleDeser() {
            super(double[].class);
        }

        protected DoubleDeser(DoubleDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new DoubleDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected double[] _constructEmpty() {
            return new double[0];
        }

        @Override
        public double[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (double[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.DoubleBuilder builder = ctxt.getArrayBuilders().getDoubleBuilder();
            double[] chunk = (double[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    if (t == JsonToken.VALUE_NULL && this._nuller != null) {
                        this._nuller.getNullValue(ctxt);
                        continue;
                    }
                    double value = this._parseDoublePrimitive(p, ctxt);
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected double[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new double[]{this._parseDoublePrimitive(p, ctxt)};
        }

        @Override
        protected double[] _concat(double[] oldValue, double[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            double[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class FloatDeser
    extends PrimitiveArrayDeserializers<float[]> {
        private static final long serialVersionUID = 1L;

        public FloatDeser() {
            super(float[].class);
        }

        protected FloatDeser(FloatDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new FloatDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected float[] _constructEmpty() {
            return new float[0];
        }

        @Override
        public float[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (float[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.FloatBuilder builder = ctxt.getArrayBuilders().getFloatBuilder();
            float[] chunk = (float[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    if (t == JsonToken.VALUE_NULL && this._nuller != null) {
                        this._nuller.getNullValue(ctxt);
                        continue;
                    }
                    float value = this._parseFloatPrimitive(p, ctxt);
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected float[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new float[]{this._parseFloatPrimitive(p, ctxt)};
        }

        @Override
        protected float[] _concat(float[] oldValue, float[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            float[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class LongDeser
    extends PrimitiveArrayDeserializers<long[]> {
        private static final long serialVersionUID = 1L;
        public static final LongDeser instance = new LongDeser();

        public LongDeser() {
            super(long[].class);
        }

        protected LongDeser(LongDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new LongDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected long[] _constructEmpty() {
            return new long[0];
        }

        @Override
        public long[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (long[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.LongBuilder builder = ctxt.getArrayBuilders().getLongBuilder();
            long[] chunk = (long[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    long value;
                    if (t == JsonToken.VALUE_NUMBER_INT) {
                        value = p.getLongValue();
                    } else if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        value = 0L;
                    } else {
                        value = this._parseLongPrimitive(p, ctxt);
                    }
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected long[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new long[]{this._parseLongPrimitive(p, ctxt)};
        }

        @Override
        protected long[] _concat(long[] oldValue, long[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            long[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class IntDeser
    extends PrimitiveArrayDeserializers<int[]> {
        private static final long serialVersionUID = 1L;
        public static final IntDeser instance = new IntDeser();

        public IntDeser() {
            super(int[].class);
        }

        protected IntDeser(IntDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new IntDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected int[] _constructEmpty() {
            return new int[0];
        }

        @Override
        public int[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (int[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.IntBuilder builder = ctxt.getArrayBuilders().getIntBuilder();
            int[] chunk = (int[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    int value;
                    if (t == JsonToken.VALUE_NUMBER_INT) {
                        value = p.getIntValue();
                    } else if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        value = 0;
                    } else {
                        value = this._parseIntPrimitive(p, ctxt);
                    }
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected int[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new int[]{this._parseIntPrimitive(p, ctxt)};
        }

        @Override
        protected int[] _concat(int[] oldValue, int[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            int[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class ShortDeser
    extends PrimitiveArrayDeserializers<short[]> {
        private static final long serialVersionUID = 1L;

        public ShortDeser() {
            super(short[].class);
        }

        protected ShortDeser(ShortDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new ShortDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected short[] _constructEmpty() {
            return new short[0];
        }

        @Override
        public short[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (short[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.ShortBuilder builder = ctxt.getArrayBuilders().getShortBuilder();
            short[] chunk = (short[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    short value;
                    if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        value = 0;
                    } else {
                        value = this._parseShortPrimitive(p, ctxt);
                    }
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected short[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new short[]{this._parseShortPrimitive(p, ctxt)};
        }

        @Override
        protected short[] _concat(short[] oldValue, short[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            short[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class ByteDeser
    extends PrimitiveArrayDeserializers<byte[]> {
        private static final long serialVersionUID = 1L;

        public ByteDeser() {
            super(byte[].class);
        }

        protected ByteDeser(ByteDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new ByteDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected byte[] _constructEmpty() {
            return new byte[0];
        }

        @Override
        public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken t;
            block16: {
                t = p.currentToken();
                if (t == JsonToken.VALUE_STRING) {
                    try {
                        return p.getBinaryValue(ctxt.getBase64Variant());
                    } catch (JsonParseException e) {
                        String msg = e.getOriginalMessage();
                        if (!msg.contains("base64")) break block16;
                        return (byte[])ctxt.handleWeirdStringValue(byte[].class, p.getText(), msg, new Object[0]);
                    }
                }
            }
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                Object ob = p.getEmbeddedObject();
                if (ob == null) {
                    return null;
                }
                if (ob instanceof byte[]) {
                    return (byte[])ob;
                }
            }
            if (!p.isExpectedStartArrayToken()) {
                return (byte[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.ByteBuilder builder = ctxt.getArrayBuilders().getByteBuilder();
            byte[] chunk = (byte[])builder.resetAndStart();
            int ix = 0;
            try {
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    byte value;
                    if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                        value = p.getByteValue();
                    } else if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        value = 0;
                    } else {
                        value = this._parseBytePrimitive(p, ctxt);
                    }
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected byte[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            byte value;
            JsonToken t = p.currentToken();
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                value = p.getByteValue();
            } else {
                if (t == JsonToken.VALUE_NULL) {
                    if (this._nuller != null) {
                        this._nuller.getNullValue(ctxt);
                        return (byte[])this.getEmptyValue(ctxt);
                    }
                    this._verifyNullForPrimitive(ctxt);
                    return null;
                }
                Number n = (Number)ctxt.handleUnexpectedToken(this._valueClass.getComponentType(), p);
                value = n.byteValue();
            }
            return new byte[]{value};
        }

        @Override
        protected byte[] _concat(byte[] oldValue, byte[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            byte[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class BooleanDeser
    extends PrimitiveArrayDeserializers<boolean[]> {
        private static final long serialVersionUID = 1L;

        public BooleanDeser() {
            super(boolean[].class);
        }

        protected BooleanDeser(BooleanDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return new BooleanDeser(this, nuller, unwrapSingle);
        }

        @Override
        protected boolean[] _constructEmpty() {
            return new boolean[0];
        }

        @Override
        public boolean[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                return (boolean[])this.handleNonArray(p, ctxt);
            }
            ArrayBuilders.BooleanBuilder builder = ctxt.getArrayBuilders().getBooleanBuilder();
            boolean[] chunk = (boolean[])builder.resetAndStart();
            int ix = 0;
            try {
                JsonToken t;
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    boolean value;
                    if (t == JsonToken.VALUE_TRUE) {
                        value = true;
                    } else if (t == JsonToken.VALUE_FALSE) {
                        value = false;
                    } else if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        value = false;
                    } else {
                        value = this._parseBooleanPrimitive(ctxt, p, Boolean.TYPE);
                    }
                    if (ix >= chunk.length) {
                        chunk = builder.appendCompletedChunk(chunk, ix);
                        ix = 0;
                    }
                    chunk[ix++] = value;
                }
            } catch (Exception e) {
                throw JsonMappingException.wrapWithPath((Throwable)e, (Object)chunk, builder.bufferedSize() + ix);
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }

        @Override
        protected boolean[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new boolean[]{this._parseBooleanPrimitive(ctxt, p, Boolean.TYPE)};
        }

        @Override
        protected boolean[] _concat(boolean[] oldValue, boolean[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            boolean[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }

    @JacksonStdImpl
    static final class CharDeser
    extends PrimitiveArrayDeserializers<char[]> {
        private static final long serialVersionUID = 1L;

        public CharDeser() {
            super(char[].class);
        }

        protected CharDeser(CharDeser base, NullValueProvider nuller, Boolean unwrapSingle) {
            super(base, nuller, unwrapSingle);
        }

        @Override
        protected PrimitiveArrayDeserializers<?> withResolved(NullValueProvider nuller, Boolean unwrapSingle) {
            return this;
        }

        @Override
        protected char[] _constructEmpty() {
            return new char[0];
        }

        @Override
        public char[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.hasToken(JsonToken.VALUE_STRING)) {
                char[] buffer = p.getTextCharacters();
                int offset = p.getTextOffset();
                int len = p.getTextLength();
                char[] result = new char[len];
                System.arraycopy(buffer, offset, result, 0, len);
                return result;
            }
            if (p.isExpectedStartArrayToken()) {
                JsonToken t;
                StringBuilder sb = new StringBuilder(64);
                while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                    String str;
                    if (t == JsonToken.VALUE_STRING) {
                        str = p.getText();
                    } else if (t == JsonToken.VALUE_NULL) {
                        if (this._nuller != null) {
                            this._nuller.getNullValue(ctxt);
                            continue;
                        }
                        this._verifyNullForPrimitive(ctxt);
                        str = "\u0000";
                    } else {
                        CharSequence cs = (CharSequence)ctxt.handleUnexpectedToken(Character.TYPE, p);
                        str = cs.toString();
                    }
                    if (str.length() != 1) {
                        ctxt.reportInputMismatch(this, "Cannot convert a JSON String of length %d into a char element of char array", str.length());
                    }
                    sb.append(str.charAt(0));
                }
                return sb.toString().toCharArray();
            }
            if (p.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
                Object ob = p.getEmbeddedObject();
                if (ob == null) {
                    return null;
                }
                if (ob instanceof char[]) {
                    return (char[])ob;
                }
                if (ob instanceof String) {
                    return ((String)ob).toCharArray();
                }
                if (ob instanceof byte[]) {
                    return Base64Variants.getDefaultVariant().encode((byte[])ob, false).toCharArray();
                }
            }
            return (char[])ctxt.handleUnexpectedToken(this._valueClass, p);
        }

        @Override
        protected char[] handleSingleElementUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
            return (char[])ctxt.handleUnexpectedToken(this._valueClass, p);
        }

        @Override
        protected char[] _concat(char[] oldValue, char[] newValue) {
            int len1 = oldValue.length;
            int len2 = newValue.length;
            char[] result = Arrays.copyOf(oldValue, len1 + len2);
            System.arraycopy(newValue, 0, result, len1, len2);
            return result;
        }
    }
}

