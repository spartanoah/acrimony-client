/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.lang.annotation.Annotation;

public interface Annotations {
    public <A extends Annotation> A get(Class<A> var1);

    public boolean has(Class<?> var1);

    public boolean hasOneOf(Class<? extends Annotation>[] var1);

    public int size();
}

