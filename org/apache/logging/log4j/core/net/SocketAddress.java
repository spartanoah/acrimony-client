/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidHost;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;

@Plugin(name="SocketAddress", category="Core", printObject=true)
public class SocketAddress {
    private final InetSocketAddress socketAddress;

    public static SocketAddress getLoopback() {
        return new SocketAddress(InetAddress.getLoopbackAddress(), 0);
    }

    private SocketAddress(InetAddress host, int port) {
        this.socketAddress = new InetSocketAddress(host, port);
    }

    public InetSocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    public int getPort() {
        return this.socketAddress.getPort();
    }

    public InetAddress getAddress() {
        return this.socketAddress.getAddress();
    }

    public String getHostName() {
        return this.socketAddress.getHostName();
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public String toString() {
        return this.socketAddress.toString();
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<SocketAddress> {
        @PluginBuilderAttribute
        @ValidHost
        private InetAddress host;
        @PluginBuilderAttribute
        @ValidPort
        private int port;

        public Builder setHost(InetAddress host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public SocketAddress build() {
            return new SocketAddress(this.host, this.port);
        }
    }
}

