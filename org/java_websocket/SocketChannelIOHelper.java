/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.WrappedByteChannel;
import org.java_websocket.enums.Role;

public class SocketChannelIOHelper {
    private SocketChannelIOHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean read(ByteBuffer buf, WebSocketImpl ws, ByteChannel channel) throws IOException {
        buf.clear();
        int read = channel.read(buf);
        buf.flip();
        if (read == -1) {
            ws.eot();
            return false;
        }
        return read != 0;
    }

    public static boolean readMore(ByteBuffer buf, WebSocketImpl ws, WrappedByteChannel channel) throws IOException {
        buf.clear();
        int read = channel.readMore(buf);
        buf.flip();
        if (read == -1) {
            ws.eot();
            return false;
        }
        return channel.isNeedRead();
    }

    public static boolean batch(WebSocketImpl ws, ByteChannel sockchannel) throws IOException {
        if (ws == null) {
            return false;
        }
        ByteBuffer buffer = (ByteBuffer)ws.outQueue.peek();
        WrappedByteChannel c = null;
        if (buffer == null) {
            if (sockchannel instanceof WrappedByteChannel && (c = (WrappedByteChannel)sockchannel).isNeedWrite()) {
                c.writeMore();
            }
        } else {
            do {
                sockchannel.write(buffer);
                if (buffer.remaining() > 0) {
                    return false;
                }
                ws.outQueue.poll();
            } while ((buffer = (ByteBuffer)ws.outQueue.peek()) != null);
        }
        if (ws.outQueue.isEmpty() && ws.isFlushAndClose() && ws.getDraft() != null && ws.getDraft().getRole() != null && ws.getDraft().getRole() == Role.SERVER) {
            ws.closeConnection();
        }
        return c == null || !((WrappedByteChannel)sockchannel).isNeedWrite();
    }
}

