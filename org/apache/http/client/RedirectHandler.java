/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client;

import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.protocol.HttpContext;

@Deprecated
public interface RedirectHandler {
    public boolean isRedirectRequested(HttpResponse var1, HttpContext var2);

    public URI getLocationURI(HttpResponse var1, HttpContext var2) throws ProtocolException;
}

