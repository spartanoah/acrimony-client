/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.HeapResource;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class HeapResourceFactory
implements ResourceFactory {
    public static final HeapResourceFactory INSTANCE = new HeapResourceFactory();

    @Override
    public Resource generate(String requestId, byte[] content, int off, int len) {
        byte[] copy = new byte[len];
        System.arraycopy(content, off, copy, 0, len);
        return new HeapResource(copy);
    }

    @Override
    public Resource generate(String requestId, byte[] content) {
        return new HeapResource(content != null ? (byte[])content.clone() : null);
    }

    @Override
    public Resource copy(String requestId, Resource resource) throws ResourceIOException {
        Args.notNull(resource, "Resource");
        return new HeapResource(resource.get());
    }
}

