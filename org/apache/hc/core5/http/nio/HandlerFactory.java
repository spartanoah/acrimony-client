/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.ResourceHolder;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface HandlerFactory<T extends ResourceHolder> {
    public T create(HttpRequest var1, HttpContext var2) throws HttpException;
}

