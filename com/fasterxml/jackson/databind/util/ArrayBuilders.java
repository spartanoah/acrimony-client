/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.PrimitiveArrayBuilder;
import java.lang.reflect.Array;
import java.util.HashSet;

public final class ArrayBuilders {
    private BooleanBuilder _booleanBuilder = null;
    private ByteBuilder _byteBuilder = null;
    private ShortBuilder _shortBuilder = null;
    private IntBuilder _intBuilder = null;
    private LongBuilder _longBuilder = null;
    private FloatBuilder _floatBuilder = null;
    private DoubleBuilder _doubleBuilder = null;

    public BooleanBuilder getBooleanBuilder() {
        if (this._booleanBuilder == null) {
            this._booleanBuilder = new BooleanBuilder();
        }
        return this._booleanBuilder;
    }

    public ByteBuilder getByteBuilder() {
        if (this._byteBuilder == null) {
            this._byteBuilder = new ByteBuilder();
        }
        return this._byteBuilder;
    }

    public ShortBuilder getShortBuilder() {
        if (this._shortBuilder == null) {
            this._shortBuilder = new ShortBuilder();
        }
        return this._shortBuilder;
    }

    public IntBuilder getIntBuilder() {
        if (this._intBuilder == null) {
            this._intBuilder = new IntBuilder();
        }
        return this._intBuilder;
    }

    public LongBuilder getLongBuilder() {
        if (this._longBuilder == null) {
            this._longBuilder = new LongBuilder();
        }
        return this._longBuilder;
    }

    public FloatBuilder getFloatBuilder() {
        if (this._floatBuilder == null) {
            this._floatBuilder = new FloatBuilder();
        }
        return this._floatBuilder;
    }

    public DoubleBuilder getDoubleBuilder() {
        if (this._doubleBuilder == null) {
            this._doubleBuilder = new DoubleBuilder();
        }
        return this._doubleBuilder;
    }

    public static Object getArrayComparator(final Object defaultValue) {
        final int length = Array.getLength(defaultValue);
        final Class<?> defaultValueType = defaultValue.getClass();
        return new Object(){

            public boolean equals(Object other) {
                if (other == this) {
                    return true;
                }
                if (!ClassUtil.hasClass(other, defaultValueType)) {
                    return false;
                }
                if (Array.getLength(other) != length) {
                    return false;
                }
                for (int i = 0; i < length; ++i) {
                    Object value2;
                    Object value1 = Array.get(defaultValue, i);
                    if (value1 == (value2 = Array.get(other, i)) || value1 == null || value1.equals(value2)) continue;
                    return false;
                }
                return true;
            }
        };
    }

    public static <T> HashSet<T> arrayToSet(T[] elements) {
        if (elements != null) {
            int len = elements.length;
            HashSet<T> result = new HashSet<T>(len);
            for (int i = 0; i < len; ++i) {
                result.add(elements[i]);
            }
            return result;
        }
        return new HashSet();
    }

    public static <T> T[] insertInListNoDup(T[] array, T element) {
        int len = array.length;
        for (int ix = 0; ix < len; ++ix) {
            if (array[ix] != element) continue;
            if (ix == 0) {
                return array;
            }
            Object[] result = (Object[])Array.newInstance(array.getClass().getComponentType(), len);
            System.arraycopy(array, 0, result, 1, ix);
            result[0] = element;
            int left = len - ++ix;
            if (left > 0) {
                System.arraycopy(array, ix, result, ix, left);
            }
            return result;
        }
        Object[] result = (Object[])Array.newInstance(array.getClass().getComponentType(), len + 1);
        if (len > 0) {
            System.arraycopy(array, 0, result, 1, len);
        }
        result[0] = element;
        return result;
    }

    public static final class DoubleBuilder
    extends PrimitiveArrayBuilder<double[]> {
        @Override
        public final double[] _constructArray(int len) {
            return new double[len];
        }
    }

    public static final class FloatBuilder
    extends PrimitiveArrayBuilder<float[]> {
        @Override
        public final float[] _constructArray(int len) {
            return new float[len];
        }
    }

    public static final class LongBuilder
    extends PrimitiveArrayBuilder<long[]> {
        @Override
        public final long[] _constructArray(int len) {
            return new long[len];
        }
    }

    public static final class IntBuilder
    extends PrimitiveArrayBuilder<int[]> {
        @Override
        public final int[] _constructArray(int len) {
            return new int[len];
        }
    }

    public static final class ShortBuilder
    extends PrimitiveArrayBuilder<short[]> {
        @Override
        public final short[] _constructArray(int len) {
            return new short[len];
        }
    }

    public static final class ByteBuilder
    extends PrimitiveArrayBuilder<byte[]> {
        @Override
        public final byte[] _constructArray(int len) {
            return new byte[len];
        }
    }

    public static final class BooleanBuilder
    extends PrimitiveArrayBuilder<boolean[]> {
        @Override
        public final boolean[] _constructArray(int len) {
            return new boolean[len];
        }
    }
}

