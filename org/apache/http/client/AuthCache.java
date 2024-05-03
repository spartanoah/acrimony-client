/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;

public interface AuthCache {
    public void put(HttpHost var1, AuthScheme var2);

    public AuthScheme get(HttpHost var1);

    public void remove(HttpHost var1);

    public void clear();
}

