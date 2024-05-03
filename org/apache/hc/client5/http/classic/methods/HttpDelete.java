/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic.methods;

import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

public class HttpDelete
extends HttpUriRequestBase {
    private static final long serialVersionUID = 1L;
    public static final String METHOD_NAME = "DELETE";

    public HttpDelete(URI uri) {
        super(METHOD_NAME, uri);
    }

    public HttpDelete(String uri) {
        this(URI.create(uri));
    }
}

