/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

@Plugin(name="upper", category="Lookup")
public class UpperLookup
implements StrLookup {
    @Override
    public String lookup(String key) {
        return key != null ? key.toUpperCase() : null;
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return this.lookup(key);
    }
}

