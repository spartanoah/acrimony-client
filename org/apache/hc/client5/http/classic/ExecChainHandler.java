/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic;

import java.io.IOException;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ExecChainHandler {
    public ClassicHttpResponse execute(ClassicHttpRequest var1, ExecChain.Scope var2, ExecChain var3) throws IOException, HttpException;
}

