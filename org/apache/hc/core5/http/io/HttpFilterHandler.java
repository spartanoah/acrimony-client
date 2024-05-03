/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface HttpFilterHandler {
    public void handle(ClassicHttpRequest var1, HttpFilterChain.ResponseTrigger var2, HttpContext var3, HttpFilterChain var4) throws HttpException, IOException;
}

