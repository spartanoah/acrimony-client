/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

final class UnixResolverOptions {
    private final int ndots;
    private final int timeout;
    private final int attempts;

    UnixResolverOptions(int ndots, int timeout, int attempts) {
        this.ndots = ndots;
        this.timeout = timeout;
        this.attempts = attempts;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    int ndots() {
        return this.ndots;
    }

    int timeout() {
        return this.timeout;
    }

    int attempts() {
        return this.attempts;
    }

    static final class Builder {
        private int ndots = 1;
        private int timeout = 5;
        private int attempts = 16;

        private Builder() {
        }

        void setNdots(int ndots) {
            this.ndots = ndots;
        }

        void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        UnixResolverOptions build() {
            return new UnixResolverOptions(this.ndots, this.timeout, this.attempts);
        }
    }
}

