/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.StreamClosedException;
import org.apache.hc.core5.http.TruncatedChunkException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class ChunkedInputStream
extends InputStream {
    private static final int BUFFER_SIZE = 2048;
    private static final Header[] EMPTY_FOOTERS = new Header[0];
    private final SessionInputBuffer buffer;
    private final InputStream inputStream;
    private final CharArrayBuffer lineBuffer;
    private final Http1Config http1Config;
    private State state;
    private long chunkSize;
    private long pos;
    private boolean eof = false;
    private boolean closed = false;
    private Header[] footers = EMPTY_FOOTERS;

    public ChunkedInputStream(SessionInputBuffer buffer, InputStream inputStream, Http1Config http1Config) {
        this.buffer = Args.notNull(buffer, "Session input buffer");
        this.inputStream = Args.notNull(inputStream, "Input stream");
        this.pos = 0L;
        this.lineBuffer = new CharArrayBuffer(16);
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.state = State.CHUNK_LEN;
    }

    public ChunkedInputStream(SessionInputBuffer buffer, InputStream inputStream) {
        this(buffer, inputStream, null);
    }

    @Override
    public int available() throws IOException {
        int len = this.buffer.length();
        return (int)Math.min((long)len, this.chunkSize - this.pos);
    }

    @Override
    public int read() throws IOException {
        int b;
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.eof) {
            return -1;
        }
        if (this.state != State.CHUNK_DATA) {
            this.nextChunk();
            if (this.eof) {
                return -1;
            }
        }
        if ((b = this.buffer.read(this.inputStream)) != -1) {
            ++this.pos;
            if (this.pos >= this.chunkSize) {
                this.state = State.CHUNK_CRLF;
            }
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead;
        if (this.closed) {
            throw new StreamClosedException();
        }
        if (this.eof) {
            return -1;
        }
        if (this.state != State.CHUNK_DATA) {
            this.nextChunk();
            if (this.eof) {
                return -1;
            }
        }
        if ((bytesRead = this.buffer.read(b, off, (int)Math.min((long)len, this.chunkSize - this.pos), this.inputStream)) != -1) {
            this.pos += (long)bytesRead;
            if (this.pos >= this.chunkSize) {
                this.state = State.CHUNK_CRLF;
            }
            return bytesRead;
        }
        this.eof = true;
        throw new TruncatedChunkException("Truncated chunk (expected size: %d; actual size: %d)", this.chunkSize, this.pos);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    private void nextChunk() throws IOException {
        if (this.state == State.CHUNK_INVALID) {
            throw new MalformedChunkCodingException("Corrupt data stream");
        }
        try {
            this.chunkSize = this.getChunkSize();
            if (this.chunkSize < 0L) {
                throw new MalformedChunkCodingException("Negative chunk size");
            }
            this.state = State.CHUNK_DATA;
            this.pos = 0L;
            if (this.chunkSize == 0L) {
                this.eof = true;
                this.parseTrailerHeaders();
            }
        } catch (MalformedChunkCodingException ex) {
            this.state = State.CHUNK_INVALID;
            throw ex;
        }
    }

    private long getChunkSize() throws IOException {
        State st = this.state;
        switch (st) {
            case CHUNK_CRLF: {
                this.lineBuffer.clear();
                int bytesRead1 = this.buffer.readLine(this.lineBuffer, this.inputStream);
                if (bytesRead1 == -1) {
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                }
                if (!this.lineBuffer.isEmpty()) {
                    throw new MalformedChunkCodingException("Unexpected content at the end of chunk");
                }
                this.state = State.CHUNK_LEN;
            }
            case CHUNK_LEN: {
                this.lineBuffer.clear();
                int bytesRead2 = this.buffer.readLine(this.lineBuffer, this.inputStream);
                if (bytesRead2 == -1) {
                    throw new ConnectionClosedException("Premature end of chunk coded message body: closing chunk expected");
                }
                int separator = this.lineBuffer.indexOf(59);
                if (separator < 0) {
                    separator = this.lineBuffer.length();
                }
                String s = this.lineBuffer.substringTrimmed(0, separator);
                try {
                    return Long.parseLong(s, 16);
                } catch (NumberFormatException e) {
                    throw new MalformedChunkCodingException("Bad chunk header: " + s);
                }
            }
        }
        throw new IllegalStateException("Inconsistent codec state");
    }

    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParser.parseHeaders(this.buffer, this.inputStream, this.http1Config.getMaxHeaderCount(), this.http1Config.getMaxLineLength(), null);
        } catch (HttpException ex) {
            MalformedChunkCodingException ioe = new MalformedChunkCodingException("Invalid trailing header: " + ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            try {
                if (!this.eof && this.state != State.CHUNK_INVALID) {
                    if (this.chunkSize == this.pos && this.chunkSize > 0L && this.read() == -1) {
                        return;
                    }
                    byte[] buff = new byte[2048];
                    while (this.read(buff) >= 0) {
                    }
                }
            } finally {
                this.eof = true;
                this.closed = true;
            }
        }
    }

    public Header[] getFooters() {
        return this.footers.length > 0 ? (Header[])this.footers.clone() : EMPTY_FOOTERS;
    }

    private static enum State {
        CHUNK_LEN,
        CHUNK_DATA,
        CHUNK_CRLF,
        CHUNK_INVALID;

    }
}

