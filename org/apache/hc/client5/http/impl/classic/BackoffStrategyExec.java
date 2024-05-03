/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.BackoffManager;
import org.apache.hc.client5.http.classic.ConnectionBackoffStrategy;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Experimental;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
@Experimental
public final class BackoffStrategyExec
implements ExecChainHandler {
    private final ConnectionBackoffStrategy connectionBackoffStrategy;
    private final BackoffManager backoffManager;

    public BackoffStrategyExec(ConnectionBackoffStrategy connectionBackoffStrategy, BackoffManager backoffManager) {
        Args.notNull(connectionBackoffStrategy, "Connection backoff strategy");
        Args.notNull(backoffManager, "Backoff manager");
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        this.backoffManager = backoffManager;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        ClassicHttpResponse response;
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        HttpRoute route = scope.route;
        try {
            response = chain.proceed(request, scope);
        } catch (IOException | HttpException ex) {
            if (this.connectionBackoffStrategy.shouldBackoff(ex)) {
                this.backoffManager.backOff(route);
            }
            throw ex;
        }
        if (this.connectionBackoffStrategy.shouldBackoff(response)) {
            this.backoffManager.backOff(route);
        } else {
            this.backoffManager.probe(route);
        }
        return response;
    }
}

