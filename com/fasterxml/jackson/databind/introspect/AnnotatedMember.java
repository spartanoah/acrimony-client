/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Collections;

public abstract class AnnotatedMember
extends Annotated
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final transient TypeResolutionContext _typeContext;
    protected final transient AnnotationMap _annotations;

    protected AnnotatedMember(TypeResolutionContext ctxt, AnnotationMap annotations) {
        this._typeContext = ctxt;
        this._annotations = annotations;
    }

    protected AnnotatedMember(AnnotatedMember base) {
        this._typeContext = base._typeContext;
        this._annotations = base._annotations;
    }

    public abstract Annotated withAnnotations(AnnotationMap var1);

    public abstract Class<?> getDeclaringClass();

    public abstract Member getMember();

    public String getFullName() {
        return this.getDeclaringClass().getName() + "#" + this.getName();
    }

    @Deprecated
    public TypeResolutionContext getTypeContext() {
        return this._typeContext;
    }

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> acls) {
        if (this._annotations == null) {
            return null;
        }
        return this._annotations.get(acls);
    }

    @Override
    public final boolean hasAnnotation(Class<?> acls) {
        if (this._annotations == null) {
            return false;
        }
        return this._annotations.has(acls);
    }

    @Override
    public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) {
        if (this._annotations == null) {
            return false;
        }
        return this._annotations.hasOneOf(annoClasses);
    }

    @Override
    @Deprecated
    public Iterable<Annotation> annotations() {
        if (this._annotations == null) {
            return Collections.emptyList();
        }
        return this._annotations.annotations();
    }

    public AnnotationMap getAllAnnotations() {
        return this._annotations;
    }

    public final void fixAccess(boolean force) {
        Member m = this.getMember();
        if (m != null) {
            ClassUtil.checkAndFixAccess(m, force);
        }
    }

    public abstract void setValue(Object var1, Object var2) throws UnsupportedOperationException, IllegalArgumentException;

    public abstract Object getValue(Object var1) throws UnsupportedOperationException, IllegalArgumentException;
}

