/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.eventbus;

import com.google.common.eventbus.EventSubscriber;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class SynchronizedEventSubscriber
extends EventSubscriber {
    public SynchronizedEventSubscriber(Object target, Method method) {
        super(target, method);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleEvent(Object event) throws InvocationTargetException {
        SynchronizedEventSubscriber synchronizedEventSubscriber = this;
        synchronized (synchronizedEventSubscriber) {
            super.handleEvent(event);
        }
    }
}

