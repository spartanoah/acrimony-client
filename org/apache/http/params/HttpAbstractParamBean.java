/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.params;

import org.apache.http.params.HttpParams;
import org.apache.http.util.Args;

@Deprecated
public abstract class HttpAbstractParamBean {
    protected final HttpParams params;

    public HttpAbstractParamBean(HttpParams params) {
        this.params = Args.notNull(params, "HTTP parameters");
    }
}

