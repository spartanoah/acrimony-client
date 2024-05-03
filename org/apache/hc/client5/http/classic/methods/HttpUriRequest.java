/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic.methods;

import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.core5.http.ClassicHttpRequest;

public interface HttpUriRequest
extends ClassicHttpRequest,
Configurable {
    public void abort() throws UnsupportedOperationException;

    public boolean isAborted();
}

