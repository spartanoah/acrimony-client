/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.Serializable;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class ProtocolVersion
implements Serializable {
    private static final long serialVersionUID = 8950662842175091068L;
    private final String protocol;
    private final int major;
    private final int minor;

    public ProtocolVersion(String protocol, int major, int minor) {
        this.protocol = Args.notNull(protocol, "Protocol name");
        this.major = Args.notNegative(major, "Protocol minor version");
        this.minor = Args.notNegative(minor, "Protocol minor version");
    }

    public final String getProtocol() {
        return this.protocol;
    }

    public final int getMajor() {
        return this.major;
    }

    public final int getMinor() {
        return this.minor;
    }

    public final int hashCode() {
        return this.protocol.hashCode() ^ this.major * 100000 ^ this.minor;
    }

    public final boolean equals(int major, int minor) {
        return this.major == major && this.minor == minor;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProtocolVersion)) {
            return false;
        }
        ProtocolVersion that = (ProtocolVersion)obj;
        return this.protocol.equals(that.protocol) && this.major == that.major && this.minor == that.minor;
    }

    public String format() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.protocol);
        buffer.append('/');
        buffer.append(Integer.toString(this.major));
        buffer.append('.');
        buffer.append(Integer.toString(this.minor));
        return buffer.toString();
    }

    public boolean isComparable(ProtocolVersion that) {
        return that != null && this.protocol.equals(that.protocol);
    }

    public int compareToVersion(ProtocolVersion that) {
        Args.notNull(that, "Protocol version");
        Args.check(this.protocol.equals(that.protocol), "Versions for different protocols cannot be compared: %s %s", this, that);
        int delta = this.getMajor() - that.getMajor();
        if (delta == 0) {
            delta = this.getMinor() - that.getMinor();
        }
        return delta;
    }

    public final boolean greaterEquals(ProtocolVersion version) {
        return this.isComparable(version) && this.compareToVersion(version) >= 0;
    }

    public final boolean lessEquals(ProtocolVersion version) {
        return this.isComparable(version) && this.compareToVersion(version) <= 0;
    }

    public String toString() {
        return this.format();
    }
}

