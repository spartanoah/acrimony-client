/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.security.Principal;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.MalformedChallengeException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AuthScheme {
    public String getName();

    public boolean isConnectionBased();

    public void processChallenge(AuthChallenge var1, HttpContext var2) throws MalformedChallengeException;

    public boolean isChallengeComplete();

    public String getRealm();

    public boolean isResponseReady(HttpHost var1, CredentialsProvider var2, HttpContext var3) throws AuthenticationException;

    public Principal getPrincipal();

    public String generateAuthResponse(HttpHost var1, HttpRequest var2, HttpContext var3) throws AuthenticationException;
}

