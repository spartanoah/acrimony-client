/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.util.logging.Logger;

public abstract class Java7Handlers {
    private static final Java7Handlers IMPL;

    public static Java7Handlers instance() {
        return IMPL;
    }

    public abstract Class<?> getClassJavaNioFilePath();

    public abstract JsonDeserializer<?> getDeserializerForJavaNioFilePath(Class<?> var1);

    public abstract JsonSerializer<?> getSerializerForJavaNioFilePath(Class<?> var1);

    static {
        Java7Handlers impl = null;
        try {
            Class<?> cls = Class.forName("com.fasterxml.jackson.databind.ext.Java7HandlersImpl");
            impl = (Java7Handlers)ClassUtil.createInstance(cls, false);
        } catch (Throwable t) {
            Logger.getLogger(Java7Handlers.class.getName()).warning("Unable to load JDK7 types (java.nio.file.Path): no Java7 type support added");
        }
        IMPL = impl;
    }
}

