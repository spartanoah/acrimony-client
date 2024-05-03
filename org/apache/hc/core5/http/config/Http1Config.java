/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.config;

import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

public class Http1Config {
    public static final Http1Config DEFAULT = new Builder().build();
    private final int bufferSize;
    private final int chunkSizeHint;
    private final Timeout waitForContinueTimeout;
    private final int maxLineLength;
    private final int maxHeaderCount;
    private final int maxEmptyLineCount;
    private final int initialWindowSize;

    Http1Config(int bufferSize, int chunkSizeHint, Timeout waitForContinueTimeout, int maxLineLength, int maxHeaderCount, int maxEmptyLineCount, int initialWindowSize) {
        this.bufferSize = bufferSize;
        this.chunkSizeHint = chunkSizeHint;
        this.waitForContinueTimeout = waitForContinueTimeout;
        this.maxLineLength = maxLineLength;
        this.maxHeaderCount = maxHeaderCount;
        this.maxEmptyLineCount = maxEmptyLineCount;
        this.initialWindowSize = initialWindowSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public int getChunkSizeHint() {
        return this.chunkSizeHint;
    }

    public Timeout getWaitForContinueTimeout() {
        return this.waitForContinueTimeout;
    }

    public int getMaxLineLength() {
        return this.maxLineLength;
    }

    public int getMaxHeaderCount() {
        return this.maxHeaderCount;
    }

    public int getMaxEmptyLineCount() {
        return this.maxEmptyLineCount;
    }

    public int getInitialWindowSize() {
        return this.initialWindowSize;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[bufferSize=").append(this.bufferSize).append(", chunkSizeHint=").append(this.chunkSizeHint).append(", waitForContinueTimeout=").append(this.waitForContinueTimeout).append(", maxLineLength=").append(this.maxLineLength).append(", maxHeaderCount=").append(this.maxHeaderCount).append(", maxEmptyLineCount=").append(this.maxEmptyLineCount).append(", initialWindowSize=").append(this.initialWindowSize).append("]");
        return builder.toString();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(Http1Config config) {
        Args.notNull(config, "Config");
        return new Builder().setBufferSize(config.getBufferSize()).setChunkSizeHint(config.getChunkSizeHint()).setWaitForContinueTimeout(config.getWaitForContinueTimeout()).setMaxHeaderCount(config.getMaxHeaderCount()).setMaxLineLength(config.getMaxLineLength()).setMaxEmptyLineCount(config.maxEmptyLineCount);
    }

    public static class Builder {
        private int bufferSize = -1;
        private int chunkSizeHint = -1;
        private Timeout waitForContinueTimeout = Timeout.ofSeconds(3L);
        private int maxLineLength = -1;
        private int maxHeaderCount = -1;
        private int maxEmptyLineCount = 10;
        private int initialWindowSize = -1;

        Builder() {
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setChunkSizeHint(int chunkSizeHint) {
            this.chunkSizeHint = chunkSizeHint;
            return this;
        }

        public Builder setWaitForContinueTimeout(Timeout waitForContinueTimeout) {
            this.waitForContinueTimeout = waitForContinueTimeout;
            return this;
        }

        public Builder setMaxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public Builder setMaxHeaderCount(int maxHeaderCount) {
            this.maxHeaderCount = maxHeaderCount;
            return this;
        }

        public Builder setMaxEmptyLineCount(int maxEmptyLineCount) {
            this.maxEmptyLineCount = maxEmptyLineCount;
            return this;
        }

        public Builder setInitialWindowSize(int initialWindowSize) {
            this.initialWindowSize = initialWindowSize;
            return this;
        }

        public Http1Config build() {
            return new Http1Config(this.bufferSize > 0 ? this.bufferSize : 8192, this.chunkSizeHint, this.waitForContinueTimeout != null ? this.waitForContinueTimeout : Timeout.ofSeconds(3L), this.maxLineLength, this.maxHeaderCount, this.maxEmptyLineCount, this.initialWindowSize > 0 ? this.initialWindowSize : 65535);
        }
    }
}

