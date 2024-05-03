/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.util.Annotations;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;

public final class AnnotationMap
implements Annotations {
    protected HashMap<Class<?>, Annotation> _annotations;

    public AnnotationMap() {
    }

    public static AnnotationMap of(Class<?> type, Annotation value) {
        HashMap ann = new HashMap(4);
        ann.put(type, value);
        return new AnnotationMap(ann);
    }

    AnnotationMap(HashMap<Class<?>, Annotation> a) {
        this._annotations = a;
    }

    @Override
    public <A extends Annotation> A get(Class<A> cls) {
        if (this._annotations == null) {
            return null;
        }
        return (A)this._annotations.get(cls);
    }

    @Override
    public boolean has(Class<?> cls) {
        if (this._annotations == null) {
            return false;
        }
        return this._annotations.containsKey(cls);
    }

    @Override
    public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) {
        if (this._annotations != null) {
            int end = annoClasses.length;
            for (int i = 0; i < end; ++i) {
                if (!this._annotations.containsKey(annoClasses[i])) continue;
                return true;
            }
        }
        return false;
    }

    public Iterable<Annotation> annotations() {
        if (this._annotations == null || this._annotations.size() == 0) {
            return Collections.emptyList();
        }
        return this._annotations.values();
    }

    public static AnnotationMap merge(AnnotationMap primary, AnnotationMap secondary) {
        if (primary == null || primary._annotations == null || primary._annotations.isEmpty()) {
            return secondary;
        }
        if (secondary == null || secondary._annotations == null || secondary._annotations.isEmpty()) {
            return primary;
        }
        HashMap annotations = new HashMap();
        for (Annotation ann : secondary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
        }
        for (Annotation ann : primary._annotations.values()) {
            annotations.put(ann.annotationType(), ann);
        }
        return new AnnotationMap(annotations);
    }

    @Override
    public int size() {
        return this._annotations == null ? 0 : this._annotations.size();
    }

    public boolean addIfNotPresent(Annotation ann) {
        if (this._annotations == null || !this._annotations.containsKey(ann.annotationType())) {
            this._add(ann);
            return true;
        }
        return false;
    }

    public boolean add(Annotation ann) {
        return this._add(ann);
    }

    public String toString() {
        if (this._annotations == null) {
            return "[null]";
        }
        return this._annotations.toString();
    }

    protected final boolean _add(Annotation ann) {
        Annotation previous;
        if (this._annotations == null) {
            this._annotations = new HashMap();
        }
        return (previous = this._annotations.put(ann.annotationType(), ann)) == null || !previous.equals(ann);
    }
}

