/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http;

import java.io.IOException;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public interface HttpServerConnection
extends HttpConnection {
    public HttpRequest receiveRequestHeader() throws HttpException, IOException;

    public void receiveRequestEntity(HttpEntityEnclosingRequest var1) throws HttpException, IOException;

    public void sendResponseHeader(HttpResponse var1) throws HttpException, IOException;

    public void sendResponseEntity(HttpResponse var1) throws HttpException, IOException;

    public void flush() throws IOException;
}

