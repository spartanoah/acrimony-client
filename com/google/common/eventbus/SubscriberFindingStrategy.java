/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.eventbus;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventSubscriber;

interface SubscriberFindingStrategy {
    public Multimap<Class<?>, EventSubscriber> findAllSubscribers(Object var1);
}

