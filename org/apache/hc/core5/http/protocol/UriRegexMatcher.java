/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class UriRegexMatcher<T>
implements LookupRegistry<T> {
    private final Map<String, T> objectMap = new LinkedHashMap<String, T>();
    private final Map<String, Pattern> patternMap = new LinkedHashMap<String, Pattern>();

    @Override
    public synchronized void register(String regex, T obj) {
        Args.notNull(regex, "URI request regex");
        this.objectMap.put(regex, obj);
        this.patternMap.put(regex, Pattern.compile(regex));
    }

    @Override
    public synchronized void unregister(String regex) {
        if (regex == null) {
            return;
        }
        this.objectMap.remove(regex);
        this.patternMap.remove(regex);
    }

    @Override
    public synchronized T lookup(String path) {
        Args.notNull(path, "Request path");
        T obj = this.objectMap.get(path);
        if (obj == null) {
            for (Map.Entry<String, Pattern> entry : this.patternMap.entrySet()) {
                if (!entry.getValue().matcher(path).matches()) continue;
                return this.objectMap.get(entry.getKey());
            }
        }
        return obj;
    }

    public String toString() {
        return this.objectMap.toString();
    }
}

