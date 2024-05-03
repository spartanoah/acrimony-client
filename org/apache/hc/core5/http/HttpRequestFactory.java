/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.net.URI;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.MethodNotSupportedException;

public interface HttpRequestFactory<T extends HttpRequest> {
    public T newHttpRequest(String var1, String var2) throws MethodNotSupportedException;

    public T newHttpRequest(String var1, URI var2) throws MethodNotSupportedException;
}

