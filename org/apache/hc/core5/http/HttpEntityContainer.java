/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.HttpEntity;

public interface HttpEntityContainer {
    public HttpEntity getEntity();

    public void setEntity(HttpEntity var1);
}

