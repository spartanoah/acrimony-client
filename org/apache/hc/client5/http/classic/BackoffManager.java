/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic;

import org.apache.hc.client5.http.HttpRoute;

public interface BackoffManager {
    public void backOff(HttpRoute var1);

    public void probe(HttpRoute var1);
}

