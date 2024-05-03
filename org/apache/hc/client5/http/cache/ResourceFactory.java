/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ResourceFactory {
    public Resource generate(String var1, byte[] var2) throws ResourceIOException;

    public Resource generate(String var1, byte[] var2, int var3, int var4) throws ResourceIOException;

    public Resource copy(String var1, Resource var2) throws ResourceIOException;
}

