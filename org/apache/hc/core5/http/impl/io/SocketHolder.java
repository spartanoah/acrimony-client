/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.util.Args;

public class SocketHolder {
    private final Socket socket;
    private final AtomicReference<InputStream> inputStreamRef;
    private final AtomicReference<OutputStream> outputStreamRef;

    public SocketHolder(Socket socket) {
        this.socket = Args.notNull(socket, "Socket");
        this.inputStreamRef = new AtomicReference<Object>(null);
        this.outputStreamRef = new AtomicReference<Object>(null);
    }

    public final Socket getSocket() {
        return this.socket;
    }

    public final InputStream getInputStream() throws IOException {
        InputStream local = this.inputStreamRef.get();
        if (local != null) {
            return local;
        }
        local = this.getInputStream(this.socket);
        if (this.inputStreamRef.compareAndSet(null, local)) {
            return local;
        }
        return this.inputStreamRef.get();
    }

    protected InputStream getInputStream(Socket socket) throws IOException {
        return socket.getInputStream();
    }

    protected OutputStream getOutputStream(Socket socket) throws IOException {
        return socket.getOutputStream();
    }

    public final OutputStream getOutputStream() throws IOException {
        OutputStream local = this.outputStreamRef.get();
        if (local != null) {
            return local;
        }
        local = this.getOutputStream(this.socket);
        if (this.outputStreamRef.compareAndSet(null, local)) {
            return local;
        }
        return this.outputStreamRef.get();
    }

    public String toString() {
        return this.socket.toString();
    }
}

