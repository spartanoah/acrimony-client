/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import io.netty.util.ReferenceCounted;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public interface FileRegion
extends ReferenceCounted {
    public long position();

    public long transfered();

    public long count();

    public long transferTo(WritableByteChannel var1, long var2) throws IOException;
}

