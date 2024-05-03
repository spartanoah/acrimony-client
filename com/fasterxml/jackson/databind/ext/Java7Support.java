/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.util.ClassUtil;

public abstract class Java7Support {
    private static final Java7Support IMPL;

    public static Java7Support instance() {
        return IMPL;
    }

    public abstract Boolean findTransient(Annotated var1);

    public abstract Boolean hasCreatorAnnotation(Annotated var1);

    public abstract PropertyName findConstructorName(AnnotatedParameter var1);

    static {
        Java7Support impl = null;
        try {
            Class<?> cls = Class.forName("com.fasterxml.jackson.databind.ext.Java7SupportImpl");
            impl = (Java7Support)ClassUtil.createInstance(cls, false);
        } catch (Throwable throwable) {
            // empty catch block
        }
        IMPL = impl;
    }
}

