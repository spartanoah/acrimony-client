/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client.protocol;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Deprecated
@NotThreadSafe
public class ClientContextConfigurer
implements ClientContext {
    private final HttpContext context;

    public ClientContextConfigurer(HttpContext context) {
        Args.notNull(context, "HTTP context");
        this.context = context;
    }

    public void setCookieSpecRegistry(CookieSpecRegistry registry) {
        this.context.setAttribute("http.cookiespec-registry", registry);
    }

    public void setAuthSchemeRegistry(AuthSchemeRegistry registry) {
        this.context.setAttribute("http.authscheme-registry", registry);
    }

    public void setCookieStore(CookieStore store) {
        this.context.setAttribute("http.cookie-store", store);
    }

    public void setCredentialsProvider(CredentialsProvider provider) {
        this.context.setAttribute("http.auth.credentials-provider", provider);
    }
}

