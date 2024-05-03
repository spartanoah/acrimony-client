/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public class PlainConnectionSocketFactory
implements ConnectionSocketFactory {
    public static final PlainConnectionSocketFactory INSTANCE = new PlainConnectionSocketFactory();

    public static PlainConnectionSocketFactory getSocketFactory() {
        return INSTANCE;
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return new Socket();
    }

    @Override
    public Socket connectSocket(final TimeValue connectTimeout, Socket socket, HttpHost host, final InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        Socket sock;
        Socket socket2 = sock = socket != null ? socket : this.createSocket(context);
        if (localAddress != null) {
            sock.bind(localAddress);
        }
        try {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                    @Override
                    public Object run() throws IOException {
                        sock.connect(remoteAddress, TimeValue.isPositive(connectTimeout) ? connectTimeout.toMillisecondsIntBound() : 0);
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                Asserts.check(e.getCause() instanceof IOException, "method contract violation only checked exceptions are wrapped: " + e.getCause());
                throw (IOException)e.getCause();
            }
        } catch (IOException ex) {
            Closer.closeQuietly(sock);
            throw ex;
        }
        return sock;
    }
}

