/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.command;

import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOSession;

public final class ShutdownCommand
implements Command {
    public static final ShutdownCommand GRACEFUL = new ShutdownCommand(CloseMode.GRACEFUL);
    public static final ShutdownCommand IMMEDIATE = new ShutdownCommand(CloseMode.IMMEDIATE);
    public static final Callback<IOSession> GRACEFUL_IMMEDIATE_CALLBACK = ShutdownCommand.createIOSessionCallback(Command.Priority.IMMEDIATE);
    public static final Callback<IOSession> GRACEFUL_NORMAL_CALLBACK = ShutdownCommand.createIOSessionCallback(Command.Priority.NORMAL);
    private final CloseMode type;

    private static Callback<IOSession> createIOSessionCallback(final Command.Priority priority) {
        return new Callback<IOSession>(){

            @Override
            public void execute(IOSession session) {
                session.enqueue(GRACEFUL, priority);
            }
        };
    }

    public ShutdownCommand(CloseMode type) {
        this.type = type;
    }

    public CloseMode getType() {
        return this.type;
    }

    @Override
    public boolean cancel() {
        return true;
    }

    public String toString() {
        return "Shutdown: " + (Object)((Object)this.type);
    }
}

