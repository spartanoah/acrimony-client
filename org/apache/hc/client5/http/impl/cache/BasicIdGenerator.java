/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.Locale;

class BasicIdGenerator {
    private final String hostname;
    private final SecureRandom rnd;
    private long count;

    public BasicIdGenerator() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            hostname = "localhost";
        }
        this.hostname = hostname;
        try {
            this.rnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            throw new Error(ex);
        }
        this.rnd.setSeed(System.currentTimeMillis());
    }

    public synchronized void generate(StringBuilder buffer) {
        ++this.count;
        int rndnum = this.rnd.nextInt();
        buffer.append(System.currentTimeMillis());
        buffer.append('.');
        Formatter formatter = new Formatter(buffer, Locale.ROOT);
        formatter.format("%1$016x-%2$08x", this.count, rndnum);
        formatter.close();
        buffer.append('.');
        buffer.append(this.hostname);
    }

    public String generate() {
        StringBuilder buffer = new StringBuilder();
        this.generate(buffer);
        return buffer.toString();
    }
}

