/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.util.CompactStringObjectMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class EnumResolver
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final Class<Enum<?>> _enumClass;
    protected final Enum<?>[] _enums;
    protected final HashMap<String, Enum<?>> _enumsById;
    protected final Enum<?> _defaultValue;

    protected EnumResolver(Class<Enum<?>> enumClass, Enum<?>[] enums, HashMap<String, Enum<?>> map, Enum<?> defaultValue) {
        this._enumClass = enumClass;
        this._enums = enums;
        this._enumsById = map;
        this._defaultValue = defaultValue;
    }

    public static EnumResolver constructFor(Class<Enum<?>> enumCls, AnnotationIntrospector ai) {
        Enum<?>[] enumValues = enumCls.getEnumConstants();
        if (enumValues == null) {
            throw new IllegalArgumentException("No enum constants for class " + enumCls.getName());
        }
        String[] names = ai.findEnumValues(enumCls, enumValues, new String[enumValues.length]);
        String[][] allAliases = new String[names.length][];
        ai.findEnumAliases(enumCls, enumValues, allAliases);
        HashMap map = new HashMap();
        int len = enumValues.length;
        for (int i = 0; i < len; ++i) {
            Enum<?> enumValue = enumValues[i];
            String name = names[i];
            if (name == null) {
                name = enumValue.name();
            }
            map.put(name, enumValue);
            String[] aliases = allAliases[i];
            if (aliases == null) continue;
            for (String alias : aliases) {
                if (map.containsKey(alias)) continue;
                map.put(alias, enumValue);
            }
        }
        return new EnumResolver(enumCls, enumValues, map, ai.findDefaultEnumValue(enumCls));
    }

    @Deprecated
    public static EnumResolver constructUsingToString(Class<Enum<?>> enumCls) {
        return EnumResolver.constructUsingToString(enumCls, null);
    }

    public static EnumResolver constructUsingToString(Class<Enum<?>> enumCls, AnnotationIntrospector ai) {
        Enum<?>[] enumConstants = enumCls.getEnumConstants();
        HashMap map = new HashMap();
        String[][] allAliases = new String[enumConstants.length][];
        ai.findEnumAliases(enumCls, enumConstants, allAliases);
        int i = enumConstants.length;
        while (--i >= 0) {
            Enum<?> enumValue = enumConstants[i];
            map.put(enumValue.toString(), enumValue);
            String[] aliases = allAliases[i];
            if (aliases == null) continue;
            for (String alias : aliases) {
                if (map.containsKey(alias)) continue;
                map.put(alias, enumValue);
            }
        }
        return new EnumResolver(enumCls, enumConstants, map, ai.findDefaultEnumValue(enumCls));
    }

    public static EnumResolver constructUsingMethod(Class<Enum<?>> enumCls, AnnotatedMember accessor, AnnotationIntrospector ai) {
        Enum<?>[] enumValues = enumCls.getEnumConstants();
        HashMap map = new HashMap();
        int i = enumValues.length;
        while (--i >= 0) {
            Enum<?> en = enumValues[i];
            try {
                Object o = accessor.getValue(en);
                if (o == null) continue;
                map.put(o.toString(), en);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to access @JsonValue of Enum value " + en + ": " + e.getMessage());
            }
        }
        Enum<?> defaultEnum = ai != null ? ai.findDefaultEnumValue(enumCls) : null;
        return new EnumResolver(enumCls, enumValues, map, defaultEnum);
    }

    public static EnumResolver constructUnsafe(Class<?> rawEnumCls, AnnotationIntrospector ai) {
        Class<Enum<?>> enumCls = rawEnumCls;
        return EnumResolver.constructFor(enumCls, ai);
    }

    public static EnumResolver constructUnsafeUsingToString(Class<?> rawEnumCls, AnnotationIntrospector ai) {
        Class<Enum<?>> enumCls = rawEnumCls;
        return EnumResolver.constructUsingToString(enumCls, ai);
    }

    public static EnumResolver constructUnsafeUsingMethod(Class<?> rawEnumCls, AnnotatedMember accessor, AnnotationIntrospector ai) {
        Class<Enum<?>> enumCls = rawEnumCls;
        return EnumResolver.constructUsingMethod(enumCls, accessor, ai);
    }

    public CompactStringObjectMap constructLookup() {
        return CompactStringObjectMap.construct(this._enumsById);
    }

    public Enum<?> findEnum(String key) {
        return this._enumsById.get(key);
    }

    public Enum<?> getEnum(int index) {
        if (index < 0 || index >= this._enums.length) {
            return null;
        }
        return this._enums[index];
    }

    public Enum<?> getDefaultValue() {
        return this._defaultValue;
    }

    public Enum<?>[] getRawEnums() {
        return this._enums;
    }

    public List<Enum<?>> getEnums() {
        ArrayList enums = new ArrayList(this._enums.length);
        for (Enum<?> e : this._enums) {
            enums.add(e);
        }
        return enums;
    }

    public Collection<String> getEnumIds() {
        return this._enumsById.keySet();
    }

    public Class<Enum<?>> getEnumClass() {
        return this._enumClass;
    }

    public int lastValidIndex() {
        return this._enums.length - 1;
    }
}

