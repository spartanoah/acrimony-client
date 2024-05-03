/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.util.LRUMap;
import java.io.Serializable;

public class RootNameLookup
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected transient LRUMap<ClassKey, PropertyName> _rootNames = new LRUMap(20, 200);

    public PropertyName findRootName(JavaType rootType, MapperConfig<?> config) {
        return this.findRootName(rootType.getRawClass(), config);
    }

    public PropertyName findRootName(Class<?> rootType, MapperConfig<?> config) {
        AnnotatedClass ac;
        ClassKey key = new ClassKey(rootType);
        PropertyName name = this._rootNames.get(key);
        if (name != null) {
            return name;
        }
        BeanDescription beanDesc = config.introspectClassAnnotations(rootType);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        name = intr.findRootName(ac = beanDesc.getClassInfo());
        if (name == null || !name.hasSimpleName()) {
            name = PropertyName.construct(rootType.getSimpleName());
        }
        this._rootNames.put(key, name);
        return name;
    }

    protected Object readResolve() {
        return new RootNameLookup();
    }
}

