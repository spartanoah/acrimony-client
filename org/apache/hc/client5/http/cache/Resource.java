/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class Resource
implements Serializable {
    private static final long serialVersionUID = 1L;

    public InputStream getInputStream() throws ResourceIOException {
        return new ByteArrayInputStream(this.get());
    }

    public abstract byte[] get() throws ResourceIOException;

    public abstract long length();

    public abstract void dispose();
}

