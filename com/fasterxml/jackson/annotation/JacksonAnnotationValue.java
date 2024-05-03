/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

import java.lang.annotation.Annotation;

public interface JacksonAnnotationValue<A extends Annotation> {
    public Class<A> valueFor();
}

