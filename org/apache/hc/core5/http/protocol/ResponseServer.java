/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class ResponseServer
implements HttpResponseInterceptor {
    private final String originServer;

    public ResponseServer(String originServer) {
        this.originServer = originServer;
    }

    public ResponseServer() {
        this(null);
    }

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (!response.containsHeader("Server") && this.originServer != null) {
            response.addHeader("Server", this.originServer);
        }
    }
}

