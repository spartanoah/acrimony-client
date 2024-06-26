/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.auth;

import java.nio.charset.Charset;
import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@Immutable
public class DigestSchemeFactory
implements AuthSchemeFactory,
AuthSchemeProvider {
    private final Charset charset;

    public DigestSchemeFactory(Charset charset) {
        this.charset = charset;
    }

    public DigestSchemeFactory() {
        this(null);
    }

    public AuthScheme newInstance(HttpParams params) {
        return new DigestScheme();
    }

    public AuthScheme create(HttpContext context) {
        return new DigestScheme(this.charset);
    }
}

