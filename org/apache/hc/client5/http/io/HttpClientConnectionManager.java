/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.io;

import java.io.IOException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public interface HttpClientConnectionManager
extends ModalCloseable {
    public LeaseRequest lease(String var1, HttpRoute var2, Timeout var3, Object var4);

    public void release(ConnectionEndpoint var1, Object var2, TimeValue var3);

    public void connect(ConnectionEndpoint var1, TimeValue var2, HttpContext var3) throws IOException;

    public void upgrade(ConnectionEndpoint var1, HttpContext var2) throws IOException;
}

