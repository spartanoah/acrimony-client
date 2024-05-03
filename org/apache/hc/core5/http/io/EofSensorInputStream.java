/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.io.EofSensorWatcher;
import org.apache.hc.core5.util.Args;

public class EofSensorInputStream
extends InputStream {
    private InputStream wrappedStream;
    private boolean selfClosed;
    private final EofSensorWatcher eofWatcher;

    public EofSensorInputStream(InputStream in, EofSensorWatcher watcher) {
        Args.notNull(in, "Wrapped stream");
        this.wrappedStream = in;
        this.selfClosed = false;
        this.eofWatcher = watcher;
    }

    boolean isSelfClosed() {
        return this.selfClosed;
    }

    InputStream getWrappedStream() {
        return this.wrappedStream;
    }

    private boolean isReadAllowed() throws IOException {
        if (this.selfClosed) {
            throw new IOException("Attempted read on closed stream.");
        }
        return this.wrappedStream != null;
    }

    @Override
    public int read() throws IOException {
        int b = -1;
        if (this.isReadAllowed()) {
            try {
                b = this.wrappedStream.read();
                this.checkEOF(b);
            } catch (IOException ex) {
                this.checkAbort();
                throw ex;
            }
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = -1;
        if (this.isReadAllowed()) {
            try {
                readLen = this.wrappedStream.read(b, off, len);
                this.checkEOF(readLen);
            } catch (IOException ex) {
                this.checkAbort();
                throw ex;
            }
        }
        return readLen;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int available() throws IOException {
        int a = 0;
        if (this.isReadAllowed()) {
            try {
                a = this.wrappedStream.available();
            } catch (IOException ex) {
                this.checkAbort();
                throw ex;
            }
        }
        return a;
    }

    @Override
    public void close() throws IOException {
        this.selfClosed = true;
        this.checkClose();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkEOF(int eof) throws IOException {
        InputStream toCheckStream = this.wrappedStream;
        if (toCheckStream != null && eof < 0) {
            try {
                boolean scws = true;
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.eofDetected(toCheckStream);
                }
                if (scws) {
                    toCheckStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkClose() throws IOException {
        InputStream toCloseStream = this.wrappedStream;
        if (toCloseStream != null) {
            try {
                boolean scws = true;
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.streamClosed(toCloseStream);
                }
                if (scws) {
                    toCloseStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkAbort() throws IOException {
        InputStream toAbortStream = this.wrappedStream;
        if (toAbortStream != null) {
            try {
                boolean scws = true;
                if (this.eofWatcher != null) {
                    scws = this.eofWatcher.streamAbort(toAbortStream);
                }
                if (scws) {
                    toAbortStream.close();
                }
            } finally {
                this.wrappedStream = null;
            }
        }
    }

    public void abort() throws IOException {
        this.selfClosed = true;
        this.checkAbort();
    }
}

