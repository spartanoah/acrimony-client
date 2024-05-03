/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.JavaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;

public abstract class Annotated {
    protected Annotated() {
    }

    public abstract <A extends Annotation> A getAnnotation(Class<A> var1);

    public abstract boolean hasAnnotation(Class<?> var1);

    public abstract boolean hasOneOf(Class<? extends Annotation>[] var1);

    public abstract AnnotatedElement getAnnotated();

    protected abstract int getModifiers();

    public boolean isPublic() {
        return Modifier.isPublic(this.getModifiers());
    }

    public abstract String getName();

    public abstract JavaType getType();

    public abstract Class<?> getRawType();

    @Deprecated
    public abstract Iterable<Annotation> annotations();

    public abstract boolean equals(Object var1);

    public abstract int hashCode();

    public abstract String toString();
}

