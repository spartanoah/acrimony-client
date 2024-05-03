/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class WriterOutputStream
extends OutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private final Writer writer;
    private final CharsetDecoder decoder;
    private final boolean writeImmediately;
    private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
    private final CharBuffer decoderOut;

    public WriterOutputStream(Writer writer, CharsetDecoder decoder) {
        this(writer, decoder, 1024, false);
    }

    public WriterOutputStream(Writer writer, CharsetDecoder decoder, int bufferSize, boolean writeImmediately) {
        this.writer = writer;
        this.decoder = decoder;
        this.writeImmediately = writeImmediately;
        this.decoderOut = CharBuffer.allocate(bufferSize);
    }

    public WriterOutputStream(Writer writer, Charset charset, int bufferSize, boolean writeImmediately) {
        this(writer, charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?"), bufferSize, writeImmediately);
    }

    public WriterOutputStream(Writer writer, Charset charset) {
        this(writer, charset, 1024, false);
    }

    public WriterOutputStream(Writer writer, String charsetName, int bufferSize, boolean writeImmediately) {
        this(writer, Charset.forName(charsetName), bufferSize, writeImmediately);
    }

    public WriterOutputStream(Writer writer, String charsetName) {
        this(writer, charsetName, 1024, false);
    }

    public WriterOutputStream(Writer writer) {
        this(writer, Charset.defaultCharset(), 1024, false);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int c = Math.min(len, this.decoderIn.remaining());
            this.decoderIn.put(b, off, c);
            this.processInput(false);
            len -= c;
            off += c;
        }
        if (this.writeImmediately) {
            this.flushOutput();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte)b}, 0, 1);
    }

    @Override
    public void flush() throws IOException {
        this.flushOutput();
        this.writer.flush();
    }

    @Override
    public void close() throws IOException {
        this.processInput(true);
        this.flushOutput();
        this.writer.close();
    }

    private void processInput(boolean endOfInput) throws IOException {
        CoderResult coderResult;
        this.decoderIn.flip();
        while ((coderResult = this.decoder.decode(this.decoderIn, this.decoderOut, endOfInput)).isOverflow()) {
            this.flushOutput();
        }
        if (!coderResult.isUnderflow()) {
            throw new IOException("Unexpected coder result");
        }
        this.decoderIn.compact();
    }

    private void flushOutput() throws IOException {
        if (this.decoderOut.position() > 0) {
            this.writer.write(this.decoderOut.array(), 0, this.decoderOut.position());
            this.decoderOut.rewind();
        }
    }
}

