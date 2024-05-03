/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.lookup.LookupResult;
import org.apache.logging.log4j.core.lookup.StrLookup;

public abstract class AbstractLookup
implements StrLookup {
    @Override
    public String lookup(String key) {
        return this.lookup(null, key);
    }

    @Override
    public LookupResult evaluate(String key) {
        return this.evaluate(null, key);
    }
}

