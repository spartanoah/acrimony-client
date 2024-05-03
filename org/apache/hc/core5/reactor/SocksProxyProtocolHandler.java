/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.nio.command.CommandSupport;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.SocketTimeoutExceptionFactory;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Timeout;

final class SocksProxyProtocolHandler
implements IOEventHandler {
    private static final int MAX_COMMAND_CONNECT_LENGTH = 22;
    private static final byte CLIENT_VERSION = 5;
    private static final byte NO_AUTHENTICATION_REQUIRED = 0;
    private static final byte USERNAME_PASSWORD = 2;
    private static final byte USERNAME_PASSWORD_VERSION = 1;
    private static final byte SUCCESS = 0;
    private static final byte COMMAND_CONNECT = 1;
    private static final byte ATYP_IPV4 = 1;
    private static final byte ATYP_DOMAINNAME = 3;
    private static final byte ATYP_IPV6 = 4;
    private final ProtocolIOSession ioSession;
    private final Object attachment;
    private final InetSocketAddress targetAddress;
    private final String username;
    private final String password;
    private final IOEventHandlerFactory eventHandlerFactory;
    private ByteBuffer buffer = ByteBuffer.allocate(32);
    private State state = State.SEND_AUTH;
    private int remainingResponseSize = -1;

    SocksProxyProtocolHandler(ProtocolIOSession ioSession, Object attachment, InetSocketAddress targetAddress, String username, String password, IOEventHandlerFactory eventHandlerFactory) {
        this.ioSession = ioSession;
        this.attachment = attachment;
        this.targetAddress = targetAddress;
        this.username = username;
        this.password = password;
        this.eventHandlerFactory = eventHandlerFactory;
    }

    @Override
    public void connected(IOSession session) throws IOException {
        this.buffer.put((byte)5);
        this.buffer.put((byte)1);
        this.buffer.put((byte)0);
        this.buffer.flip();
        session.setEventMask(4);
    }

    @Override
    public void outputReady(IOSession session) throws IOException {
        switch (this.state) {
            case SEND_AUTH: {
                if (!this.writeAndPrepareRead(session, 2)) break;
                session.setEventMask(1);
                this.state = State.RECEIVE_AUTH_METHOD;
                break;
            }
            case SEND_USERNAME_PASSWORD: {
                if (!this.writeAndPrepareRead(session, 2)) break;
                session.setEventMask(1);
                this.state = State.RECEIVE_AUTH;
                break;
            }
            case SEND_CONNECT: {
                if (!this.writeAndPrepareRead(session, 2)) break;
                session.setEventMask(1);
                this.state = State.RECEIVE_RESPONSE_CODE;
                break;
            }
            case RECEIVE_AUTH_METHOD: 
            case RECEIVE_AUTH: 
            case RECEIVE_ADDRESS: 
            case RECEIVE_ADDRESS_TYPE: 
            case RECEIVE_RESPONSE_CODE: {
                session.setEventMask(1);
                break;
            }
        }
    }

    @Override
    public void inputReady(IOSession session, ByteBuffer src) throws IOException {
        if (src != null) {
            try {
                this.buffer.put(src);
            } catch (BufferOverflowException ex) {
                throw new IOException("Unexpected input data");
            }
        }
        switch (this.state) {
            case RECEIVE_AUTH_METHOD: {
                if (!this.fillBuffer(session)) break;
                this.buffer.flip();
                byte serverVersion = this.buffer.get();
                byte serverMethod = this.buffer.get();
                if (serverVersion != 5) {
                    throw new IOException("SOCKS server returned unsupported version: " + serverVersion);
                }
                if (serverMethod == 2) {
                    this.buffer.clear();
                    this.setBufferLimit(this.username.length() + this.password.length() + 3);
                    this.buffer.put((byte)1);
                    this.buffer.put((byte)this.username.length());
                    this.buffer.put(this.username.getBytes(StandardCharsets.ISO_8859_1));
                    this.buffer.put((byte)this.password.length());
                    this.buffer.put(this.password.getBytes(StandardCharsets.ISO_8859_1));
                    session.setEventMask(4);
                    this.state = State.SEND_USERNAME_PASSWORD;
                    break;
                }
                if (serverMethod == 0) {
                    this.prepareConnectCommand();
                    session.setEventMask(4);
                    this.state = State.SEND_CONNECT;
                    break;
                }
                throw new IOException("SOCKS server return unsupported authentication method: " + serverMethod);
            }
            case RECEIVE_AUTH: {
                if (!this.fillBuffer(session)) break;
                this.buffer.flip();
                this.buffer.get();
                byte status = this.buffer.get();
                if (status != 0) {
                    throw new IOException("Authentication failed for external SOCKS proxy");
                }
                this.prepareConnectCommand();
                session.setEventMask(4);
                this.state = State.SEND_CONNECT;
                break;
            }
            case RECEIVE_RESPONSE_CODE: {
                if (!this.fillBuffer(session)) break;
                this.buffer.flip();
                byte serverVersion = this.buffer.get();
                byte responseCode = this.buffer.get();
                if (serverVersion != 5) {
                    throw new IOException("SOCKS server returned unsupported version: " + serverVersion);
                }
                if (responseCode != 0) {
                    throw new IOException("SOCKS server was unable to establish connection returned error code: " + responseCode);
                }
                this.buffer.compact();
                this.buffer.limit(3);
                this.state = State.RECEIVE_ADDRESS_TYPE;
            }
            case RECEIVE_ADDRESS_TYPE: {
                int addressSize;
                if (!this.fillBuffer(session)) break;
                this.buffer.flip();
                this.buffer.get();
                byte aType = this.buffer.get();
                if (aType == 1) {
                    addressSize = 4;
                } else if (aType == 4) {
                    addressSize = 16;
                } else if (aType == 3) {
                    addressSize = this.buffer.get() & 0xFF;
                } else {
                    throw new IOException("SOCKS server returned unsupported address type: " + aType);
                }
                this.remainingResponseSize = addressSize + 2;
                this.buffer.compact();
                this.buffer.limit(this.remainingResponseSize);
                this.state = State.RECEIVE_ADDRESS;
            }
            case RECEIVE_ADDRESS: {
                if (!this.fillBuffer(session)) break;
                this.buffer.clear();
                this.state = State.COMPLETE;
                IOEventHandler newHandler = this.eventHandlerFactory.createHandler(this.ioSession, this.attachment);
                this.ioSession.upgrade(newHandler);
                newHandler.connected(this.ioSession);
                break;
            }
            case SEND_AUTH: 
            case SEND_USERNAME_PASSWORD: 
            case SEND_CONNECT: {
                session.setEventMask(4);
                break;
            }
        }
    }

    private void prepareConnectCommand() throws IOException {
        InetAddress address = this.targetAddress.getAddress();
        int port = this.targetAddress.getPort();
        if (address == null || port == 0) {
            throw new UnresolvedAddressException();
        }
        this.buffer.clear();
        this.setBufferLimit(22);
        this.buffer.put((byte)5);
        this.buffer.put((byte)1);
        this.buffer.put((byte)0);
        if (address instanceof Inet4Address) {
            this.buffer.put((byte)1);
            this.buffer.put(address.getAddress());
        } else if (address instanceof Inet6Address) {
            this.buffer.put((byte)4);
            this.buffer.put(address.getAddress());
        } else {
            throw new IOException("Unsupported remote address class: " + address.getClass().getName());
        }
        this.buffer.putShort((short)port);
        this.buffer.flip();
    }

    private void setBufferLimit(int newLimit) {
        if (this.buffer.capacity() < newLimit) {
            ByteBuffer newBuffer = ByteBuffer.allocate(newLimit);
            this.buffer.flip();
            newBuffer.put(this.buffer);
            this.buffer = newBuffer;
        } else {
            this.buffer.limit(newLimit);
        }
    }

    private boolean writeAndPrepareRead(ByteChannel channel, int readSize) throws IOException {
        if (this.writeBuffer(channel)) {
            this.buffer.clear();
            this.setBufferLimit(readSize);
            return true;
        }
        return false;
    }

    private boolean writeBuffer(ByteChannel channel) throws IOException {
        if (this.buffer.hasRemaining()) {
            channel.write(this.buffer);
        }
        return !this.buffer.hasRemaining();
    }

    private boolean fillBuffer(ByteChannel channel) throws IOException {
        if (this.buffer.hasRemaining()) {
            channel.read(this.buffer);
        }
        return !this.buffer.hasRemaining();
    }

    @Override
    public void timeout(IOSession session, Timeout timeout) throws IOException {
        this.exception(session, SocketTimeoutExceptionFactory.create(timeout));
    }

    @Override
    public void exception(IOSession session, Exception cause) {
        session.close(CloseMode.IMMEDIATE);
        CommandSupport.failCommands(session, cause);
    }

    @Override
    public void disconnected(IOSession session) {
        CommandSupport.cancelCommands(session);
    }

    private static enum State {
        SEND_AUTH,
        RECEIVE_AUTH_METHOD,
        SEND_USERNAME_PASSWORD,
        RECEIVE_AUTH,
        SEND_CONNECT,
        RECEIVE_RESPONSE_CODE,
        RECEIVE_ADDRESS_TYPE,
        RECEIVE_ADDRESS,
        COMPLETE;

    }
}

