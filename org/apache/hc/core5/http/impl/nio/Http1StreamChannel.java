/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.impl.nio.FlushMode;
import org.apache.hc.core5.http.nio.ContentEncoder;
import org.apache.hc.core5.util.Timeout;

interface Http1StreamChannel<OutgoingMessage extends HttpMessage>
extends ContentEncoder {
    public void close();

    public void activate() throws HttpException, IOException;

    public void submit(OutgoingMessage var1, boolean var2, FlushMode var3) throws HttpException, IOException;

    public void requestOutput();

    public void suspendOutput() throws IOException;

    public boolean abortGracefully() throws IOException;

    public Timeout getSocketTimeout();

    public void setSocketTimeout(Timeout var1);
}

