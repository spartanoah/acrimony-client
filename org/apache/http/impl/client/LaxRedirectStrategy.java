/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import org.apache.http.annotation.Immutable;
import org.apache.http.impl.client.DefaultRedirectStrategy;

@Immutable
public class LaxRedirectStrategy
extends DefaultRedirectStrategy {
    private static final String[] REDIRECT_METHODS = new String[]{"GET", "POST", "HEAD"};

    protected boolean isRedirectable(String method) {
        for (String m : REDIRECT_METHODS) {
            if (!m.equalsIgnoreCase(method)) continue;
            return true;
        }
        return false;
    }
}

