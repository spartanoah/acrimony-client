/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Immutable;
import org.apache.http.protocol.HttpContext;

@Immutable
public class NoConnectionReuseStrategy
implements ConnectionReuseStrategy {
    public static final NoConnectionReuseStrategy INSTANCE = new NoConnectionReuseStrategy();

    public boolean keepAlive(HttpResponse response, HttpContext context) {
        return false;
    }
}

