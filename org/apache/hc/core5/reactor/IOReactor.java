/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.util.TimeValue;

public interface IOReactor
extends ModalCloseable {
    @Override
    public void close(CloseMode var1);

    public IOReactorStatus getStatus();

    public void initiateShutdown();

    public void awaitShutdown(TimeValue var1) throws InterruptedException;
}

