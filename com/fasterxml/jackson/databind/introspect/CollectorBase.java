/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

class CollectorBase {
    protected static final AnnotationMap[] NO_ANNOTATION_MAPS = new AnnotationMap[0];
    protected static final Annotation[] NO_ANNOTATIONS = new Annotation[0];
    protected final AnnotationIntrospector _intr;

    protected CollectorBase(AnnotationIntrospector intr) {
        this._intr = intr;
    }

    protected final AnnotationCollector collectAnnotations(Annotation[] anns) {
        AnnotationCollector c = AnnotationCollector.emptyCollector();
        for (Annotation ann : anns) {
            c = c.addOrOverride(ann);
            if (!this._intr.isAnnotationBundle(ann)) continue;
            c = this.collectFromBundle(c, ann);
        }
        return c;
    }

    protected final AnnotationCollector collectAnnotations(AnnotationCollector c, Annotation[] anns) {
        for (Annotation ann : anns) {
            c = c.addOrOverride(ann);
            if (!this._intr.isAnnotationBundle(ann)) continue;
            c = this.collectFromBundle(c, ann);
        }
        return c;
    }

    protected final AnnotationCollector collectFromBundle(AnnotationCollector c, Annotation bundle) {
        for (Annotation ann : ClassUtil.findClassAnnotations(bundle.annotationType())) {
            if (CollectorBase._ignorableAnnotation(ann)) continue;
            if (this._intr.isAnnotationBundle(ann)) {
                if (c.isPresent(ann)) continue;
                c = c.addOrOverride(ann);
                c = this.collectFromBundle(c, ann);
                continue;
            }
            c = c.addOrOverride(ann);
        }
        return c;
    }

    protected final AnnotationCollector collectDefaultAnnotations(AnnotationCollector c, Annotation[] anns) {
        for (Annotation ann : anns) {
            if (c.isPresent(ann)) continue;
            c = c.addOrOverride(ann);
            if (!this._intr.isAnnotationBundle(ann)) continue;
            c = this.collectDefaultFromBundle(c, ann);
        }
        return c;
    }

    protected final AnnotationCollector collectDefaultFromBundle(AnnotationCollector c, Annotation bundle) {
        for (Annotation ann : ClassUtil.findClassAnnotations(bundle.annotationType())) {
            if (CollectorBase._ignorableAnnotation(ann) || c.isPresent(ann)) continue;
            c = c.addOrOverride(ann);
            if (!this._intr.isAnnotationBundle(ann)) continue;
            c = this.collectFromBundle(c, ann);
        }
        return c;
    }

    protected static final boolean _ignorableAnnotation(Annotation a) {
        return a instanceof Target || a instanceof Retention;
    }

    static AnnotationMap _emptyAnnotationMap() {
        return new AnnotationMap();
    }

    static AnnotationMap[] _emptyAnnotationMaps(int count) {
        if (count == 0) {
            return NO_ANNOTATION_MAPS;
        }
        AnnotationMap[] maps = new AnnotationMap[count];
        for (int i = 0; i < count; ++i) {
            maps[i] = CollectorBase._emptyAnnotationMap();
        }
        return maps;
    }
}

