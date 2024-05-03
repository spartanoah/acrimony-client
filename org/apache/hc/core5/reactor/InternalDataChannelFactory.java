/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.InternalDataChannel;

interface InternalDataChannelFactory {
    public InternalDataChannel create(SelectionKey var1, SocketChannel var2, NamedEndpoint var3, Object var4);
}

