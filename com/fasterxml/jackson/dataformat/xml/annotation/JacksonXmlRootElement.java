/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface JacksonXmlRootElement {
    public String namespace() default "";

    public String localName() default "";
}

