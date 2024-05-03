/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.ext.Java7Support;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import java.beans.ConstructorProperties;
import java.beans.Transient;

public class Java7SupportImpl
extends Java7Support {
    private final Class<?> _bogus;

    public Java7SupportImpl() {
        Class cls = Transient.class;
        cls = ConstructorProperties.class;
        this._bogus = cls;
    }

    @Override
    public Boolean findTransient(Annotated a) {
        Transient t = a.getAnnotation(Transient.class);
        if (t != null) {
            return t.value();
        }
        return null;
    }

    @Override
    public Boolean hasCreatorAnnotation(Annotated a) {
        ConstructorProperties props = a.getAnnotation(ConstructorProperties.class);
        if (props != null) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public PropertyName findConstructorName(AnnotatedParameter p) {
        ConstructorProperties props;
        AnnotatedWithParams ctor = p.getOwner();
        if (ctor != null && (props = ctor.getAnnotation(ConstructorProperties.class)) != null) {
            String[] names = props.value();
            int ix = p.getIndex();
            if (ix < names.length) {
                return PropertyName.construct(names[ix]);
            }
        }
        return null;
    }
}

