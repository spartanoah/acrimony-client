/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.TruncatedChunkException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.nio.AbstractContentDecoder;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class ChunkDecoder
extends AbstractContentDecoder {
    private State state = State.READ_CONTENT;
    private boolean endOfChunk = false;
    private boolean endOfStream = false;
    private CharArrayBuffer lineBuf;
    private long chunkSize = -1L;
    private long pos = 0L;
    private final Http1Config http1Config;
    private final List<CharArrayBuffer> trailerBufs;
    private final List<Header> trailers;

    public ChunkDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, Http1Config http1Config, BasicHttpTransportMetrics metrics) {
        super(channel, buffer, metrics);
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.trailerBufs = new ArrayList<CharArrayBuffer>();
        this.trailers = new ArrayList<Header>();
    }

    public ChunkDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, BasicHttpTransportMetrics metrics) {
        this(channel, buffer, null, metrics);
    }

    private void readChunkHead() throws IOException {
        if (this.lineBuf == null) {
            this.lineBuf = new CharArrayBuffer(32);
        } else {
            this.lineBuf.clear();
        }
        if (this.endOfChunk) {
            if (this.buffer.readLine(this.lineBuf, this.endOfStream)) {
                if (!this.lineBuf.isEmpty()) {
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                }
            } else {
                if (this.buffer.length() > 2 || this.endOfStream) {
                    throw new MalformedChunkCodingException("CRLF expected at end of chunk");
                }
                return;
            }
            this.endOfChunk = false;
        }
        boolean lineComplete = this.buffer.readLine(this.lineBuf, this.endOfStream);
        int maxLineLen = this.http1Config.getMaxLineLength();
        if (maxLineLen > 0 && (this.lineBuf.length() > maxLineLen || !lineComplete && this.buffer.length() > maxLineLen)) {
            throw new MessageConstraintException("Maximum line length limit exceeded");
        }
        if (lineComplete) {
            int separator = this.lineBuf.indexOf(59);
            if (separator < 0) {
                separator = this.lineBuf.length();
            }
            String s = this.lineBuf.substringTrimmed(0, separator);
            try {
                this.chunkSize = Long.parseLong(s, 16);
            } catch (NumberFormatException e) {
                throw new MalformedChunkCodingException("Bad chunk header: " + s);
            }
            this.pos = 0L;
        } else if (this.endOfStream) {
            throw new ConnectionClosedException("Premature end of chunk coded message body: closing chunk expected");
        }
    }

    private void parseHeader() throws IOException {
        CharArrayBuffer current = this.lineBuf;
        int count = this.trailerBufs.size();
        if ((this.lineBuf.charAt(0) == ' ' || this.lineBuf.charAt(0) == '\t') && count > 0) {
            char ch;
            int i;
            CharArrayBuffer previous = this.trailerBufs.get(count - 1);
            for (i = 0; i < current.length() && ((ch = current.charAt(i)) == ' ' || ch == '\t'); ++i) {
            }
            int maxLineLen = this.http1Config.getMaxLineLength();
            if (maxLineLen > 0 && previous.length() + 1 + current.length() - i > maxLineLen) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            previous.append(' ');
            previous.append(current, i, current.length() - i);
        } else {
            this.trailerBufs.add(current);
            this.lineBuf = null;
        }
    }

    private void processFooters() throws IOException {
        int count = this.trailerBufs.size();
        if (count > 0) {
            this.trailers.clear();
            for (int i = 0; i < this.trailerBufs.size(); ++i) {
                try {
                    this.trailers.add(new BufferedHeader(this.trailerBufs.get(i)));
                    continue;
                } catch (ParseException ex) {
                    throw new IOException(ex);
                }
            }
        }
        this.trailerBufs.clear();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        Args.notNull(dst, "Byte buffer");
        if (this.state == State.COMPLETED) {
            return -1;
        }
        int totalRead = 0;
        while (this.state != State.COMPLETED) {
            int bytesRead;
            if (!(this.buffer.hasData() && this.chunkSize != -1L || (bytesRead = this.fillBufferFromChannel()) != -1)) {
                this.endOfStream = true;
            }
            switch (this.state) {
                case READ_CONTENT: {
                    long maxLen;
                    int len;
                    if (this.chunkSize == -1L) {
                        this.readChunkHead();
                        if (this.chunkSize == -1L) {
                            return totalRead;
                        }
                        if (this.chunkSize == 0L) {
                            this.chunkSize = -1L;
                            this.state = State.READ_FOOTERS;
                            break;
                        }
                    }
                    if ((len = this.buffer.read(dst, (int)Math.min(maxLen = this.chunkSize - this.pos, Integer.MAX_VALUE))) > 0) {
                        this.pos += (long)len;
                        totalRead += len;
                    } else if (!this.buffer.hasData() && this.endOfStream) {
                        this.state = State.COMPLETED;
                        this.setCompleted();
                        throw new TruncatedChunkException("Truncated chunk (expected size: %d; actual size: %d)", this.chunkSize, this.pos);
                    }
                    if (this.pos == this.chunkSize) {
                        this.chunkSize = -1L;
                        this.pos = 0L;
                        this.endOfChunk = true;
                        break;
                    }
                    return totalRead;
                }
                case READ_FOOTERS: {
                    if (this.lineBuf == null) {
                        this.lineBuf = new CharArrayBuffer(32);
                    } else {
                        this.lineBuf.clear();
                    }
                    if (!this.buffer.readLine(this.lineBuf, this.endOfStream)) {
                        if (this.endOfStream) {
                            this.state = State.COMPLETED;
                            this.setCompleted();
                        }
                        return totalRead;
                    }
                    if (this.lineBuf.length() > 0) {
                        int maxHeaderCount = this.http1Config.getMaxHeaderCount();
                        if (maxHeaderCount > 0 && this.trailerBufs.size() >= maxHeaderCount) {
                            throw new MessageConstraintException("Maximum header count exceeded");
                        }
                        this.parseHeader();
                        break;
                    }
                    this.state = State.COMPLETED;
                    this.setCompleted();
                    this.processFooters();
                }
            }
        }
        return totalRead;
    }

    @Override
    public List<? extends Header> getTrailers() {
        return this.trailers.isEmpty() ? null : new ArrayList<Header>(this.trailers);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[chunk-coded; completed: ");
        sb.append(this.completed);
        sb.append("]");
        return sb.toString();
    }

    private static enum State {
        READ_CONTENT,
        READ_FOOTERS,
        COMPLETED;

    }
}

