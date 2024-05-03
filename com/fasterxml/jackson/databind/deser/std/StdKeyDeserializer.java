/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@JacksonStdImpl
public class StdKeyDeserializer
extends KeyDeserializer
implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_BYTE = 2;
    public static final int TYPE_SHORT = 3;
    public static final int TYPE_CHAR = 4;
    public static final int TYPE_INT = 5;
    public static final int TYPE_LONG = 6;
    public static final int TYPE_FLOAT = 7;
    public static final int TYPE_DOUBLE = 8;
    public static final int TYPE_LOCALE = 9;
    public static final int TYPE_DATE = 10;
    public static final int TYPE_CALENDAR = 11;
    public static final int TYPE_UUID = 12;
    public static final int TYPE_URI = 13;
    public static final int TYPE_URL = 14;
    public static final int TYPE_CLASS = 15;
    public static final int TYPE_CURRENCY = 16;
    public static final int TYPE_BYTE_ARRAY = 17;
    protected final int _kind;
    protected final Class<?> _keyClass;
    protected final FromStringDeserializer<?> _deser;

    protected StdKeyDeserializer(int kind, Class<?> cls) {
        this(kind, cls, null);
    }

    protected StdKeyDeserializer(int kind, Class<?> cls, FromStringDeserializer<?> deser) {
        this._kind = kind;
        this._keyClass = cls;
        this._deser = deser;
    }

    public static StdKeyDeserializer forType(Class<?> raw) {
        int kind;
        if (raw == String.class || raw == Object.class || raw == CharSequence.class || raw == Serializable.class) {
            return StringKD.forType(raw);
        }
        if (raw == UUID.class) {
            kind = 12;
        } else if (raw == Integer.class) {
            kind = 5;
        } else if (raw == Long.class) {
            kind = 6;
        } else if (raw == Date.class) {
            kind = 10;
        } else if (raw == Calendar.class) {
            kind = 11;
        } else if (raw == Boolean.class) {
            kind = 1;
        } else if (raw == Byte.class) {
            kind = 2;
        } else if (raw == Character.class) {
            kind = 4;
        } else if (raw == Short.class) {
            kind = 3;
        } else if (raw == Float.class) {
            kind = 7;
        } else if (raw == Double.class) {
            kind = 8;
        } else if (raw == URI.class) {
            kind = 13;
        } else if (raw == URL.class) {
            kind = 14;
        } else if (raw == Class.class) {
            kind = 15;
        } else {
            if (raw == Locale.class) {
                FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Locale.class);
                return new StdKeyDeserializer(9, raw, deser);
            }
            if (raw == Currency.class) {
                FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Currency.class);
                return new StdKeyDeserializer(16, raw, deser);
            }
            if (raw == byte[].class) {
                kind = 17;
            } else {
                return null;
            }
        }
        return new StdKeyDeserializer(kind, raw);
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        if (key == null) {
            return null;
        }
        try {
            Object result = this._parse(key, ctxt);
            if (result != null) {
                return result;
            }
        } catch (Exception re) {
            return ctxt.handleWeirdKey(this._keyClass, key, "not a valid representation, problem: (%s) %s", re.getClass().getName(), ClassUtil.exceptionMessage(re));
        }
        if (ClassUtil.isEnumType(this._keyClass) && ctxt.getConfig().isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            return null;
        }
        return ctxt.handleWeirdKey(this._keyClass, key, "not a valid representation", new Object[0]);
    }

    public Class<?> getKeyClass() {
        return this._keyClass;
    }

    protected Object _parse(String key, DeserializationContext ctxt) throws Exception {
        switch (this._kind) {
            case 1: {
                if ("true".equals(key)) {
                    return Boolean.TRUE;
                }
                if ("false".equals(key)) {
                    return Boolean.FALSE;
                }
                return ctxt.handleWeirdKey(this._keyClass, key, "value not 'true' or 'false'", new Object[0]);
            }
            case 2: {
                int value = this._parseInt(key);
                if (value < -128 || value > 255) {
                    return ctxt.handleWeirdKey(this._keyClass, key, "overflow, value cannot be represented as 8-bit value", new Object[0]);
                }
                return (byte)value;
            }
            case 3: {
                int value = this._parseInt(key);
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    return ctxt.handleWeirdKey(this._keyClass, key, "overflow, value cannot be represented as 16-bit value", new Object[0]);
                }
                return (short)value;
            }
            case 4: {
                if (key.length() == 1) {
                    return Character.valueOf(key.charAt(0));
                }
                return ctxt.handleWeirdKey(this._keyClass, key, "can only convert 1-character Strings", new Object[0]);
            }
            case 5: {
                return this._parseInt(key);
            }
            case 6: {
                return this._parseLong(key);
            }
            case 7: {
                return Float.valueOf((float)this._parseDouble(key));
            }
            case 8: {
                return this._parseDouble(key);
            }
            case 9: {
                try {
                    return this._deser._deserialize(key, ctxt);
                } catch (IllegalArgumentException e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
            case 16: {
                try {
                    return this._deser._deserialize(key, ctxt);
                } catch (IllegalArgumentException e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
            case 10: {
                return ctxt.parseDate(key);
            }
            case 11: {
                return ctxt.constructCalendar(ctxt.parseDate(key));
            }
            case 12: {
                try {
                    return UUID.fromString(key);
                } catch (Exception e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
            case 13: {
                try {
                    return URI.create(key);
                } catch (Exception e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
            case 14: {
                try {
                    return new URL(key);
                } catch (MalformedURLException e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
            case 15: {
                try {
                    return ctxt.findClass(key);
                } catch (Exception e) {
                    return ctxt.handleWeirdKey(this._keyClass, key, "unable to parse key as Class", new Object[0]);
                }
            }
            case 17: {
                try {
                    return ctxt.getConfig().getBase64Variant().decode(key);
                } catch (IllegalArgumentException e) {
                    return this._weirdKey(ctxt, key, e);
                }
            }
        }
        throw new IllegalStateException("Internal error: unknown key type " + this._keyClass);
    }

    protected int _parseInt(String key) throws IllegalArgumentException {
        return Integer.parseInt(key);
    }

    protected long _parseLong(String key) throws IllegalArgumentException {
        return Long.parseLong(key);
    }

    protected double _parseDouble(String key) throws IllegalArgumentException {
        return NumberInput.parseDouble(key);
    }

    protected Object _weirdKey(DeserializationContext ctxt, String key, Exception e) throws IOException {
        return ctxt.handleWeirdKey(this._keyClass, key, "problem: %s", ClassUtil.exceptionMessage(e));
    }

    static final class StringFactoryKeyDeserializer
    extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        final Method _factoryMethod;

        public StringFactoryKeyDeserializer(Method fm) {
            super(-1, fm.getDeclaringClass());
            this._factoryMethod = fm;
        }

        @Override
        public Object _parse(String key, DeserializationContext ctxt) throws Exception {
            return this._factoryMethod.invoke(null, key);
        }
    }

    static final class StringCtorKeyDeserializer
    extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        protected final Constructor<?> _ctor;

        public StringCtorKeyDeserializer(Constructor<?> ctor) {
            super(-1, ctor.getDeclaringClass());
            this._ctor = ctor;
        }

        @Override
        public Object _parse(String key, DeserializationContext ctxt) throws Exception {
            return this._ctor.newInstance(key);
        }
    }

    @JacksonStdImpl
    static final class EnumKD
    extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        protected final EnumResolver _byNameResolver;
        protected final AnnotatedMethod _factory;
        protected EnumResolver _byToStringResolver;
        protected final Enum<?> _enumDefaultValue;

        protected EnumKD(EnumResolver er, AnnotatedMethod factory) {
            super(-1, er.getEnumClass());
            this._byNameResolver = er;
            this._factory = factory;
            this._enumDefaultValue = er.getDefaultValue();
        }

        @Override
        public Object _parse(String key, DeserializationContext ctxt) throws IOException {
            EnumResolver res;
            Enum<?> e;
            if (this._factory != null) {
                try {
                    return this._factory.call1(key);
                } catch (Exception e2) {
                    ClassUtil.unwrapAndThrowAsIAE(e2);
                }
            }
            if ((e = (res = ctxt.isEnabled(DeserializationFeature.READ_ENUMS_USING_TO_STRING) ? this._getToStringResolver(ctxt) : this._byNameResolver).findEnum(key)) == null) {
                if (this._enumDefaultValue != null && ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)) {
                    e = this._enumDefaultValue;
                } else if (!ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
                    return ctxt.handleWeirdKey(this._keyClass, key, "not one of the values accepted for Enum class: %s", res.getEnumIds());
                }
            }
            return e;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private EnumResolver _getToStringResolver(DeserializationContext ctxt) {
            EnumResolver res = this._byToStringResolver;
            if (res == null) {
                EnumKD enumKD = this;
                synchronized (enumKD) {
                    this._byToStringResolver = res = EnumResolver.constructUnsafeUsingToString(this._byNameResolver.getEnumClass(), ctxt.getAnnotationIntrospector());
                }
            }
            return res;
        }
    }

    static final class DelegatingKD
    extends KeyDeserializer
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final Class<?> _keyClass;
        protected final JsonDeserializer<?> _delegate;

        protected DelegatingKD(Class<?> cls, JsonDeserializer<?> deser) {
            this._keyClass = cls;
            this._delegate = deser;
        }

        @Override
        public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            if (key == null) {
                return null;
            }
            TokenBuffer tb = new TokenBuffer(ctxt.getParser(), ctxt);
            tb.writeString(key);
            try {
                JsonParser p = tb.asParser();
                p.nextToken();
                Object result = this._delegate.deserialize(p, ctxt);
                if (result != null) {
                    return result;
                }
                return ctxt.handleWeirdKey(this._keyClass, key, "not a valid representation", new Object[0]);
            } catch (Exception re) {
                return ctxt.handleWeirdKey(this._keyClass, key, "not a valid representation: %s", re.getMessage());
            }
        }

        public Class<?> getKeyClass() {
            return this._keyClass;
        }
    }

    @JacksonStdImpl
    static final class StringKD
    extends StdKeyDeserializer {
        private static final long serialVersionUID = 1L;
        private static final StringKD sString = new StringKD(String.class);
        private static final StringKD sObject = new StringKD(Object.class);

        private StringKD(Class<?> nominalType) {
            super(-1, nominalType);
        }

        public static StringKD forType(Class<?> nominalType) {
            if (nominalType == String.class) {
                return sString;
            }
            if (nominalType == Object.class) {
                return sObject;
            }
            return new StringKD(nominalType);
        }

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return key;
        }
    }
}

