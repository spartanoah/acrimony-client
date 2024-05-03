/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.io;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.common.io.Closer;
import com.google.common.io.OutputSupplier;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public abstract class ByteSink
implements OutputSupplier<OutputStream> {
    protected ByteSink() {
    }

    public CharSink asCharSink(Charset charset) {
        return new AsCharSink(charset);
    }

    public abstract OutputStream openStream() throws IOException;

    @Override
    @Deprecated
    public final OutputStream getOutput() throws IOException {
        return this.openStream();
    }

    public OutputStream openBufferedStream() throws IOException {
        OutputStream out = this.openStream();
        return out instanceof BufferedOutputStream ? (BufferedOutputStream)out : new BufferedOutputStream(out);
    }

    public void write(byte[] bytes) throws IOException {
        Preconditions.checkNotNull(bytes);
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(this.openStream());
            out.write(bytes);
            out.flush();
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public long writeFrom(InputStream input) throws IOException {
        Preconditions.checkNotNull(input);
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(this.openStream());
            long written = ByteStreams.copy(input, out);
            out.flush();
            long l = written;
            return l;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private final class AsCharSink
    extends CharSink {
        private final Charset charset;

        private AsCharSink(Charset charset) {
            this.charset = Preconditions.checkNotNull(charset);
        }

        @Override
        public Writer openStream() throws IOException {
            return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
        }

        public String toString() {
            return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
        }
    }
}

