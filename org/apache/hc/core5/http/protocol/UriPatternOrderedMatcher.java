/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class UriPatternOrderedMatcher<T>
implements LookupRegistry<T> {
    private final Map<String, T> map = new LinkedHashMap<String, T>();

    public synchronized Set<Map.Entry<String, T>> entrySet() {
        return new HashSet<Map.Entry<String, T>>(this.map.entrySet());
    }

    @Override
    public synchronized void register(String pattern, T obj) {
        Args.notNull(pattern, "URI request pattern");
        this.map.put(pattern, obj);
    }

    @Override
    public synchronized void unregister(String pattern) {
        if (pattern == null) {
            return;
        }
        this.map.remove(pattern);
    }

    @Override
    public synchronized T lookup(String path) {
        Args.notNull(path, "Request path");
        for (Map.Entry<String, T> entry : this.map.entrySet()) {
            String pattern = entry.getKey();
            if (path.equals(pattern)) {
                return entry.getValue();
            }
            if (!this.matchUriRequestPattern(pattern, path)) continue;
            return this.map.get(pattern);
        }
        return null;
    }

    protected boolean matchUriRequestPattern(String pattern, String path) {
        if (pattern.equals("*")) {
            return true;
        }
        return pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1)) || pattern.startsWith("*") && path.endsWith(pattern.substring(1, pattern.length()));
    }

    public String toString() {
        return this.map.toString();
    }
}

