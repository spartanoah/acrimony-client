/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.channel.ChannelException;
import io.netty.channel.unix.DatagramSocketAddress;
import io.netty.channel.unix.DomainDatagramSocketAddress;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.unix.NativeInetAddress;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Socket
extends FileDescriptor {
    @Deprecated
    public static final int UDS_SUN_PATH_SIZE = 100;
    protected final boolean ipv6;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    public Socket(int fd) {
        super(fd);
        this.ipv6 = Socket.isIPv6(fd);
    }

    private boolean useIpv6(InetAddress address) {
        return this.ipv6 || address instanceof Inet6Address;
    }

    public final void shutdown() throws IOException {
        this.shutdown(true, true);
    }

    public final void shutdown(boolean read, boolean write) throws IOException {
        int newState;
        int oldState;
        do {
            if (Socket.isClosed(oldState = this.state)) {
                throw new ClosedChannelException();
            }
            newState = oldState;
            if (read && !Socket.isInputShutdown(newState)) {
                newState = Socket.inputShutdown(newState);
            }
            if (write && !Socket.isOutputShutdown(newState)) {
                newState = Socket.outputShutdown(newState);
            }
            if (newState != oldState) continue;
            return;
        } while (!this.casState(oldState, newState));
        int res = Socket.shutdown(this.fd, read, write);
        if (res < 0) {
            Errors.ioResult("shutdown", res);
        }
    }

    public final boolean isShutdown() {
        int state = this.state;
        return Socket.isInputShutdown(state) && Socket.isOutputShutdown(state);
    }

    public final boolean isInputShutdown() {
        return Socket.isInputShutdown(this.state);
    }

    public final boolean isOutputShutdown() {
        return Socket.isOutputShutdown(this.state);
    }

    public final int sendTo(ByteBuffer buf, int pos, int limit, InetAddress addr, int port) throws IOException {
        return this.sendTo(buf, pos, limit, addr, port, false);
    }

    public final int sendTo(ByteBuffer buf, int pos, int limit, InetAddress addr, int port, boolean fastOpen) throws IOException {
        int scopeId;
        byte[] address;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
        }
        int flags = fastOpen ? Socket.msgFastopen() : 0;
        int res = Socket.sendTo(this.fd, this.useIpv6(addr), buf, pos, limit, address, scopeId, port, flags);
        if (res >= 0) {
            return res;
        }
        if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE && fastOpen) {
            return 0;
        }
        if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
            throw new PortUnreachableException("sendTo failed");
        }
        return Errors.ioResult("sendTo", res);
    }

    public final int sendToDomainSocket(ByteBuffer buf, int pos, int limit, byte[] path) throws IOException {
        int res = Socket.sendToDomainSocket(this.fd, buf, pos, limit, path);
        if (res >= 0) {
            return res;
        }
        return Errors.ioResult("sendToDomainSocket", res);
    }

    public final int sendToAddress(long memoryAddress, int pos, int limit, InetAddress addr, int port) throws IOException {
        return this.sendToAddress(memoryAddress, pos, limit, addr, port, false);
    }

    public final int sendToAddress(long memoryAddress, int pos, int limit, InetAddress addr, int port, boolean fastOpen) throws IOException {
        int scopeId;
        byte[] address;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
        }
        int flags = fastOpen ? Socket.msgFastopen() : 0;
        int res = Socket.sendToAddress(this.fd, this.useIpv6(addr), memoryAddress, pos, limit, address, scopeId, port, flags);
        if (res >= 0) {
            return res;
        }
        if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE && fastOpen) {
            return 0;
        }
        if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
            throw new PortUnreachableException("sendToAddress failed");
        }
        return Errors.ioResult("sendToAddress", res);
    }

    public final int sendToAddressDomainSocket(long memoryAddress, int pos, int limit, byte[] path) throws IOException {
        int res = Socket.sendToAddressDomainSocket(this.fd, memoryAddress, pos, limit, path);
        if (res >= 0) {
            return res;
        }
        return Errors.ioResult("sendToAddressDomainSocket", res);
    }

    public final int sendToAddresses(long memoryAddress, int length, InetAddress addr, int port) throws IOException {
        return this.sendToAddresses(memoryAddress, length, addr, port, false);
    }

    public final int sendToAddresses(long memoryAddress, int length, InetAddress addr, int port, boolean fastOpen) throws IOException {
        int scopeId;
        byte[] address;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        } else {
            scopeId = 0;
            address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
        }
        int flags = fastOpen ? Socket.msgFastopen() : 0;
        int res = Socket.sendToAddresses(this.fd, this.useIpv6(addr), memoryAddress, length, address, scopeId, port, flags);
        if (res >= 0) {
            return res;
        }
        if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE && fastOpen) {
            return 0;
        }
        if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE) {
            throw new PortUnreachableException("sendToAddresses failed");
        }
        return Errors.ioResult("sendToAddresses", res);
    }

    public final int sendToAddressesDomainSocket(long memoryAddress, int length, byte[] path) throws IOException {
        int res = Socket.sendToAddressesDomainSocket(this.fd, memoryAddress, length, path);
        if (res >= 0) {
            return res;
        }
        return Errors.ioResult("sendToAddressesDomainSocket", res);
    }

    public final DatagramSocketAddress recvFrom(ByteBuffer buf, int pos, int limit) throws IOException {
        return Socket.recvFrom(this.fd, buf, pos, limit);
    }

    public final DatagramSocketAddress recvFromAddress(long memoryAddress, int pos, int limit) throws IOException {
        return Socket.recvFromAddress(this.fd, memoryAddress, pos, limit);
    }

    public final DomainDatagramSocketAddress recvFromDomainSocket(ByteBuffer buf, int pos, int limit) throws IOException {
        return Socket.recvFromDomainSocket(this.fd, buf, pos, limit);
    }

    public final DomainDatagramSocketAddress recvFromAddressDomainSocket(long memoryAddress, int pos, int limit) throws IOException {
        return Socket.recvFromAddressDomainSocket(this.fd, memoryAddress, pos, limit);
    }

    public final int recvFd() throws IOException {
        int res = Socket.recvFd(this.fd);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        throw Errors.newIOException("recvFd", res);
    }

    public final int sendFd(int fdToSend) throws IOException {
        int res = Socket.sendFd(this.fd, fdToSend);
        if (res >= 0) {
            return res;
        }
        if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw Errors.newIOException("sendFd", res);
    }

    public final boolean connect(SocketAddress socketAddress) throws IOException {
        int res;
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            InetAddress inetAddress = inetSocketAddress.getAddress();
            NativeInetAddress address = NativeInetAddress.newInstance(inetAddress);
            res = Socket.connect(this.fd, this.useIpv6(inetAddress), address.address, address.scopeId, inetSocketAddress.getPort());
        } else if (socketAddress instanceof DomainSocketAddress) {
            DomainSocketAddress unixDomainSocketAddress = (DomainSocketAddress)socketAddress;
            res = Socket.connectDomainSocket(this.fd, unixDomainSocketAddress.path().getBytes(CharsetUtil.UTF_8));
        } else {
            throw new Error("Unexpected SocketAddress implementation " + socketAddress);
        }
        if (res < 0) {
            return Errors.handleConnectErrno("connect", res);
        }
        return true;
    }

    public final boolean finishConnect() throws IOException {
        int res = Socket.finishConnect(this.fd);
        if (res < 0) {
            return Errors.handleConnectErrno("finishConnect", res);
        }
        return true;
    }

    public final void disconnect() throws IOException {
        int res = Socket.disconnect(this.fd, this.ipv6);
        if (res < 0) {
            Errors.handleConnectErrno("disconnect", res);
        }
    }

    public final void bind(SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress)socketAddress;
            InetAddress inetAddress = addr.getAddress();
            NativeInetAddress address = NativeInetAddress.newInstance(inetAddress);
            int res = Socket.bind(this.fd, this.useIpv6(inetAddress), address.address, address.scopeId, addr.getPort());
            if (res < 0) {
                throw Errors.newIOException("bind", res);
            }
        } else if (socketAddress instanceof DomainSocketAddress) {
            DomainSocketAddress addr = (DomainSocketAddress)socketAddress;
            int res = Socket.bindDomainSocket(this.fd, addr.path().getBytes(CharsetUtil.UTF_8));
            if (res < 0) {
                throw Errors.newIOException("bind", res);
            }
        } else {
            throw new Error("Unexpected SocketAddress implementation " + socketAddress);
        }
    }

    public final void listen(int backlog) throws IOException {
        int res = Socket.listen(this.fd, backlog);
        if (res < 0) {
            throw Errors.newIOException("listen", res);
        }
    }

    public final int accept(byte[] addr) throws IOException {
        int res = Socket.accept(this.fd, addr);
        if (res >= 0) {
            return res;
        }
        if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw Errors.newIOException("accept", res);
    }

    public final InetSocketAddress remoteAddress() {
        byte[] addr = Socket.remoteAddress(this.fd);
        return addr == null ? null : NativeInetAddress.address(addr, 0, addr.length);
    }

    public final InetSocketAddress localAddress() {
        byte[] addr = Socket.localAddress(this.fd);
        return addr == null ? null : NativeInetAddress.address(addr, 0, addr.length);
    }

    public final int getReceiveBufferSize() throws IOException {
        return Socket.getReceiveBufferSize(this.fd);
    }

    public final int getSendBufferSize() throws IOException {
        return Socket.getSendBufferSize(this.fd);
    }

    public final boolean isKeepAlive() throws IOException {
        return Socket.isKeepAlive(this.fd) != 0;
    }

    public final boolean isTcpNoDelay() throws IOException {
        return Socket.isTcpNoDelay(this.fd) != 0;
    }

    public final boolean isReuseAddress() throws IOException {
        return Socket.isReuseAddress(this.fd) != 0;
    }

    public final boolean isReusePort() throws IOException {
        return Socket.isReusePort(this.fd) != 0;
    }

    public final boolean isBroadcast() throws IOException {
        return Socket.isBroadcast(this.fd) != 0;
    }

    public final int getSoLinger() throws IOException {
        return Socket.getSoLinger(this.fd);
    }

    public final int getSoError() throws IOException {
        return Socket.getSoError(this.fd);
    }

    public final int getTrafficClass() throws IOException {
        return Socket.getTrafficClass(this.fd, this.ipv6);
    }

    public final void setKeepAlive(boolean keepAlive) throws IOException {
        Socket.setKeepAlive(this.fd, keepAlive ? 1 : 0);
    }

    public final void setReceiveBufferSize(int receiveBufferSize) throws IOException {
        Socket.setReceiveBufferSize(this.fd, receiveBufferSize);
    }

    public final void setSendBufferSize(int sendBufferSize) throws IOException {
        Socket.setSendBufferSize(this.fd, sendBufferSize);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) throws IOException {
        Socket.setTcpNoDelay(this.fd, tcpNoDelay ? 1 : 0);
    }

    public final void setSoLinger(int soLinger) throws IOException {
        Socket.setSoLinger(this.fd, soLinger);
    }

    public final void setReuseAddress(boolean reuseAddress) throws IOException {
        Socket.setReuseAddress(this.fd, reuseAddress ? 1 : 0);
    }

    public final void setReusePort(boolean reusePort) throws IOException {
        Socket.setReusePort(this.fd, reusePort ? 1 : 0);
    }

    public final void setBroadcast(boolean broadcast) throws IOException {
        Socket.setBroadcast(this.fd, broadcast ? 1 : 0);
    }

    public final void setTrafficClass(int trafficClass) throws IOException {
        Socket.setTrafficClass(this.fd, this.ipv6, trafficClass);
    }

    public static native boolean isIPv6Preferred();

    private static native boolean isIPv6(int var0);

    @Override
    public String toString() {
        return "Socket{fd=" + this.fd + '}';
    }

    public static Socket newSocketStream() {
        return new Socket(Socket.newSocketStream0());
    }

    public static Socket newSocketDgram() {
        return new Socket(Socket.newSocketDgram0());
    }

    public static Socket newSocketDomain() {
        return new Socket(Socket.newSocketDomain0());
    }

    public static Socket newSocketDomainDgram() {
        return new Socket(Socket.newSocketDomainDgram0());
    }

    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            Socket.initialize(NetUtil.isIpV4StackPreferred());
        }
    }

    protected static int newSocketStream0() {
        return Socket.newSocketStream0(Socket.isIPv6Preferred());
    }

    protected static int newSocketStream0(boolean ipv6) {
        int res = Socket.newSocketStreamFd(ipv6);
        if (res < 0) {
            throw new ChannelException(Errors.newIOException("newSocketStream", res));
        }
        return res;
    }

    protected static int newSocketDgram0() {
        return Socket.newSocketDgram0(Socket.isIPv6Preferred());
    }

    protected static int newSocketDgram0(boolean ipv6) {
        int res = Socket.newSocketDgramFd(ipv6);
        if (res < 0) {
            throw new ChannelException(Errors.newIOException("newSocketDgram", res));
        }
        return res;
    }

    protected static int newSocketDomain0() {
        int res = Socket.newSocketDomainFd();
        if (res < 0) {
            throw new ChannelException(Errors.newIOException("newSocketDomain", res));
        }
        return res;
    }

    protected static int newSocketDomainDgram0() {
        int res = Socket.newSocketDomainDgramFd();
        if (res < 0) {
            throw new ChannelException(Errors.newIOException("newSocketDomainDgram", res));
        }
        return res;
    }

    private static native int shutdown(int var0, boolean var1, boolean var2);

    private static native int connect(int var0, boolean var1, byte[] var2, int var3, int var4);

    private static native int connectDomainSocket(int var0, byte[] var1);

    private static native int finishConnect(int var0);

    private static native int disconnect(int var0, boolean var1);

    private static native int bind(int var0, boolean var1, byte[] var2, int var3, int var4);

    private static native int bindDomainSocket(int var0, byte[] var1);

    private static native int listen(int var0, int var1);

    private static native int accept(int var0, byte[] var1);

    private static native byte[] remoteAddress(int var0);

    private static native byte[] localAddress(int var0);

    private static native int sendTo(int var0, boolean var1, ByteBuffer var2, int var3, int var4, byte[] var5, int var6, int var7, int var8);

    private static native int sendToAddress(int var0, boolean var1, long var2, int var4, int var5, byte[] var6, int var7, int var8, int var9);

    private static native int sendToAddresses(int var0, boolean var1, long var2, int var4, byte[] var5, int var6, int var7, int var8);

    private static native int sendToDomainSocket(int var0, ByteBuffer var1, int var2, int var3, byte[] var4);

    private static native int sendToAddressDomainSocket(int var0, long var1, int var3, int var4, byte[] var5);

    private static native int sendToAddressesDomainSocket(int var0, long var1, int var3, byte[] var4);

    private static native DatagramSocketAddress recvFrom(int var0, ByteBuffer var1, int var2, int var3) throws IOException;

    private static native DatagramSocketAddress recvFromAddress(int var0, long var1, int var3, int var4) throws IOException;

    private static native DomainDatagramSocketAddress recvFromDomainSocket(int var0, ByteBuffer var1, int var2, int var3) throws IOException;

    private static native DomainDatagramSocketAddress recvFromAddressDomainSocket(int var0, long var1, int var3, int var4) throws IOException;

    private static native int recvFd(int var0);

    private static native int sendFd(int var0, int var1);

    private static native int msgFastopen();

    private static native int newSocketStreamFd(boolean var0);

    private static native int newSocketDgramFd(boolean var0);

    private static native int newSocketDomainFd();

    private static native int newSocketDomainDgramFd();

    private static native int isReuseAddress(int var0) throws IOException;

    private static native int isReusePort(int var0) throws IOException;

    private static native int getReceiveBufferSize(int var0) throws IOException;

    private static native int getSendBufferSize(int var0) throws IOException;

    private static native int isKeepAlive(int var0) throws IOException;

    private static native int isTcpNoDelay(int var0) throws IOException;

    private static native int isBroadcast(int var0) throws IOException;

    private static native int getSoLinger(int var0) throws IOException;

    private static native int getSoError(int var0) throws IOException;

    private static native int getTrafficClass(int var0, boolean var1) throws IOException;

    private static native void setReuseAddress(int var0, int var1) throws IOException;

    private static native void setReusePort(int var0, int var1) throws IOException;

    private static native void setKeepAlive(int var0, int var1) throws IOException;

    private static native void setReceiveBufferSize(int var0, int var1) throws IOException;

    private static native void setSendBufferSize(int var0, int var1) throws IOException;

    private static native void setTcpNoDelay(int var0, int var1) throws IOException;

    private static native void setSoLinger(int var0, int var1) throws IOException;

    private static native void setBroadcast(int var0, int var1) throws IOException;

    private static native void setTrafficClass(int var0, boolean var1, int var2) throws IOException;

    private static native void initialize(boolean var0);
}

