/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import org.apache.hc.client5.http.impl.async.LoggingIOSession;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.reactor.IOSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LoggingIOSessionDecorator
implements Decorator<IOSession> {
    public static final LoggingIOSessionDecorator INSTANCE = new LoggingIOSessionDecorator();
    private static final Logger WIRE_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http.wire");

    private LoggingIOSessionDecorator() {
    }

    @Override
    public IOSession decorate(IOSession ioSession) {
        Logger sessionLog = LoggerFactory.getLogger(ioSession.getClass());
        if (sessionLog.isDebugEnabled() || WIRE_LOG.isDebugEnabled()) {
            return new LoggingIOSession(ioSession, sessionLog, WIRE_LOG);
        }
        return ioSession;
    }
}

