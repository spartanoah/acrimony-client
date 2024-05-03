/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.fastutil.io;

import java.io.IOException;

public interface RepositionableStream {
    public void position(long var1) throws IOException;

    public long position() throws IOException;
}

