/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class IOReactorConfig {
    public static final IOReactorConfig DEFAULT = new Builder().build();
    private final TimeValue selectInterval;
    private final int ioThreadCount;
    private final Timeout soTimeout;
    private final boolean soReuseAddress;
    private final TimeValue soLinger;
    private final boolean soKeepAlive;
    private final boolean tcpNoDelay;
    private final int sndBufSize;
    private final int rcvBufSize;
    private final int backlogSize;
    private final SocketAddress socksProxyAddress;
    private final String socksProxyUsername;
    private final String socksProxyPassword;

    IOReactorConfig(TimeValue selectInterval, int ioThreadCount, Timeout soTimeout, boolean soReuseAddress, TimeValue soLinger, boolean soKeepAlive, boolean tcpNoDelay, int sndBufSize, int rcvBufSize, int backlogSize, SocketAddress socksProxyAddress, String socksProxyUsername, String socksProxyPassword) {
        this.selectInterval = selectInterval;
        this.ioThreadCount = ioThreadCount;
        this.soTimeout = soTimeout;
        this.soReuseAddress = soReuseAddress;
        this.soLinger = soLinger;
        this.soKeepAlive = soKeepAlive;
        this.tcpNoDelay = tcpNoDelay;
        this.sndBufSize = sndBufSize;
        this.rcvBufSize = rcvBufSize;
        this.backlogSize = backlogSize;
        this.socksProxyAddress = socksProxyAddress;
        this.socksProxyUsername = socksProxyUsername;
        this.socksProxyPassword = socksProxyPassword;
    }

    public TimeValue getSelectInterval() {
        return this.selectInterval;
    }

    public int getIoThreadCount() {
        return this.ioThreadCount;
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

    public boolean isSoKeepalive() {
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

    public String getSocksProxyUsername() {
        return this.socksProxyUsername;
    }

    public String getSocksProxyPassword() {
        return this.socksProxyPassword;
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(IOReactorConfig config) {
        Args.notNull(config, "I/O reactor config");
        return new Builder().setSelectInterval(config.getSelectInterval()).setIoThreadCount(config.getIoThreadCount()).setSoTimeout(config.getSoTimeout()).setSoReuseAddress(config.isSoReuseAddress()).setSoLinger(config.getSoLinger()).setSoKeepAlive(config.isSoKeepalive()).setTcpNoDelay(config.isTcpNoDelay()).setSndBufSize(config.getSndBufSize()).setRcvBufSize(config.getRcvBufSize()).setBacklogSize(config.getBacklogSize()).setSocksProxyAddress(config.getSocksProxyAddress()).setSocksProxyUsername(config.getSocksProxyUsername()).setSocksProxyPassword(config.getSocksProxyPassword());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[selectInterval=").append(this.selectInterval).append(", ioThreadCount=").append(this.ioThreadCount).append(", soTimeout=").append(this.soTimeout).append(", soReuseAddress=").append(this.soReuseAddress).append(", soLinger=").append(this.soLinger).append(", soKeepAlive=").append(this.soKeepAlive).append(", tcpNoDelay=").append(this.tcpNoDelay).append(", sndBufSize=").append(this.sndBufSize).append(", rcvBufSize=").append(this.rcvBufSize).append(", backlogSize=").append(this.backlogSize).append(", socksProxyAddress=").append(this.socksProxyAddress).append("]");
        return builder.toString();
    }

    public static class Builder {
        private static int defaultMaxIOThreadCount = -1;
        private TimeValue selectInterval = TimeValue.ofSeconds(1L);
        private int ioThreadCount = Builder.getDefaultMaxIOThreadCount();
        private Timeout soTimeout = Timeout.ZERO_MILLISECONDS;
        private boolean soReuseAddress = false;
        private TimeValue soLinger = TimeValue.NEG_ONE_SECOND;
        private boolean soKeepAlive = false;
        private boolean tcpNoDelay = true;
        private int sndBufSize = 0;
        private int rcvBufSize = 0;
        private int backlogSize = 0;
        private SocketAddress socksProxyAddress = null;
        private String socksProxyUsername = null;
        private String socksProxyPassword = null;

        public static int getDefaultMaxIOThreadCount() {
            return defaultMaxIOThreadCount > 0 ? defaultMaxIOThreadCount : Runtime.getRuntime().availableProcessors();
        }

        public static void setDefaultMaxIOThreadCount(int defaultMaxIOThreadCount) {
            Builder.defaultMaxIOThreadCount = defaultMaxIOThreadCount;
        }

        Builder() {
        }

        public Builder setSelectInterval(TimeValue selectInterval) {
            this.selectInterval = selectInterval;
            return this;
        }

        public Builder setIoThreadCount(int ioThreadCount) {
            this.ioThreadCount = ioThreadCount;
            return this;
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
            this.soLinger = TimeValue.of(soLinger, timeUnit);
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

        public Builder setSocksProxyUsername(String socksProxyUsername) {
            this.socksProxyUsername = socksProxyUsername;
            return this;
        }

        public Builder setSocksProxyPassword(String socksProxyPassword) {
            this.socksProxyPassword = socksProxyPassword;
            return this;
        }

        public IOReactorConfig build() {
            return new IOReactorConfig(this.selectInterval != null ? this.selectInterval : TimeValue.ofSeconds(1L), this.ioThreadCount, Timeout.defaultsToDisabled(this.soTimeout), this.soReuseAddress, TimeValue.defaultsToNegativeOneMillisecond(this.soLinger), this.soKeepAlive, this.tcpNoDelay, this.sndBufSize, this.rcvBufSize, this.backlogSize, this.socksProxyAddress, this.socksProxyUsername, this.socksProxyPassword);
        }
    }
}

