/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.impl.io.ChunkedInputStream;
import org.apache.hc.core5.http.io.EofSensorInputStream;
import org.apache.hc.core5.http.io.EofSensorWatcher;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;

class ResponseEntityProxy
extends HttpEntityWrapper
implements EofSensorWatcher {
    private final ExecRuntime execRuntime;

    public static void enhance(ClassicHttpResponse response, ExecRuntime execRuntime) {
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming() && execRuntime != null) {
            response.setEntity(new ResponseEntityProxy(entity, execRuntime));
        }
    }

    ResponseEntityProxy(HttpEntity entity, ExecRuntime execRuntime) {
        super(entity);
        this.execRuntime = execRuntime;
    }

    private void cleanup() throws IOException {
        if (this.execRuntime != null) {
            if (this.execRuntime.isEndpointConnected()) {
                this.execRuntime.disconnectEndpoint();
            }
            this.execRuntime.discardEndpoint();
        }
    }

    private void discardConnection() {
        if (this.execRuntime != null) {
            this.execRuntime.discardEndpoint();
        }
    }

    public void releaseConnection() {
        if (this.execRuntime != null) {
            this.execRuntime.releaseEndpoint();
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(super.getContent(), this);
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        try {
            if (outStream != null) {
                super.writeTo(outStream);
            }
            this.releaseConnection();
        } catch (IOException | RuntimeException ex) {
            this.discardConnection();
            throw ex;
        } finally {
            this.cleanup();
        }
    }

    @Override
    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            if (wrapped != null) {
                wrapped.close();
            }
            this.releaseConnection();
        } catch (IOException | RuntimeException ex) {
            this.discardConnection();
            throw ex;
        } finally {
            this.cleanup();
        }
        return false;
    }

    @Override
    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            boolean open = this.execRuntime != null && this.execRuntime.isEndpointAcquired();
            try {
                if (wrapped != null) {
                    wrapped.close();
                }
                this.releaseConnection();
            } catch (SocketException ex) {
                if (open) {
                    throw ex;
                }
            }
        } catch (IOException | RuntimeException ex) {
            this.discardConnection();
            throw ex;
        } finally {
            this.cleanup();
        }
        return false;
    }

    @Override
    public boolean streamAbort(InputStream wrapped) throws IOException {
        this.cleanup();
        return false;
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        try {
            final InputStream underlyingStream = super.getContent();
            return new Supplier<List<? extends Header>>(){

                @Override
                public List<? extends Header> get() {
                    Header[] footers;
                    if (underlyingStream instanceof ChunkedInputStream) {
                        ChunkedInputStream chunkedInputStream = (ChunkedInputStream)underlyingStream;
                        footers = chunkedInputStream.getFooters();
                    } else {
                        footers = new Header[]{};
                    }
                    return Arrays.asList(footers);
                }
            };
        } catch (IOException e) {
            throw new IllegalStateException("Unable to retrieve input stream", e);
        }
    }
}

