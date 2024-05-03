/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.command;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.reactor.Command;

@Internal
public abstract class ExecutableCommand
implements Command {
    public abstract CancellableDependency getCancellableDependency();

    public abstract void failed(Exception var1);
}

