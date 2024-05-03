/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class StdKeyDeserializers
implements KeyDeserializers,
Serializable {
    private static final long serialVersionUID = 1L;

    public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver) {
        return new StdKeyDeserializer.EnumKD(enumResolver, null);
    }

    public static KeyDeserializer constructEnumKeyDeserializer(EnumResolver enumResolver, AnnotatedMethod factory) {
        return new StdKeyDeserializer.EnumKD(enumResolver, factory);
    }

    public static KeyDeserializer constructDelegatingKeyDeserializer(DeserializationConfig config, JavaType type, JsonDeserializer<?> deser) {
        return new StdKeyDeserializer.DelegatingKD(type.getRawClass(), deser);
    }

    public static KeyDeserializer findStringBasedKeyDeserializer(DeserializationConfig config, JavaType type) {
        Object beanDesc = config.introspect(type);
        Constructor<?> ctor = ((BeanDescription)beanDesc).findSingleArgConstructor(String.class);
        if (ctor != null) {
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(ctor, config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            return new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        }
        Method m = ((BeanDescription)beanDesc).findFactoryMethod(String.class);
        if (m != null) {
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(m, config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            return new StdKeyDeserializer.StringFactoryKeyDeserializer(m);
        }
        return null;
    }

    @Override
    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        Class<?> raw = type.getRawClass();
        if (raw.isPrimitive()) {
            raw = ClassUtil.wrapperType(raw);
        }
        return StdKeyDeserializer.forType(raw);
    }
}

