/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.MisdirectedRequestException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.http.protocol.UriPatternMatcher;
import org.apache.hc.core5.http.protocol.UriPatternType;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class RequestHandlerRegistry<T>
implements HttpRequestMapper<T> {
    private static final String LOCALHOST = "localhost";
    private final String canonicalHostName;
    private final Supplier<LookupRegistry<T>> registrySupplier;
    private final LookupRegistry<T> primary;
    private final ConcurrentMap<String, LookupRegistry<T>> virtualMap;

    public RequestHandlerRegistry(String canonicalHostName, Supplier<LookupRegistry<T>> registrySupplier) {
        this.canonicalHostName = Args.notNull(canonicalHostName, "Canonical hostname").toLowerCase(Locale.ROOT);
        this.registrySupplier = registrySupplier != null ? registrySupplier : new Supplier<LookupRegistry<T>>(){

            @Override
            public LookupRegistry<T> get() {
                return new UriPatternMatcher();
            }
        };
        this.primary = this.registrySupplier.get();
        this.virtualMap = new ConcurrentHashMap<String, LookupRegistry<T>>();
    }

    public RequestHandlerRegistry(String canonicalHostName, final UriPatternType patternType) {
        this(canonicalHostName, new Supplier<LookupRegistry<T>>(){

            @Override
            public LookupRegistry<T> get() {
                return UriPatternType.newMatcher(patternType);
            }
        });
    }

    public RequestHandlerRegistry(UriPatternType patternType) {
        this(LOCALHOST, patternType);
    }

    public RequestHandlerRegistry() {
        this(LOCALHOST, UriPatternType.URI_PATTERN);
    }

    private LookupRegistry<T> getPatternMatcher(String hostname) {
        if (hostname == null) {
            return this.primary;
        }
        if (hostname.equals(this.canonicalHostName) || hostname.equals(LOCALHOST)) {
            return this.primary;
        }
        return (LookupRegistry)this.virtualMap.get(hostname);
    }

    @Override
    public T resolve(HttpRequest request, HttpContext context) throws MisdirectedRequestException {
        URIAuthority authority = request.getAuthority();
        String key = authority != null ? authority.getHostName().toLowerCase(Locale.ROOT) : null;
        LookupRegistry<T> patternMatcher = this.getPatternMatcher(key);
        if (patternMatcher == null) {
            throw new MisdirectedRequestException("Not authoritative");
        }
        String path = request.getPath();
        int i = path.indexOf(63);
        if (i != -1) {
            path = path.substring(0, i);
        }
        return patternMatcher.lookup(path);
    }

    public void register(String hostname, String uriPattern, T object) {
        String key;
        Args.notBlank(uriPattern, "URI pattern");
        if (object == null) {
            return;
        }
        String string = key = hostname != null ? hostname.toLowerCase(Locale.ROOT) : null;
        if (hostname == null || hostname.equals(this.canonicalHostName) || hostname.equals(LOCALHOST)) {
            this.primary.register(uriPattern, object);
        } else {
            LookupRegistry<T> newPatternMatcher;
            LookupRegistry<T> patternMatcher = (LookupRegistry<T>)this.virtualMap.get(key);
            if (patternMatcher == null && (patternMatcher = this.virtualMap.putIfAbsent(key, newPatternMatcher = this.registrySupplier.get())) == null) {
                patternMatcher = newPatternMatcher;
            }
            patternMatcher.register(uriPattern, object);
        }
    }
}

