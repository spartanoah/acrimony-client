/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.ssl;

import java.util.ArrayList;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.ssl.TlsVersionParser;

public enum TLS {
    V_1_0("TLSv1", new ProtocolVersion("TLS", 1, 0)),
    V_1_1("TLSv1.1", new ProtocolVersion("TLS", 1, 1)),
    V_1_2("TLSv1.2", new ProtocolVersion("TLS", 1, 2)),
    V_1_3("TLSv1.3", new ProtocolVersion("TLS", 1, 3));

    public final String id;
    public final ProtocolVersion version;

    private TLS(String id, ProtocolVersion version) {
        this.id = id;
        this.version = version;
    }

    public boolean isSame(ProtocolVersion protocolVersion) {
        return this.version.equals(protocolVersion);
    }

    public boolean isComparable(ProtocolVersion protocolVersion) {
        return this.version.isComparable(protocolVersion);
    }

    public boolean greaterEquals(ProtocolVersion protocolVersion) {
        return this.version.greaterEquals(protocolVersion);
    }

    public boolean lessEquals(ProtocolVersion protocolVersion) {
        return this.version.lessEquals(protocolVersion);
    }

    public static ProtocolVersion parse(String s) throws ParseException {
        if (s == null) {
            return null;
        }
        ParserCursor cursor = new ParserCursor(0, s.length());
        return TlsVersionParser.INSTANCE.parse(s, cursor, null);
    }

    public static String[] excludeWeak(String ... protocols) {
        if (protocols == null) {
            return null;
        }
        ArrayList<String> enabledProtocols = new ArrayList<String>();
        for (String protocol : protocols) {
            if (protocol.startsWith("SSL") || protocol.equals(TLS.V_1_0.id) || protocol.equals(TLS.V_1_1.id)) continue;
            enabledProtocols.add(protocol);
        }
        if (enabledProtocols.isEmpty()) {
            enabledProtocols.add(TLS.V_1_2.id);
        }
        return enabledProtocols.toArray(new String[0]);
    }
}

