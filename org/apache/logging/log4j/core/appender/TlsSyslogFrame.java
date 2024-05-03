/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TlsSyslogFrame {
    private final String message;
    private final int byteLength;

    public TlsSyslogFrame(String message) {
        this.message = message;
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        this.byteLength = messageBytes.length;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return Integer.toString(this.byteLength) + ' ' + this.message;
    }

    public int hashCode() {
        return 31 + Objects.hashCode(this.message);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TlsSyslogFrame)) {
            return false;
        }
        TlsSyslogFrame other = (TlsSyslogFrame)obj;
        return Objects.equals(this.message, other.message);
    }
}

