/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.ToNativeContext;
import java.lang.reflect.Method;

public class CallbackResultContext
extends ToNativeContext {
    private Method method;

    CallbackResultContext(Method callbackMethod) {
        this.method = callbackMethod;
    }

    public Method getMethod() {
        return this.method;
    }
}

