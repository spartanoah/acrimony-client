/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class TypeNameIdResolver
extends TypeIdResolverBase {
    protected final MapperConfig<?> _config;
    protected final ConcurrentHashMap<String, String> _typeToId;
    protected final Map<String, JavaType> _idToType;
    protected final boolean _caseInsensitive;

    protected TypeNameIdResolver(MapperConfig<?> config, JavaType baseType, ConcurrentHashMap<String, String> typeToId, HashMap<String, JavaType> idToType) {
        super(baseType, config.getTypeFactory());
        this._config = config;
        this._typeToId = typeToId;
        this._idToType = idToType;
        this._caseInsensitive = config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES);
    }

    public static TypeNameIdResolver construct(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
        HashMap<String, JavaType> idToType;
        ConcurrentHashMap<String, String> typeToId;
        if (forSer == forDeser) {
            throw new IllegalArgumentException();
        }
        if (forSer) {
            typeToId = new ConcurrentHashMap<String, String>();
            idToType = null;
        } else {
            idToType = new HashMap<String, JavaType>();
            typeToId = new ConcurrentHashMap(4);
        }
        boolean caseInsensitive = config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES);
        if (subtypes != null) {
            for (NamedType t : subtypes) {
                JavaType prev;
                String id;
                Class<?> cls = t.getType();
                String string = id = t.hasName() ? t.getName() : TypeNameIdResolver._defaultTypeId(cls);
                if (forSer) {
                    typeToId.put(cls.getName(), id);
                }
                if (!forDeser) continue;
                if (caseInsensitive) {
                    id = id.toLowerCase();
                }
                if ((prev = idToType.get(id)) != null && cls.isAssignableFrom(prev.getRawClass())) continue;
                idToType.put(id, config.constructType(cls));
            }
        }
        return new TypeNameIdResolver(config, baseType, typeToId, idToType);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override
    public String idFromValue(Object value) {
        return this.idFromClass(value.getClass());
    }

    protected String idFromClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        String key = clazz.getName();
        String name = this._typeToId.get(key);
        if (name == null) {
            Class<?> cls = this._typeFactory.constructType(clazz).getRawClass();
            if (this._config.isAnnotationProcessingEnabled()) {
                BeanDescription beanDesc = this._config.introspectClassAnnotations(cls);
                name = this._config.getAnnotationIntrospector().findTypeName(beanDesc.getClassInfo());
            }
            if (name == null) {
                name = TypeNameIdResolver._defaultTypeId(cls);
            }
            this._typeToId.put(key, name);
        }
        return name;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> type) {
        if (value == null) {
            return this.idFromClass(type);
        }
        return this.idFromValue(value);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        return this._typeFromId(id);
    }

    protected JavaType _typeFromId(String id) {
        if (this._caseInsensitive) {
            id = id.toLowerCase();
        }
        return this._idToType.get(id);
    }

    @Override
    public String getDescForKnownTypeIds() {
        return new TreeSet<String>(this._idToType.keySet()).toString();
    }

    public String toString() {
        return String.format("[%s; id-to-type=%s]", this.getClass().getName(), this._idToType);
    }

    protected static String _defaultTypeId(Class<?> cls) {
        String n = cls.getName();
        int ix = n.lastIndexOf(46);
        return ix < 0 ? n : n.substring(ix + 1);
    }
}

