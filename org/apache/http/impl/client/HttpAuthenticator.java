/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import org.apache.commons.logging.Log;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthState;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class HttpAuthenticator
extends org.apache.http.impl.auth.HttpAuthenticator {
    public HttpAuthenticator(Log log) {
        super(log);
    }

    public HttpAuthenticator() {
    }

    public boolean authenticate(HttpHost host, HttpResponse response, AuthenticationStrategy authStrategy, AuthState authState, HttpContext context) {
        return this.handleAuthChallenge(host, response, authStrategy, authState, context);
    }
}

