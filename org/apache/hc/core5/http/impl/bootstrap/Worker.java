/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;

class Worker
implements Runnable {
    private final HttpService httpservice;
    private final HttpServerConnection conn;
    private final ExceptionListener exceptionListener;

    Worker(HttpService httpservice, HttpServerConnection conn, ExceptionListener exceptionListener) {
        this.httpservice = httpservice;
        this.conn = conn;
        this.exceptionListener = exceptionListener;
    }

    public HttpServerConnection getConnection() {
        return this.conn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            BasicHttpContext localContext = new BasicHttpContext();
            HttpCoreContext context = HttpCoreContext.adapt(localContext);
            while (!Thread.interrupted() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
                localContext.clear();
            }
            this.conn.close();
        } catch (Exception ex) {
            this.exceptionListener.onError(this.conn, ex);
        } finally {
            this.conn.close(CloseMode.IMMEDIATE);
        }
    }
}

