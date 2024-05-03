/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.fastutil.io;

import java.io.IOException;

public interface MeasurableStream {
    public long length() throws IOException;

    public long position() throws IOException;
}

