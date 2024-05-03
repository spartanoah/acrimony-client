/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.command;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;

@Internal
public final class CommandSupport {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void failCommands(IOSession ioSession, Exception ex) {
        Command command;
        Args.notNull(ioSession, "I/O session");
        while ((command = ioSession.poll()) != null) {
            if (command instanceof RequestExecutionCommand) {
                AsyncClientExchangeHandler exchangeHandler = ((RequestExecutionCommand)command).getExchangeHandler();
                try {
                    exchangeHandler.failed(ex);
                    continue;
                } finally {
                    exchangeHandler.releaseResources();
                    continue;
                }
            }
            command.cancel();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void cancelCommands(IOSession ioSession) {
        Command command;
        Args.notNull(ioSession, "I/O session");
        while ((command = ioSession.poll()) != null) {
            if (command instanceof RequestExecutionCommand) {
                AsyncClientExchangeHandler exchangeHandler = ((RequestExecutionCommand)command).getExchangeHandler();
                try {
                    if (!ioSession.isOpen()) {
                        exchangeHandler.failed(new ConnectionClosedException());
                        continue;
                    }
                    exchangeHandler.cancel();
                    continue;
                } finally {
                    exchangeHandler.releaseResources();
                    continue;
                }
            }
            command.cancel();
        }
    }
}

