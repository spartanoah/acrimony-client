/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.nio.charset.Charset;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class BasicSchemeFactory
implements AuthSchemeFactory {
    public static final BasicSchemeFactory INSTANCE = new BasicSchemeFactory();
    private final Charset charset;

    public BasicSchemeFactory(Charset charset) {
        this.charset = charset;
    }

    public BasicSchemeFactory() {
        this(null);
    }

    @Override
    public AuthScheme create(HttpContext context) {
        return new BasicScheme(this.charset);
    }
}

