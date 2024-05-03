/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServer;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;

public class HttpAsyncServer
extends AsyncServer {
    @Internal
    public HttpAsyncServer(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig ioReactorConfig, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener) {
        super(eventHandlerFactory, ioReactorConfig, ioSessionDecorator, exceptionCallback, sessionListener, ShutdownCommand.GRACEFUL_NORMAL_CALLBACK);
    }
}

