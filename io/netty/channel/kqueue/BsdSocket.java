/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.channel.DefaultFileRegion;
import io.netty.channel.kqueue.AcceptFilter;
import io.netty.channel.kqueue.Native;
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.NativeInetAddress;
import io.netty.channel.unix.PeerCredentials;
import io.netty.channel.unix.Socket;
import io.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

final class BsdSocket
extends Socket {
    private static final int APPLE_SND_LOW_AT_MAX = 131072;
    private static final int FREEBSD_SND_LOW_AT_MAX = 32768;
    static final int BSD_SND_LOW_AT_MAX = Math.min(131072, 32768);
    private static final int UNSPECIFIED_SOURCE_INTERFACE = 0;

    BsdSocket(int fd) {
        super(fd);
    }

    void setAcceptFilter(AcceptFilter acceptFilter) throws IOException {
        BsdSocket.setAcceptFilter(this.intValue(), acceptFilter.filterName(), acceptFilter.filterArgs());
    }

    void setTcpNoPush(boolean tcpNoPush) throws IOException {
        BsdSocket.setTcpNoPush(this.intValue(), tcpNoPush ? 1 : 0);
    }

    void setSndLowAt(int lowAt) throws IOException {
        BsdSocket.setSndLowAt(this.intValue(), lowAt);
    }

    boolean isTcpNoPush() throws IOException {
        return BsdSocket.getTcpNoPush(this.intValue()) != 0;
    }

    int getSndLowAt() throws IOException {
        return BsdSocket.getSndLowAt(this.intValue());
    }

    AcceptFilter getAcceptFilter() throws IOException {
        String[] result = BsdSocket.getAcceptFilter(this.intValue());
        return result == null ? AcceptFilter.PLATFORM_UNSUPPORTED : new AcceptFilter(result[0], result[1]);
    }

    PeerCredentials getPeerCredentials() throws IOException {
        return BsdSocket.getPeerCredentials(this.intValue());
    }

    long sendFile(DefaultFileRegion src, long baseOffset, long offset, long length) throws IOException {
        src.open();
        long res = BsdSocket.sendFile(this.intValue(), src, baseOffset, offset, length);
        if (res >= 0L) {
            return res;
        }
        return Errors.ioResult("sendfile", (int)res);
    }

    int connectx(InetSocketAddress source, InetSocketAddress destination, IovArray data, boolean tcpFastOpen) throws IOException {
        int iovDataLength;
        int iovCount;
        long iovAddress;
        int destinationScopeId;
        byte[] destinationAddress;
        int sourcePort;
        int sourceScopeId;
        byte[] sourceAddress;
        boolean sourceIPv6;
        int flags;
        ObjectUtil.checkNotNull(destination, "Destination InetSocketAddress cannot be null.");
        int n = flags = tcpFastOpen ? Native.CONNECT_TCP_FASTOPEN : 0;
        if (source == null) {
            sourceIPv6 = false;
            sourceAddress = null;
            sourceScopeId = 0;
            sourcePort = 0;
        } else {
            InetAddress sourceInetAddress = source.getAddress();
            sourceIPv6 = sourceInetAddress instanceof Inet6Address;
            if (sourceIPv6) {
                sourceAddress = sourceInetAddress.getAddress();
                sourceScopeId = ((Inet6Address)sourceInetAddress).getScopeId();
            } else {
                sourceScopeId = 0;
                sourceAddress = NativeInetAddress.ipv4MappedIpv6Address(sourceInetAddress.getAddress());
            }
            sourcePort = source.getPort();
        }
        InetAddress destinationInetAddress = destination.getAddress();
        boolean destinationIPv6 = destinationInetAddress instanceof Inet6Address;
        if (destinationIPv6) {
            destinationAddress = destinationInetAddress.getAddress();
            destinationScopeId = ((Inet6Address)destinationInetAddress).getScopeId();
        } else {
            destinationScopeId = 0;
            destinationAddress = NativeInetAddress.ipv4MappedIpv6Address(destinationInetAddress.getAddress());
        }
        int destinationPort = destination.getPort();
        if (data == null || data.count() == 0) {
            iovAddress = 0L;
            iovCount = 0;
            iovDataLength = 0;
        } else {
            iovAddress = data.memoryAddress(0);
            iovCount = data.count();
            long size = data.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("IovArray.size() too big: " + size + " bytes.");
            }
            iovDataLength = (int)size;
        }
        int result = BsdSocket.connectx(this.intValue(), 0, sourceIPv6, sourceAddress, sourceScopeId, sourcePort, destinationIPv6, destinationAddress, destinationScopeId, destinationPort, flags, iovAddress, iovCount, iovDataLength);
        if (result == Errors.ERRNO_EINPROGRESS_NEGATIVE) {
            return -iovDataLength;
        }
        if (result < 0) {
            return Errors.ioResult("connectx", result);
        }
        return result;
    }

    public static BsdSocket newSocketStream() {
        return new BsdSocket(BsdSocket.newSocketStream0());
    }

    public static BsdSocket newSocketDgram() {
        return new BsdSocket(BsdSocket.newSocketDgram0());
    }

    public static BsdSocket newSocketDomain() {
        return new BsdSocket(BsdSocket.newSocketDomain0());
    }

    public static BsdSocket newSocketDomainDgram() {
        return new BsdSocket(BsdSocket.newSocketDomainDgram0());
    }

    private static native long sendFile(int var0, DefaultFileRegion var1, long var2, long var4, long var6) throws IOException;

    private static native int connectx(int var0, int var1, boolean var2, byte[] var3, int var4, int var5, boolean var6, byte[] var7, int var8, int var9, int var10, long var11, int var13, int var14);

    private static native String[] getAcceptFilter(int var0) throws IOException;

    private static native int getTcpNoPush(int var0) throws IOException;

    private static native int getSndLowAt(int var0) throws IOException;

    private static native PeerCredentials getPeerCredentials(int var0) throws IOException;

    private static native void setAcceptFilter(int var0, String var1, String var2) throws IOException;

    private static native void setTcpNoPush(int var0, int var1) throws IOException;

    private static native void setSndLowAt(int var0, int var1) throws IOException;
}

