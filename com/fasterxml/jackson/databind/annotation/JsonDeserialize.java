/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.util.Converter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonDeserialize {
    public Class<? extends JsonDeserializer> using() default JsonDeserializer.None.class;

    public Class<? extends JsonDeserializer> contentUsing() default JsonDeserializer.None.class;

    public Class<? extends KeyDeserializer> keyUsing() default KeyDeserializer.None.class;

    public Class<?> builder() default Void.class;

    public Class<? extends Converter> converter() default Converter.None.class;

    public Class<? extends Converter> contentConverter() default Converter.None.class;

    public Class<?> as() default Void.class;

    public Class<?> keyAs() default Void.class;

    public Class<?> contentAs() default Void.class;
}

