/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.hc.client5.http.impl.Wire;
import org.apache.hc.client5.http.impl.io.LoggingInputStream;
import org.apache.hc.client5.http.impl.io.LoggingOutputStream;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.slf4j.Logger;

class LoggingSocketHolder
extends SocketHolder {
    private final Wire wire;

    public LoggingSocketHolder(Socket socket, String id, Logger log) {
        super(socket);
        this.wire = new Wire(log, id);
    }

    @Override
    protected InputStream getInputStream(Socket socket) throws IOException {
        return new LoggingInputStream(super.getInputStream(socket), this.wire);
    }

    @Override
    protected OutputStream getOutputStream(Socket socket) throws IOException {
        return new LoggingOutputStream(super.getOutputStream(socket), this.wire);
    }
}

