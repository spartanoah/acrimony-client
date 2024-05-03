/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonschema;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonSerializableSchema {
    public static final String NO_VALUE = "##irrelevant";

    public String id() default "";

    public String schemaType() default "any";

    @Deprecated
    public String schemaObjectPropertiesDefinition() default "##irrelevant";

    @Deprecated
    public String schemaItemDefinition() default "##irrelevant";
}

