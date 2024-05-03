/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache.memcached;

import org.apache.hc.client5.http.cache.ResourceIOException;

public class MemcachedOperationTimeoutException
extends ResourceIOException {
    private static final long serialVersionUID = 1L;

    public MemcachedOperationTimeoutException(Throwable cause) {
        super(cause != null ? cause.getMessage() : null, cause);
    }
}

