/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.JsonLocationInstantiator;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class JDKValueInstantiators {
    public static ValueInstantiator findStdValueInstantiator(DeserializationConfig config, Class<?> raw) {
        if (raw == JsonLocation.class) {
            return new JsonLocationInstantiator();
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (raw == ArrayList.class) {
                return ArrayListInstantiator.INSTANCE;
            }
            if (Collections.EMPTY_SET.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_SET);
            }
            if (Collections.EMPTY_LIST.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_LIST);
            }
        } else if (Map.class.isAssignableFrom(raw)) {
            if (raw == LinkedHashMap.class) {
                return LinkedHashMapInstantiator.INSTANCE;
            }
            if (raw == HashMap.class) {
                return HashMapInstantiator.INSTANCE;
            }
            if (Collections.EMPTY_MAP.getClass() == raw) {
                return new ConstantValueInstantiator(Collections.EMPTY_MAP);
            }
        }
        return null;
    }

    private static class ConstantValueInstantiator
    extends ValueInstantiator.Base
    implements Serializable {
        private static final long serialVersionUID = 2L;
        protected final Object _value;

        public ConstantValueInstantiator(Object value) {
            super(value.getClass());
            this._value = value;
        }

        @Override
        public boolean canInstantiate() {
            return true;
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
            return this._value;
        }
    }

    private static class LinkedHashMapInstantiator
    extends ValueInstantiator.Base
    implements Serializable {
        private static final long serialVersionUID = 2L;
        public static final LinkedHashMapInstantiator INSTANCE = new LinkedHashMapInstantiator();

        public LinkedHashMapInstantiator() {
            super(LinkedHashMap.class);
        }

        @Override
        public boolean canInstantiate() {
            return true;
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
            return new LinkedHashMap();
        }
    }

    private static class HashMapInstantiator
    extends ValueInstantiator.Base
    implements Serializable {
        private static final long serialVersionUID = 2L;
        public static final HashMapInstantiator INSTANCE = new HashMapInstantiator();

        public HashMapInstantiator() {
            super(HashMap.class);
        }

        @Override
        public boolean canInstantiate() {
            return true;
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
            return new HashMap();
        }
    }

    private static class ArrayListInstantiator
    extends ValueInstantiator.Base
    implements Serializable {
        private static final long serialVersionUID = 2L;
        public static final ArrayListInstantiator INSTANCE = new ArrayListInstantiator();

        public ArrayListInstantiator() {
            super(ArrayList.class);
        }

        @Override
        public boolean canInstantiate() {
            return true;
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
            return new ArrayList();
        }
    }
}

