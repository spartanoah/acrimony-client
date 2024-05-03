/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class BasicCredentialsProvider
implements CredentialsStore {
    private final ConcurrentHashMap<AuthScope, Credentials> credMap = new ConcurrentHashMap();

    @Override
    public void setCredentials(AuthScope authScope, Credentials credentials) {
        Args.notNull(authScope, "Authentication scope");
        this.credMap.put(authScope, credentials);
    }

    private static Credentials matchCredentials(Map<AuthScope, Credentials> map, AuthScope authScope) {
        Credentials creds = map.get(authScope);
        if (creds == null) {
            int bestMatchFactor = -1;
            AuthScope bestMatch = null;
            for (AuthScope current : map.keySet()) {
                int factor = authScope.match(current);
                if (factor <= bestMatchFactor) continue;
                bestMatchFactor = factor;
                bestMatch = current;
            }
            if (bestMatch != null) {
                creds = map.get(bestMatch);
            }
        }
        return creds;
    }

    @Override
    public Credentials getCredentials(AuthScope authScope, HttpContext httpContext) {
        Args.notNull(authScope, "Authentication scope");
        return BasicCredentialsProvider.matchCredentials(this.credMap, authScope);
    }

    @Override
    public void clear() {
        this.credMap.clear();
    }

    public String toString() {
        return this.credMap.toString();
    }
}

