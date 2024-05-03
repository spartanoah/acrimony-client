/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonProperty {
    public static final String USE_DEFAULT_NAME = "";
    public static final int INDEX_UNKNOWN = -1;

    public String value() default "";

    public boolean required() default false;

    public int index() default -1;

    public String defaultValue() default "";

    public Access access() default Access.AUTO;

    public static enum Access {
        AUTO,
        READ_ONLY,
        WRITE_ONLY,
        READ_WRITE;

    }
}

