/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.BHttpConnection;

public interface HttpClientConnection
extends BHttpConnection {
    public boolean isConsistent();

    public void sendRequestHeader(ClassicHttpRequest var1) throws HttpException, IOException;

    public void terminateRequest(ClassicHttpRequest var1) throws HttpException, IOException;

    public void sendRequestEntity(ClassicHttpRequest var1) throws HttpException, IOException;

    public ClassicHttpResponse receiveResponseHeader() throws HttpException, IOException;

    public void receiveResponseEntity(ClassicHttpResponse var1) throws HttpException, IOException;
}

