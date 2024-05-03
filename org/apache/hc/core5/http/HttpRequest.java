/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.net.URIAuthority;

public interface HttpRequest
extends HttpMessage {
    public String getMethod();

    public String getPath();

    public void setPath(String var1);

    public String getScheme();

    public void setScheme(String var1);

    public URIAuthority getAuthority();

    public void setAuthority(URIAuthority var1);

    public String getRequestUri();

    public URI getUri() throws URISyntaxException;

    public void setUri(URI var1);
}

