/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface DnsResolver {
    public InetAddress[] resolve(String var1) throws UnknownHostException;
}

