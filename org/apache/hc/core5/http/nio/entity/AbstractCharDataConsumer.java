/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.util.Args;

public abstract class AbstractCharDataConsumer
implements AsyncDataConsumer {
    protected static final int DEF_BUF_SIZE = 8192;
    private static final ByteBuffer EMPTY_BIN = ByteBuffer.wrap(new byte[0]);
    private final CharBuffer charbuf;
    private final CharCodingConfig charCodingConfig;
    private volatile Charset charset;
    private volatile CharsetDecoder charsetDecoder;

    protected AbstractCharDataConsumer(int bufSize, CharCodingConfig charCodingConfig) {
        this.charbuf = CharBuffer.allocate(Args.positive(bufSize, "Buffer size"));
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
    }

    public AbstractCharDataConsumer() {
        this(8192, CharCodingConfig.DEFAULT);
    }

    protected abstract int capacityIncrement();

    protected abstract void data(CharBuffer var1, boolean var2) throws IOException;

    protected abstract void completed() throws IOException;

    protected final void setCharset(Charset charset) {
        this.charset = charset != null ? charset : this.charCodingConfig.getCharset();
        this.charsetDecoder = null;
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        capacityChannel.update(this.capacityIncrement());
    }

    private void checkResult(CoderResult result) throws IOException {
        if (result.isError()) {
            result.throwException();
        }
    }

    private void doDecode(boolean endOfStream) throws IOException {
        this.charbuf.flip();
        this.data(this.charbuf, endOfStream);
        this.charbuf.clear();
    }

    private CharsetDecoder getCharsetDecoder() {
        if (this.charsetDecoder == null) {
            Charset charset = this.charset;
            if (charset == null) {
                charset = this.charCodingConfig.getCharset();
            }
            if (charset == null) {
                charset = StandardCharsets.US_ASCII;
            }
            this.charsetDecoder = charset.newDecoder();
            if (this.charCodingConfig.getMalformedInputAction() != null) {
                this.charsetDecoder.onMalformedInput(this.charCodingConfig.getMalformedInputAction());
            }
            if (this.charCodingConfig.getUnmappableInputAction() != null) {
                this.charsetDecoder.onUnmappableCharacter(this.charCodingConfig.getUnmappableInputAction());
            }
        }
        return this.charsetDecoder;
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        CharsetDecoder charsetDecoder = this.getCharsetDecoder();
        while (src.hasRemaining()) {
            this.checkResult(charsetDecoder.decode(src, this.charbuf, false));
            this.doDecode(false);
        }
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        CharsetDecoder charsetDecoder = this.getCharsetDecoder();
        this.checkResult(charsetDecoder.decode(EMPTY_BIN, this.charbuf, true));
        this.doDecode(false);
        this.checkResult(charsetDecoder.flush(this.charbuf));
        this.doDecode(true);
        this.completed();
    }
}

