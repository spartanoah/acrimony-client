/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonSerialize {
    public Class<? extends JsonSerializer> using() default JsonSerializer.None.class;

    public Class<? extends JsonSerializer> contentUsing() default JsonSerializer.None.class;

    public Class<? extends JsonSerializer> keyUsing() default JsonSerializer.None.class;

    public Class<? extends JsonSerializer> nullsUsing() default JsonSerializer.None.class;

    public Class<?> as() default Void.class;

    public Class<?> keyAs() default Void.class;

    public Class<?> contentAs() default Void.class;

    public Typing typing() default Typing.DEFAULT_TYPING;

    public Class<? extends Converter> converter() default Converter.None.class;

    public Class<? extends Converter> contentConverter() default Converter.None.class;

    @Deprecated
    public Inclusion include() default Inclusion.DEFAULT_INCLUSION;

    public static enum Typing {
        DYNAMIC,
        STATIC,
        DEFAULT_TYPING;

    }

    @Deprecated
    public static enum Inclusion {
        ALWAYS,
        NON_NULL,
        NON_DEFAULT,
        NON_EMPTY,
        DEFAULT_INCLUSION;

    }
}

