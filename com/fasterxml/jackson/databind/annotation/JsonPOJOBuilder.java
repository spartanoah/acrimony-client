/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonPOJOBuilder {
    public static final String DEFAULT_BUILD_METHOD = "build";
    public static final String DEFAULT_WITH_PREFIX = "with";

    public String buildMethodName() default "build";

    public String withPrefix() default "with";

    public static class Value {
        public final String buildMethodName;
        public final String withPrefix;

        public Value(JsonPOJOBuilder ann) {
            this(ann.buildMethodName(), ann.withPrefix());
        }

        public Value(String buildMethodName, String withPrefix) {
            this.buildMethodName = buildMethodName;
            this.withPrefix = withPrefix;
        }
    }
}

