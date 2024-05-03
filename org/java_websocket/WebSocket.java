/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.net.ssl.SSLSession;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.java_websocket.protocols.IProtocol;

public interface WebSocket {
    public void close(int var1, String var2);

    public void close(int var1);

    public void close();

    public void closeConnection(int var1, String var2);

    public void send(String var1);

    public void send(ByteBuffer var1);

    public void send(byte[] var1);

    public void sendFrame(Framedata var1);

    public void sendFrame(Collection<Framedata> var1);

    public void sendPing();

    public void sendFragmentedFrame(Opcode var1, ByteBuffer var2, boolean var3);

    public boolean hasBufferedData();

    public InetSocketAddress getRemoteSocketAddress();

    public InetSocketAddress getLocalSocketAddress();

    public boolean isOpen();

    public boolean isClosing();

    public boolean isFlushAndClose();

    public boolean isClosed();

    public Draft getDraft();

    public ReadyState getReadyState();

    public String getResourceDescriptor();

    public <T> void setAttachment(T var1);

    public <T> T getAttachment();

    public boolean hasSSLSupport();

    public SSLSession getSSLSession() throws IllegalArgumentException;

    public IProtocol getProtocol();
}

