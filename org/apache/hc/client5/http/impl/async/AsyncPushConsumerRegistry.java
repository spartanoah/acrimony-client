/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.protocol.UriPatternMatcher;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;

class AsyncPushConsumerRegistry {
    private final UriPatternMatcher<Supplier<AsyncPushConsumer>> primary = new UriPatternMatcher();
    private final ConcurrentMap<String, UriPatternMatcher<Supplier<AsyncPushConsumer>>> hostMap = new ConcurrentHashMap<String, UriPatternMatcher<Supplier<AsyncPushConsumer>>>();

    private UriPatternMatcher<Supplier<AsyncPushConsumer>> getPatternMatcher(String hostname) {
        if (hostname == null) {
            return this.primary;
        }
        UriPatternMatcher hostMatcher = (UriPatternMatcher)this.hostMap.get(hostname);
        if (hostMatcher != null) {
            return hostMatcher;
        }
        return this.primary;
    }

    public AsyncPushConsumer get(HttpRequest request) {
        Supplier<AsyncPushConsumer> supplier;
        Args.notNull(request, "Request");
        URIAuthority authority = request.getAuthority();
        String key = authority != null ? authority.getHostName().toLowerCase(Locale.ROOT) : null;
        UriPatternMatcher<Supplier<AsyncPushConsumer>> patternMatcher = this.getPatternMatcher(key);
        if (patternMatcher == null) {
            return null;
        }
        String path = request.getPath();
        int i = path.indexOf(63);
        if (i != -1) {
            path = path.substring(0, i);
        }
        return (supplier = patternMatcher.lookup(path)) != null ? supplier.get() : null;
    }

    public void register(String hostname, String uriPattern, Supplier<AsyncPushConsumer> supplier) {
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(supplier, "Supplier");
        if (hostname == null) {
            this.primary.register(uriPattern, supplier);
        } else {
            UriPatternMatcher newMatcher;
            String key = hostname.toLowerCase(Locale.ROOT);
            UriPatternMatcher<Supplier<AsyncPushConsumer>> matcher = (UriPatternMatcher<Supplier<AsyncPushConsumer>>)this.hostMap.get(key);
            if (matcher == null && (matcher = this.hostMap.putIfAbsent(key, newMatcher = new UriPatternMatcher())) == null) {
                matcher = newMatcher;
            }
            matcher.register(uriPattern, supplier);
        }
    }
}

