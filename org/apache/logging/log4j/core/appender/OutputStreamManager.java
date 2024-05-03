/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.ByteBufferDestinationHelper;
import org.apache.logging.log4j.core.util.Constants;

public class OutputStreamManager
extends AbstractManager
implements ByteBufferDestination {
    protected final Layout<?> layout;
    protected ByteBuffer byteBuffer;
    private volatile OutputStream outputStream;
    private boolean skipFooter;

    protected OutputStreamManager(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader) {
        this(os, streamName, layout, writeHeader, Constants.ENCODER_BYTE_BUFFER_SIZE);
    }

    protected OutputStreamManager(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader, int bufferSize) {
        this(os, streamName, layout, writeHeader, ByteBuffer.wrap(new byte[bufferSize]));
    }

    @Deprecated
    protected OutputStreamManager(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader, ByteBuffer byteBuffer) {
        super(null, streamName);
        this.outputStream = os;
        this.layout = layout;
        if (writeHeader) {
            this.writeHeader(os);
        }
        this.byteBuffer = Objects.requireNonNull(byteBuffer, "byteBuffer");
    }

    protected OutputStreamManager(LoggerContext loggerContext, OutputStream os, String streamName, boolean createOnDemand, Layout<? extends Serializable> layout, boolean writeHeader, ByteBuffer byteBuffer) {
        super(loggerContext, streamName);
        if (createOnDemand && os != null) {
            LOGGER.error("Invalid OutputStreamManager configuration for '{}': You cannot both set the OutputStream and request on-demand.", (Object)streamName);
        }
        this.layout = layout;
        this.byteBuffer = Objects.requireNonNull(byteBuffer, "byteBuffer");
        this.outputStream = os;
        if (writeHeader) {
            this.writeHeader(os);
        }
    }

    public static <T> OutputStreamManager getManager(String name, T data, ManagerFactory<? extends OutputStreamManager, T> factory) {
        return AbstractManager.getManager(name, factory, data);
    }

    protected OutputStream createOutputStream() throws IOException {
        throw new IllegalStateException(this.getClass().getCanonicalName() + " must implement createOutputStream()");
    }

    public void skipFooter(boolean skipFooter) {
        this.skipFooter = skipFooter;
    }

    @Override
    public boolean releaseSub(long timeout, TimeUnit timeUnit) {
        this.writeFooter();
        return this.closeOutputStream();
    }

    protected void writeHeader(OutputStream os) {
        byte[] header;
        if (this.layout != null && os != null && (header = this.layout.getHeader()) != null) {
            try {
                os.write(header, 0, header.length);
            } catch (IOException e) {
                this.logError("Unable to write header", e);
            }
        }
    }

    protected void writeFooter() {
        if (this.layout == null || this.skipFooter) {
            return;
        }
        byte[] footer = this.layout.getFooter();
        if (footer != null) {
            this.write(footer);
        }
    }

    public boolean isOpen() {
        return this.getCount() > 0;
    }

    public boolean hasOutputStream() {
        return this.outputStream != null;
    }

    protected OutputStream getOutputStream() throws IOException {
        if (this.outputStream == null) {
            this.outputStream = this.createOutputStream();
        }
        return this.outputStream;
    }

    protected void setOutputStream(OutputStream os) {
        this.outputStream = os;
    }

    protected void write(byte[] bytes) {
        this.write(bytes, 0, bytes.length, false);
    }

    protected void write(byte[] bytes, boolean immediateFlush) {
        this.write(bytes, 0, bytes.length, immediateFlush);
    }

    @Override
    public void writeBytes(byte[] data, int offset, int length) {
        this.write(data, offset, length, false);
    }

    protected void write(byte[] bytes, int offset, int length) {
        this.writeBytes(bytes, offset, length);
    }

    protected synchronized void write(byte[] bytes, int offset, int length, boolean immediateFlush) {
        if (immediateFlush && this.byteBuffer.position() == 0) {
            this.writeToDestination(bytes, offset, length);
            this.flushDestination();
            return;
        }
        if (length >= this.byteBuffer.capacity()) {
            this.flush();
            this.writeToDestination(bytes, offset, length);
        } else {
            if (length > this.byteBuffer.remaining()) {
                this.flush();
            }
            this.byteBuffer.put(bytes, offset, length);
        }
        if (immediateFlush) {
            this.flush();
        }
    }

    protected synchronized void writeToDestination(byte[] bytes, int offset, int length) {
        try {
            this.getOutputStream().write(bytes, offset, length);
        } catch (IOException ex) {
            throw new AppenderLoggingException("Error writing to stream " + this.getName(), ex);
        }
    }

    protected synchronized void flushDestination() {
        OutputStream stream = this.outputStream;
        if (stream != null) {
            try {
                stream.flush();
            } catch (IOException ex) {
                throw new AppenderLoggingException("Error flushing stream " + this.getName(), ex);
            }
        }
    }

    protected synchronized void flushBuffer(ByteBuffer buf) {
        buf.flip();
        try {
            if (buf.remaining() > 0) {
                this.writeToDestination(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
            }
        } finally {
            buf.clear();
        }
    }

    public synchronized void flush() {
        this.flushBuffer(this.byteBuffer);
        this.flushDestination();
    }

    protected synchronized boolean closeOutputStream() {
        this.flush();
        OutputStream stream = this.outputStream;
        if (stream == null || stream == System.out || stream == System.err) {
            return true;
        }
        try {
            stream.close();
            LOGGER.debug("OutputStream closed");
        } catch (IOException ex) {
            this.logError("Unable to close stream", ex);
            return false;
        }
        return true;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    @Override
    public ByteBuffer drain(ByteBuffer buf) {
        this.flushBuffer(buf);
        return buf;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeBytes(ByteBuffer data) {
        if (data.remaining() == 0) {
            return;
        }
        OutputStreamManager outputStreamManager = this;
        synchronized (outputStreamManager) {
            ByteBufferDestinationHelper.writeToUnsynchronized(data, this);
        }
    }
}

