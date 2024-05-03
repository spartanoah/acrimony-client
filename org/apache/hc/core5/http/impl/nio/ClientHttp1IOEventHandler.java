/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.http.impl.nio.AbstractHttp1IOEventHandler;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexer;
import org.apache.hc.core5.net.InetAddressUtils;

public class ClientHttp1IOEventHandler
extends AbstractHttp1IOEventHandler {
    public ClientHttp1IOEventHandler(ClientHttp1StreamDuplexer streamDuplexer) {
        super(streamDuplexer);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        InetAddressUtils.formatAddress(buf, this.getLocalAddress());
        buf.append("->");
        InetAddressUtils.formatAddress(buf, this.getRemoteAddress());
        buf.append(" [");
        this.streamDuplexer.appendState(buf);
        buf.append("]");
        return buf.toString();
    }
}

