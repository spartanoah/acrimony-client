/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import org.apache.hc.core5.io.ModalCloseable;

public interface ListenerEndpoint
extends ModalCloseable {
    public SocketAddress getAddress();

    public boolean isClosed();
}

