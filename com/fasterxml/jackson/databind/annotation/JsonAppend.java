/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonAppend {
    public Attr[] attrs() default {};

    public Prop[] props() default {};

    public boolean prepend() default false;

    public static @interface Prop {
        public Class<? extends VirtualBeanPropertyWriter> value();

        public String name() default "";

        public String namespace() default "";

        public JsonInclude.Include include() default JsonInclude.Include.NON_NULL;

        public boolean required() default false;

        public Class<?> type() default Object.class;
    }

    public static @interface Attr {
        public String value();

        public String propName() default "";

        public String propNamespace() default "";

        public JsonInclude.Include include() default JsonInclude.Include.NON_NULL;

        public boolean required() default false;
    }
}

