/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class SocketConfig {
    private static final Timeout DEFAULT_SOCKET_TIMEOUT = Timeout.ofMinutes(3L);
    public static final SocketConfig DEFAULT = new Builder().build();
    private final Timeout soTimeout;
    private final boolean soReuseAddress;
    private final TimeValue soLinger;
    private final boolean soKeepAlive;
    private final boolean tcpNoDelay;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int backlogSize;
    private final SocketAddress socksProxyAddress;

    SocketConfig(Timeout soTimeout, boolean soReuseAddress, TimeValue soLinger, boolean soKeepAlive, boolean tcpNoDelay, int sndBufSize, int rcvBufSize, int backlogSize, SocketAddress socksProxyAddress) {
        this.soTimeout = soTimeout;
        this.soReuseAddress = soReuseAddress;
        this.soLinger = soLinger;
        this.soKeepAlive = soKeepAlive;
        this.tcpNoDelay = tcpNoDelay;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.backlogSize = backlogSize;
        this.socksProxyAddress = socksProxyAddress;
    }

    public Timeout getSoTimeout() {
        return this.soTimeout;
    }

    public boolean isSoReuseAddress() {
        return this.soReuseAddress;
    }

    public TimeValue getSoLinger() {
        return this.soLinger;
    }

    public boolean isSoKeepAlive() {
        return this.soKeepAlive;
    }

    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public int getSndBufSize() {
        return this.sndBufSize;
    }

    public int getRcvBufSize() {
        return this.rcvBufSize;
    }

    public int getBacklogSize() {
        return this.backlogSize;
    }

    public SocketAddress getSocksProxyAddress() {
        return this.socksProxyAddress;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[soTimeout=").append(this.soTimeout).append(", soReuseAddress=").append(this.soReuseAddress).append(", soLinger=").append(this.soLinger).append(", soKeepAlive=").append(this.soKeepAlive).append(", tcpNoDelay=").append(this.tcpNoDelay).append(", sndBufSize=").append(this.sndBufSize).append(", rcvBufSize=").append(this.rcvBufSize).append(", backlogSize=").append(this.backlogSize).append(", socksProxyAddress=").append(this.socksProxyAddress).append("]");
        return builder.toString();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(SocketConfig config) {
        Args.notNull(config, "Socket config");
        return new Builder().setSoTimeout(config.getSoTimeout()).setSoReuseAddress(config.isSoReuseAddress()).setSoLinger(config.getSoLinger()).setSoKeepAlive(config.isSoKeepAlive()).setTcpNoDelay(config.isTcpNoDelay()).setSndBufSize(config.getSndBufSize()).setRcvBufSize(config.getRcvBufSize()).setBacklogSize(config.getBacklogSize()).setSocksProxyAddress(config.getSocksProxyAddress());
    }

    static /* synthetic */ Timeout access$000() {
        return DEFAULT_SOCKET_TIMEOUT;
    }

    public static class Builder {
        private Timeout soTimeout = SocketConfig.access$000();
        private boolean soReuseAddress = false;
        private TimeValue soLinger = TimeValue.NEG_ONE_SECOND;
        private boolean soKeepAlive = false;
        private boolean tcpNoDelay = true;
        private int sndBufSize = 0;
        private int rcvBufSize = 0;
        private int backlogSize = 0;
        private SocketAddress socksProxyAddress = null;

        Builder() {
        }

        public Builder setSoTimeout(int soTimeout, TimeUnit timeUnit) {
            this.soTimeout = Timeout.of(soTimeout, timeUnit);
            return this;
        }

        public Builder setSoTimeout(Timeout soTimeout) {
            this.soTimeout = soTimeout;
            return this;
        }

        public Builder setSoReuseAddress(boolean soReuseAddress) {
            this.soReuseAddress = soReuseAddress;
            return this;
        }

        public Builder setSoLinger(int soLinger, TimeUnit timeUnit) {
            this.soLinger = Timeout.of(soLinger, timeUnit);
            return this;
        }

        public Builder setSoLinger(TimeValue soLinger) {
            this.soLinger = soLinger;
            return this;
        }

        public Builder setSoKeepAlive(boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public Builder setSndBufSize(int sndBufSize) {
            this.sndBufSize = sndBufSize;
            return this;
        }

        public Builder setRcvBufSize(int rcvBufSize) {
            this.rcvBufSize = rcvBufSize;
            return this;
        }

        public Builder setBacklogSize(int backlogSize) {
            this.backlogSize = backlogSize;
            return this;
        }

        public Builder setSocksProxyAddress(SocketAddress socksProxyAddress) {
            this.socksProxyAddress = socksProxyAddress;
            return this;
        }

        public SocketConfig build() {
            return new SocketConfig(Timeout.defaultsToDisabled(this.soTimeout), this.soReuseAddress, this.soLinger != null ? this.soLinger : TimeValue.NEG_ONE_SECOND, this.soKeepAlive, this.tcpNoDelay, this.sndBufSize, this.rcvBufSize, this.backlogSize, this.socksProxyAddress);
        }
    }
}

