/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.io;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class ConnectionEndpoint
implements ModalCloseable {
    public abstract ClassicHttpResponse execute(String var1, ClassicHttpRequest var2, HttpRequestExecutor var3, HttpContext var4) throws IOException, HttpException;

    public abstract boolean isConnected();

    public abstract void setSocketTimeout(Timeout var1);
}

