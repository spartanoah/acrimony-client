/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.impl;

import java.util.Map;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.util.StringMap;

public class ThreadContextDataProvider
implements ContextDataProvider {
    @Override
    public Map<String, String> supplyContextData() {
        return ThreadContext.getImmutableContext();
    }

    @Override
    public StringMap supplyStringMap() {
        return ThreadContext.getThreadContextMap().getReadOnlyContextData();
    }
}

