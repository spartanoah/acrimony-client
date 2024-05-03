/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.patchy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class BlockedServers {
    @VisibleForTesting
    static final Set<String> BLOCKED_SERVERS = Sets.newHashSet();
    private static final String SRV_PREFIX = "_minecraft._tcp.";
    private static final Joiner DOT_JOINER = Joiner.on('.');
    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Charset HASH_CHARSET = Charsets.ISO_8859_1;

    public static boolean isBlockedServer(String server) {
        if (server == null || server.isEmpty()) {
            return false;
        }
        if (server.startsWith(SRV_PREFIX)) {
            server = server.substring(SRV_PREFIX.length());
        }
        while (server.charAt(server.length() - 1) == '.') {
            server = server.substring(0, server.length() - 1);
        }
        if (BlockedServers.isBlockedServerHostName(server)) {
            return true;
        }
        ArrayList<String> parts = Lists.newArrayList(DOT_SPLITTER.split(server));
        boolean isIp = BlockedServers.isIp(parts);
        if (!isIp && BlockedServers.isBlockedServerHostName("*." + server)) {
            return true;
        }
        while (parts.size() > 1) {
            parts.remove(isIp ? parts.size() - 1 : 0);
            String starredPart = isIp ? DOT_JOINER.join(parts) + ".*" : "*." + DOT_JOINER.join(parts);
            if (!BlockedServers.isBlockedServerHostName(starredPart)) continue;
            return true;
        }
        return false;
    }

    private static boolean isIp(List<String> address) {
        if (address.size() != 4) {
            return false;
        }
        for (String s : address) {
            try {
                int part = Integer.parseInt(s);
                if (part >= 0 && part <= 255) continue;
                return false;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlockedServerHostName(String server) {
        return BLOCKED_SERVERS.contains(Hashing.sha1().hashBytes(server.toLowerCase().getBytes(HASH_CHARSET)).toString());
    }

    static {
        try {
            BLOCKED_SERVERS.addAll(IOUtils.readLines(new URL("https://sessionserver.mojang.com/blockedservers").openConnection().getInputStream(), HASH_CHARSET));
        } catch (IOException iOException) {
            // empty catch block
        }
    }
}

