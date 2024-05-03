/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.BHttpConnection;

public interface HttpServerConnection
extends BHttpConnection {
    public ClassicHttpRequest receiveRequestHeader() throws HttpException, IOException;

    public void receiveRequestEntity(ClassicHttpRequest var1) throws HttpException, IOException;

    public void sendResponseHeader(ClassicHttpResponse var1) throws HttpException, IOException;

    public void sendResponseEntity(ClassicHttpResponse var1) throws HttpException, IOException;
}

