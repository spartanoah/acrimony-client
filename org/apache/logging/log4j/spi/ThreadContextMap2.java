/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.spi;

import java.util.Map;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.StringMap;

public interface ThreadContextMap2
extends ThreadContextMap {
    public void putAll(Map<String, String> var1);

    public StringMap getReadOnlyContextData();
}

