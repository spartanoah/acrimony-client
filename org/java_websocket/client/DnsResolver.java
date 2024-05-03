/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.client;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public interface DnsResolver {
    public InetAddress resolve(URI var1) throws UnknownHostException;
}

