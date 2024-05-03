/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;

@Internal
public interface IOEventHandler {
    public void connected(IOSession var1) throws IOException;

    public void inputReady(IOSession var1, ByteBuffer var2) throws IOException;

    public void outputReady(IOSession var1) throws IOException;

    public void timeout(IOSession var1, Timeout var2) throws IOException;

    public void exception(IOSession var1, Exception var2);

    public void disconnected(IOSession var1);
}

