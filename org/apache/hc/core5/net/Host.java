/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.net;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Locale;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.net.Ports;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class Host
implements NamedEndpoint,
Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String lcName;
    private final int port;

    public Host(String name, int port) {
        this.name = Args.containsNoBlanks(name, "Host name");
        this.port = Ports.check(port);
        this.lcName = this.name.toLowerCase(Locale.ROOT);
    }

    public static Host create(String s) throws URISyntaxException {
        Args.notEmpty(s, "HTTP Host");
        int portIdx = s.lastIndexOf(":");
        if (portIdx > 0) {
            int port;
            try {
                port = Integer.parseInt(s.substring(portIdx + 1));
            } catch (NumberFormatException ex) {
                throw new URISyntaxException(s, "invalid port");
            }
            String hostname = s.substring(0, portIdx);
            if (TextUtils.containsBlanks(hostname)) {
                throw new URISyntaxException(s, "hostname contains blanks");
            }
            return new Host(hostname, port);
        }
        throw new URISyntaxException(s, "port not found");
    }

    @Override
    public String getHostName() {
        return this.name;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Host) {
            Host that = (Host)o;
            return this.lcName.equals(that.lcName) && this.port == that.port;
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.lcName);
        hash = LangUtils.hashCode(hash, this.port);
        return hash;
    }

    public String toString() {
        return this.name + ":" + this.port;
    }
}

