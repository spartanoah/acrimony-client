/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.message.HttpResponseWrapper;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.support.classic.ContentInputStream;
import org.apache.hc.core5.http.nio.support.classic.ContentOutputStream;
import org.apache.hc.core5.http.nio.support.classic.SharedInputBuffer;
import org.apache.hc.core5.http.nio.support.classic.SharedOutputBuffer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public abstract class AbstractClassicServerExchangeHandler
implements AsyncServerExchangeHandler {
    private final int initialBufferSize;
    private final Executor executor;
    private final AtomicReference<State> state;
    private final AtomicReference<Exception> exception;
    private volatile SharedInputBuffer inputBuffer;
    private volatile SharedOutputBuffer outputBuffer;

    public AbstractClassicServerExchangeHandler(int initialBufferSize, Executor executor) {
        this.initialBufferSize = Args.positive(initialBufferSize, "Initial buffer size");
        this.executor = Args.notNull(executor, "Executor");
        this.exception = new AtomicReference<Object>(null);
        this.state = new AtomicReference<State>(State.IDLE);
    }

    protected abstract void handle(HttpRequest var1, InputStream var2, HttpResponse var3, OutputStream var4, HttpContext var5) throws IOException, HttpException;

    public Exception getException() {
        return this.exception.get();
    }

    @Override
    public final void handleRequest(final HttpRequest request, EntityDetails entityDetails, final ResponseChannel responseChannel, final HttpContext context) throws HttpException, IOException {
        ContentInputStream inputStream;
        final AtomicBoolean responseCommitted = new AtomicBoolean(false);
        final BasicHttpResponse response = new BasicHttpResponse(200);
        final HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response){

            private void ensureNotCommitted() {
                Asserts.check(!responseCommitted.get(), "Response already committed");
            }

            @Override
            public void addHeader(String name, Object value) {
                this.ensureNotCommitted();
                super.addHeader(name, value);
            }

            @Override
            public void setHeader(String name, Object value) {
                this.ensureNotCommitted();
                super.setHeader(name, value);
            }

            @Override
            public void setVersion(ProtocolVersion version) {
                this.ensureNotCommitted();
                super.setVersion(version);
            }

            @Override
            public void setCode(int code) {
                this.ensureNotCommitted();
                super.setCode(code);
            }

            @Override
            public void setReasonPhrase(String reason) {
                this.ensureNotCommitted();
                super.setReasonPhrase(reason);
            }

            @Override
            public void setLocale(Locale locale) {
                this.ensureNotCommitted();
                super.setLocale(locale);
            }
        };
        if (entityDetails != null) {
            this.inputBuffer = new SharedInputBuffer(this.initialBufferSize);
            inputStream = new ContentInputStream(this.inputBuffer);
        } else {
            inputStream = null;
        }
        this.outputBuffer = new SharedOutputBuffer(this.initialBufferSize);
        final ContentOutputStream outputStream = new ContentOutputStream(this.outputBuffer){

            private void triggerResponse() throws IOException {
                try {
                    if (responseCommitted.compareAndSet(false, true)) {
                        responseChannel.sendResponse(response, new EntityDetails(){

                            @Override
                            public long getContentLength() {
                                return -1L;
                            }

                            @Override
                            public String getContentType() {
                                Header h = response.getFirstHeader("Content-Type");
                                return h != null ? h.getValue() : null;
                            }

                            @Override
                            public String getContentEncoding() {
                                Header h = response.getFirstHeader("Content-Encoding");
                                return h != null ? h.getValue() : null;
                            }

                            @Override
                            public boolean isChunked() {
                                return false;
                            }

                            @Override
                            public Set<String> getTrailerNames() {
                                return null;
                            }
                        }, context);
                    }
                } catch (HttpException ex) {
                    throw new IOException(ex.getMessage(), ex);
                }
            }

            @Override
            public void close() throws IOException {
                this.triggerResponse();
                super.close();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                this.triggerResponse();
                super.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.triggerResponse();
                super.write(b);
            }

            @Override
            public void write(int b) throws IOException {
                this.triggerResponse();
                super.write(b);
            }
        };
        if (this.state.compareAndSet(State.IDLE, State.ACTIVE)) {
            this.executor.execute(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        AbstractClassicServerExchangeHandler.this.handle(request, inputStream, responseWrapper, outputStream, context);
                        Closer.close(inputStream);
                        outputStream.close();
                    } catch (Exception ex) {
                        AbstractClassicServerExchangeHandler.this.exception.compareAndSet(null, ex);
                        if (AbstractClassicServerExchangeHandler.this.inputBuffer != null) {
                            AbstractClassicServerExchangeHandler.this.inputBuffer.abort();
                        }
                        AbstractClassicServerExchangeHandler.this.outputBuffer.abort();
                    } finally {
                        AbstractClassicServerExchangeHandler.this.state.set(State.COMPLETED);
                    }
                }
            });
        }
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        if (this.inputBuffer != null) {
            this.inputBuffer.updateCapacity(capacityChannel);
        }
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        Asserts.notNull(this.inputBuffer, "Input buffer");
        this.inputBuffer.fill(src);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        Asserts.notNull(this.inputBuffer, "Input buffer");
        this.inputBuffer.markEndStream();
    }

    @Override
    public final int available() {
        Asserts.notNull(this.outputBuffer, "Output buffer");
        return this.outputBuffer.length();
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        Asserts.notNull(this.outputBuffer, "Output buffer");
        this.outputBuffer.flush(channel);
    }

    @Override
    public final void failed(Exception cause) {
        this.exception.compareAndSet(null, cause);
        this.releaseResources();
    }

    @Override
    public void releaseResources() {
    }

    private static enum State {
        IDLE,
        ACTIVE,
        COMPLETED;

    }
}

