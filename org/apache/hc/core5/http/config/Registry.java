/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.config;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.config.Lookup;

@Contract(threading=ThreadingBehavior.SAFE)
public final class Registry<I>
implements Lookup<I> {
    private final Map<String, I> map;

    Registry(Map<String, I> map) {
        this.map = new ConcurrentHashMap<String, I>(map);
    }

    @Override
    public I lookup(String key) {
        if (key == null) {
            return null;
        }
        return this.map.get(key.toLowerCase(Locale.ROOT));
    }

    public String toString() {
        return this.map.toString();
    }
}

