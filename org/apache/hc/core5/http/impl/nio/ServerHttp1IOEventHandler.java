/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.impl.nio.AbstractHttp1IOEventHandler;
import org.apache.hc.core5.http.impl.nio.ServerHttp1StreamDuplexer;
import org.apache.hc.core5.net.InetAddressUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class ServerHttp1IOEventHandler
extends AbstractHttp1IOEventHandler {
    public ServerHttp1IOEventHandler(ServerHttp1StreamDuplexer streamDuplexer) {
        super(streamDuplexer);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        InetAddressUtils.formatAddress(buf, this.getRemoteAddress());
        buf.append("->");
        InetAddressUtils.formatAddress(buf, this.getLocalAddress());
        buf.append(" [");
        this.streamDuplexer.appendState(buf);
        buf.append("]");
        return buf.toString();
    }
}

