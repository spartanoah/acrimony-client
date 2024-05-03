/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.DefaultLookupResult;
import org.apache.logging.log4j.core.lookup.LookupResult;

public interface StrLookup {
    public static final String CATEGORY = "Lookup";

    public String lookup(String var1);

    public String lookup(LogEvent var1, String var2);

    default public LookupResult evaluate(String key) {
        String value = this.lookup(key);
        return value == null ? null : new DefaultLookupResult(value);
    }

    default public LookupResult evaluate(LogEvent event, String key) {
        String value = this.lookup(event, key);
        return value == null ? null : new DefaultLookupResult(value);
    }
}

