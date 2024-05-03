/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class HeapResource
extends Resource {
    private static final long serialVersionUID = -2078599905620463394L;
    private final AtomicReference<byte[]> arrayRef;

    public HeapResource(byte[] b) {
        this.arrayRef = new AtomicReference<byte[]>(b);
    }

    @Override
    public byte[] get() throws ResourceIOException {
        byte[] byteArray = this.arrayRef.get();
        if (byteArray != null) {
            return byteArray;
        }
        throw new ResourceIOException("Resouce already dispoased");
    }

    @Override
    public long length() {
        byte[] byteArray = this.arrayRef.get();
        if (byteArray != null) {
            return byteArray.length;
        }
        return -1L;
    }

    @Override
    public void dispose() {
        this.arrayRef.set(null);
    }
}

