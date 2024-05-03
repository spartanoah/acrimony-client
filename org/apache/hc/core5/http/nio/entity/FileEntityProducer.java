/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public final class FileEntityProducer
implements AsyncEntityProducer {
    private final File file;
    private final ByteBuffer byteBuffer;
    private final long length;
    private final ContentType contentType;
    private final boolean chunked;
    private final AtomicReference<Exception> exception;
    private final AtomicReference<RandomAccessFile> accessFileRef;
    private boolean eof;

    public FileEntityProducer(File file, int bufferSize, ContentType contentType, boolean chunked) {
        this.file = Args.notNull(file, "File");
        this.length = file.length();
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.contentType = contentType;
        this.chunked = chunked;
        this.accessFileRef = new AtomicReference<Object>(null);
        this.exception = new AtomicReference<Object>(null);
    }

    public FileEntityProducer(File file, ContentType contentType, boolean chunked) {
        this(file, 8192, contentType, chunked);
    }

    public FileEntityProducer(File file, ContentType contentType) {
        this(file, contentType, false);
    }

    public FileEntityProducer(File file) {
        this(file, ContentType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public long getContentLength() {
        return this.length;
    }

    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return this.chunked;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public void produce(DataStreamChannel channel) throws IOException {
        int bytesRead;
        RandomAccessFile accessFile = this.accessFileRef.get();
        if (accessFile == null) {
            accessFile = new RandomAccessFile(this.file, "r");
            Asserts.check(this.accessFileRef.getAndSet(accessFile) == null, "Illegal producer state");
        }
        if (!this.eof && (bytesRead = accessFile.getChannel().read(this.byteBuffer)) < 0) {
            this.eof = true;
        }
        if (this.byteBuffer.position() > 0) {
            this.byteBuffer.flip();
            channel.write(this.byteBuffer);
            this.byteBuffer.compact();
        }
        if (this.eof && this.byteBuffer.position() == 0) {
            channel.endStream();
            this.releaseResources();
        }
    }

    @Override
    public void failed(Exception cause) {
        if (this.exception.compareAndSet(null, cause)) {
            this.releaseResources();
        }
    }

    public Exception getException() {
        return this.exception.get();
    }

    @Override
    public void releaseResources() {
        this.eof = false;
        Closer.closeQuietly(this.accessFileRef.getAndSet(null));
    }
}

