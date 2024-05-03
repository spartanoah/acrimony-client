/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class BasicAuthCache
implements AuthCache {
    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthCache.class);
    private final Map<HttpHost, byte[]> map = new ConcurrentHashMap<HttpHost, byte[]>();
    private final SchemePortResolver schemePortResolver;

    public BasicAuthCache(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
    }

    public BasicAuthCache() {
        this(null);
    }

    @Override
    public void put(HttpHost host, AuthScheme authScheme) {
        block19: {
            Args.notNull(host, "HTTP host");
            if (authScheme == null) {
                return;
            }
            if (authScheme instanceof Serializable) {
                try {
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    try (ObjectOutputStream out = new ObjectOutputStream(buf);){
                        out.writeObject(authScheme);
                    }
                    HttpHost key = RoutingSupport.normalize(host, this.schemePortResolver);
                    this.map.put(key, buf.toByteArray());
                } catch (IOException ex) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Unexpected I/O error while serializing auth scheme", ex);
                    }
                    break block19;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Auth scheme {} is not serializable", (Object)authScheme.getClass());
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public AuthScheme get(HttpHost host) {
        Args.notNull(host, "HTTP host");
        HttpHost key = RoutingSupport.normalize(host, this.schemePortResolver);
        byte[] bytes = this.map.get(key);
        if (bytes == null) return null;
        try {
            ByteArrayInputStream buf = new ByteArrayInputStream(bytes);
            try (ObjectInputStream in = new ObjectInputStream(buf);){
                AuthScheme authScheme = (AuthScheme)in.readObject();
                return authScheme;
            }
        } catch (IOException ex) {
            if (!LOG.isWarnEnabled()) return null;
            LOG.warn("Unexpected I/O error while de-serializing auth scheme", ex);
            return null;
        } catch (ClassNotFoundException ex) {
            if (!LOG.isWarnEnabled()) return null;
            LOG.warn("Unexpected error while de-serializing auth scheme", ex);
            return null;
        }
    }

    @Override
    public void remove(HttpHost host) {
        Args.notNull(host, "HTTP host");
        HttpHost key = RoutingSupport.normalize(host, this.schemePortResolver);
        this.map.remove(key);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    public String toString() {
        return this.map.toString();
    }
}

