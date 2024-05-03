/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.socket;

import java.io.IOException;
import java.net.Socket;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface LayeredConnectionSocketFactory
extends ConnectionSocketFactory {
    public Socket createLayeredSocket(Socket var1, String var2, int var3, HttpContext var4) throws IOException;
}

