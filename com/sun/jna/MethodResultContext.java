/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.FunctionResultContext;
import java.lang.reflect.Method;

public class MethodResultContext
extends FunctionResultContext {
    private final Method method;

    MethodResultContext(Class resultClass, Function function, Object[] args, Method method) {
        super(resultClass, function, args);
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }
}

