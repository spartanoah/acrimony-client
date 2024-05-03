/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

public class DemuxOutputStream
extends OutputStream {
    private final InheritableThreadLocal<OutputStream> m_streams = new InheritableThreadLocal();

    public OutputStream bindStream(OutputStream output) {
        OutputStream stream = (OutputStream)this.m_streams.get();
        this.m_streams.set(output);
        return stream;
    }

    @Override
    public void close() throws IOException {
        OutputStream output = (OutputStream)this.m_streams.get();
        if (null != output) {
            output.close();
        }
    }

    @Override
    public void flush() throws IOException {
        OutputStream output = (OutputStream)this.m_streams.get();
        if (null != output) {
            output.flush();
        }
    }

    @Override
    public void write(int ch) throws IOException {
        OutputStream output = (OutputStream)this.m_streams.get();
        if (null != output) {
            output.write(ch);
        }
    }
}

