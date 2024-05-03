/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.protocols;

import java.util.regex.Pattern;
import org.java_websocket.protocols.IProtocol;

public class Protocol
implements IProtocol {
    private static final Pattern patternSpace = Pattern.compile(" ");
    private static final Pattern patternComma = Pattern.compile(",");
    private final String providedProtocol;

    public Protocol(String providedProtocol) {
        if (providedProtocol == null) {
            throw new IllegalArgumentException();
        }
        this.providedProtocol = providedProtocol;
    }

    @Override
    public boolean acceptProvidedProtocol(String inputProtocolHeader) {
        String[] headers;
        if ("".equals(this.providedProtocol)) {
            return true;
        }
        String protocolHeader = patternSpace.matcher(inputProtocolHeader).replaceAll("");
        for (String header : headers = patternComma.split(protocolHeader)) {
            if (!this.providedProtocol.equals(header)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getProvidedProtocol() {
        return this.providedProtocol;
    }

    @Override
    public IProtocol copyInstance() {
        return new Protocol(this.getProvidedProtocol());
    }

    @Override
    public String toString() {
        return this.getProvidedProtocol();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Protocol protocol = (Protocol)o;
        return this.providedProtocol.equals(protocol.providedProtocol);
    }

    public int hashCode() {
        return this.providedProtocol.hashCode();
    }
}

