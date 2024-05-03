/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.HttpResponse;

public interface HttpResponseFactory<T extends HttpResponse> {
    public T newHttpResponse(int var1, String var2);

    public T newHttpResponse(int var1);
}

