/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class InjectableValues {
    public abstract Object findInjectableValue(Object var1, DeserializationContext var2, BeanProperty var3, Object var4) throws JsonMappingException;

    public static class Std
    extends InjectableValues
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final Map<String, Object> _values;

        public Std() {
            this(new HashMap<String, Object>());
        }

        public Std(Map<String, Object> values) {
            this._values = values;
        }

        public Std addValue(String key, Object value) {
            this._values.put(key, value);
            return this;
        }

        public Std addValue(Class<?> classKey, Object value) {
            this._values.put(classKey.getName(), value);
            return this;
        }

        @Override
        public Object findInjectableValue(Object valueId, DeserializationContext ctxt, BeanProperty forProperty, Object beanInstance) throws JsonMappingException {
            String key;
            Object ob;
            if (!(valueId instanceof String)) {
                ctxt.reportBadDefinition(ClassUtil.classOf(valueId), String.format("Unrecognized inject value id type (%s), expecting String", ClassUtil.classNameOf(valueId)));
            }
            if ((ob = this._values.get(key = (String)valueId)) == null && !this._values.containsKey(key)) {
                throw new IllegalArgumentException("No injectable id with value '" + key + "' found (for property '" + forProperty.getName() + "')");
            }
            return ob;
        }
    }
}

