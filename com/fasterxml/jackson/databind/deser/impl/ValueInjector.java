/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.util.Annotations;
import java.io.IOException;

public class ValueInjector
extends BeanProperty.Std {
    private static final long serialVersionUID = 1L;
    protected final Object _valueId;

    public ValueInjector(PropertyName propName, JavaType type, AnnotatedMember mutator, Object valueId) {
        super(propName, type, null, mutator, PropertyMetadata.STD_OPTIONAL);
        this._valueId = valueId;
    }

    @Deprecated
    public ValueInjector(PropertyName propName, JavaType type, Annotations contextAnnotations, AnnotatedMember mutator, Object valueId) {
        this(propName, type, mutator, valueId);
    }

    public Object findValue(DeserializationContext context, Object beanInstance) throws JsonMappingException {
        return context.findInjectableValue(this._valueId, this, beanInstance);
    }

    public void inject(DeserializationContext context, Object beanInstance) throws IOException {
        this._member.setValue(beanInstance, this.findValue(context, beanInstance));
    }
}

