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
public final class URIAuthority
implements NamedEndpoint,
Serializable {
    private static final long serialVersionUID = 1L;
    private final String userInfo;
    private final String hostname;
    private final int port;

    private URIAuthority(String userInfo, String hostname, int port, boolean internal) {
        this.userInfo = userInfo;
        this.hostname = hostname;
        this.port = Ports.checkWithDefault(port);
    }

    public URIAuthority(String userInfo, String hostname, int port) {
        Args.containsNoBlanks(hostname, "Host name");
        if (userInfo != null) {
            Args.containsNoBlanks(userInfo, "User info");
        }
        this.userInfo = userInfo;
        this.hostname = hostname.toLowerCase(Locale.ROOT);
        this.port = Ports.checkWithDefault(port);
    }

    public URIAuthority(String hostname, int port) {
        this(null, hostname, port);
    }

    public URIAuthority(NamedEndpoint namedEndpoint) {
        this(null, namedEndpoint.getHostName(), namedEndpoint.getPort());
    }

    public static URIAuthority create(String s) throws URISyntaxException {
        int atIdx;
        if (s == null) {
            return null;
        }
        String userInfo = null;
        String hostname = s;
        int port = -1;
        int portIdx = hostname.lastIndexOf(":");
        if (portIdx > 0) {
            try {
                port = Integer.parseInt(hostname.substring(portIdx + 1));
            } catch (NumberFormatException ex) {
                throw new URISyntaxException(s, "invalid port");
            }
            hostname = hostname.substring(0, portIdx);
        }
        if ((atIdx = hostname.lastIndexOf("@")) > 0) {
            userInfo = hostname.substring(0, atIdx);
            if (TextUtils.containsBlanks(userInfo)) {
                throw new URISyntaxException(s, "user info contains blanks");
            }
            hostname = hostname.substring(atIdx + 1);
        }
        if (TextUtils.containsBlanks(hostname)) {
            throw new URISyntaxException(s, "hostname contains blanks");
        }
        return new URIAuthority(userInfo, hostname.toLowerCase(Locale.ROOT), port, true);
    }

    public URIAuthority(String hostname) {
        this(null, hostname, -1);
    }

    public String getUserInfo() {
        return this.userInfo;
    }

    @Override
    public String getHostName() {
        return this.hostname;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (this.userInfo != null) {
            buffer.append(this.userInfo);
            buffer.append("@");
        }
        buffer.append(this.hostname);
        if (this.port != -1) {
            buffer.append(":");
            buffer.append(Integer.toString(this.port));
        }
        return buffer.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof URIAuthority) {
            URIAuthority that = (URIAuthority)obj;
            return LangUtils.equals(this.userInfo, that.userInfo) && LangUtils.equals(this.hostname, that.hostname) && this.port == that.port;
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.userInfo);
        hash = LangUtils.hashCode(hash, this.hostname);
        hash = LangUtils.hashCode(hash, this.port);
        return hash;
    }
}

