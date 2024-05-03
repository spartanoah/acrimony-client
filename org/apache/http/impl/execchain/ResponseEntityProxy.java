/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.execchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.conn.EofSensorWatcher;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.execchain.ConnectionHolder;

class ResponseEntityProxy
extends HttpEntityWrapper
implements EofSensorWatcher {
    private final ConnectionHolder connHolder;

    public static void enchance(HttpResponse response, ConnectionHolder connHolder) {
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming() && connHolder != null) {
            response.setEntity(new ResponseEntityProxy(entity, connHolder));
        }
    }

    ResponseEntityProxy(HttpEntity entity, ConnectionHolder connHolder) {
        super(entity);
        this.connHolder = connHolder;
    }

    private void cleanup() throws IOException {
        if (this.connHolder != null) {
            this.connHolder.close();
        }
    }

    private void abortConnection() {
        if (this.connHolder != null) {
            this.connHolder.abortConnection();
        }
    }

    public void releaseConnection() {
        if (this.connHolder != null) {
            this.connHolder.releaseConnection();
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new EofSensorInputStream(this.wrappedEntity.getContent(), this);
    }

    @Override
    public void consumeContent() throws IOException {
        this.releaseConnection();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        try {
            if (outStream != null) {
                this.wrappedEntity.writeTo(outStream);
            }
            this.releaseConnection();
        } catch (IOException ex) {
            this.abortConnection();
            throw ex;
        } catch (RuntimeException ex) {
            this.abortConnection();
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
        } catch (IOException ex) {
            this.abortConnection();
            throw ex;
        } catch (RuntimeException ex) {
            this.abortConnection();
            throw ex;
        } finally {
            this.cleanup();
        }
        return false;
    }

    @Override
    public boolean streamClosed(InputStream wrapped) throws IOException {
        try {
            boolean open = this.connHolder != null && !this.connHolder.isReleased();
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
        } catch (IOException ex) {
            this.abortConnection();
            throw ex;
        } catch (RuntimeException ex) {
            this.abortConnection();
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

    public String toString() {
        StringBuilder sb = new StringBuilder("ResponseEntityProxy{");
        sb.append(this.wrappedEntity);
        sb.append('}');
        return sb.toString();
    }
}

