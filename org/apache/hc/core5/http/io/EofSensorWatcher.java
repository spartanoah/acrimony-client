/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.InputStream;

public interface EofSensorWatcher {
    public boolean eofDetected(InputStream var1) throws IOException;

    public boolean streamClosed(InputStream var1) throws IOException;

    public boolean streamAbort(InputStream var1) throws IOException;
}

