/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async;

import java.io.IOException;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface AsyncExecChainHandler {
    public void execute(HttpRequest var1, AsyncEntityProducer var2, AsyncExecChain.Scope var3, AsyncExecChain var4, AsyncExecCallback var5) throws HttpException, IOException;
}

