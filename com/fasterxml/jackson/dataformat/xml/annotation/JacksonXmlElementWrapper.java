/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface JacksonXmlElementWrapper {
    public static final String USE_PROPERTY_NAME = "";

    public String namespace() default "";

    public String localName() default "";

    public boolean useWrapping() default true;
}

