/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.MemberKey;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public final class AnnotatedMethodMap
implements Iterable<AnnotatedMethod> {
    protected Map<MemberKey, AnnotatedMethod> _methods;

    public AnnotatedMethodMap() {
    }

    public AnnotatedMethodMap(Map<MemberKey, AnnotatedMethod> m) {
        this._methods = m;
    }

    public int size() {
        return this._methods == null ? 0 : this._methods.size();
    }

    public AnnotatedMethod find(String name, Class<?>[] paramTypes) {
        if (this._methods == null) {
            return null;
        }
        return this._methods.get(new MemberKey(name, paramTypes));
    }

    public AnnotatedMethod find(Method m) {
        if (this._methods == null) {
            return null;
        }
        return this._methods.get(new MemberKey(m));
    }

    @Override
    public Iterator<AnnotatedMethod> iterator() {
        if (this._methods == null) {
            return Collections.emptyIterator();
        }
        return this._methods.values().iterator();
    }
}

