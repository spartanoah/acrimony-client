/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface HttpServerRequestHandler {
    public void handle(ClassicHttpRequest var1, ResponseTrigger var2, HttpContext var3) throws HttpException, IOException;

    public static interface ResponseTrigger {
        public void sendInformation(ClassicHttpResponse var1) throws HttpException, IOException;

        public void submitResponse(ClassicHttpResponse var1) throws HttpException, IOException;
    }
}

