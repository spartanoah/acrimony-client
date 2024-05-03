/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class KerberosConfig
implements Cloneable {
    public static final KerberosConfig DEFAULT = new Builder().build();
    private final Option stripPort;
    private final Option useCanonicalHostname;
    private final Option requestDelegCreds;

    protected KerberosConfig() {
        this(Option.DEFAULT, Option.DEFAULT, Option.DEFAULT);
    }

    KerberosConfig(Option stripPort, Option useCanonicalHostname, Option requestDelegCreds) {
        this.stripPort = stripPort;
        this.useCanonicalHostname = useCanonicalHostname;
        this.requestDelegCreds = requestDelegCreds;
    }

    public Option getStripPort() {
        return this.stripPort;
    }

    public Option getUseCanonicalHostname() {
        return this.useCanonicalHostname;
    }

    public Option getRequestDelegCreds() {
        return this.requestDelegCreds;
    }

    protected KerberosConfig clone() throws CloneNotSupportedException {
        return (KerberosConfig)super.clone();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("stripPort=").append((Object)this.stripPort);
        builder.append(", useCanonicalHostname=").append((Object)this.useCanonicalHostname);
        builder.append(", requestDelegCreds=").append((Object)this.requestDelegCreds);
        builder.append("]");
        return builder.toString();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(KerberosConfig config) {
        return new Builder().setStripPort(config.getStripPort()).setUseCanonicalHostname(config.getUseCanonicalHostname()).setRequestDelegCreds(config.getRequestDelegCreds());
    }

    public static class Builder {
        private Option stripPort = Option.DEFAULT;
        private Option useCanonicalHostname = Option.DEFAULT;
        private Option requestDelegCreds = Option.DEFAULT;

        Builder() {
        }

        public Builder setStripPort(Option stripPort) {
            this.stripPort = stripPort;
            return this;
        }

        public Builder setStripPort(boolean stripPort) {
            this.stripPort = stripPort ? Option.ENABLE : Option.DISABLE;
            return this;
        }

        public Builder setUseCanonicalHostname(Option useCanonicalHostname) {
            this.useCanonicalHostname = useCanonicalHostname;
            return this;
        }

        public Builder setUseCanonicalHostname(boolean useCanonicalHostname) {
            this.useCanonicalHostname = useCanonicalHostname ? Option.ENABLE : Option.DISABLE;
            return this;
        }

        public Builder setRequestDelegCreds(Option requestDelegCreds) {
            this.requestDelegCreds = requestDelegCreds;
            return this;
        }

        public KerberosConfig build() {
            return new KerberosConfig(this.stripPort, this.useCanonicalHostname, this.requestDelegCreds);
        }
    }

    public static enum Option {
        DEFAULT,
        ENABLE,
        DISABLE;

    }
}

