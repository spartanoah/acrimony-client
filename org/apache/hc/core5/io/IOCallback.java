/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.io;

import java.io.IOException;

public interface IOCallback<T> {
    public void execute(T var1) throws IOException;
}

