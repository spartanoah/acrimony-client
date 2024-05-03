/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListEntry;

public class BanList
extends UserList<String, IPBanEntry> {
    public BanList(File bansFile) {
        super(bansFile);
    }

    @Override
    protected UserListEntry<String> createEntry(JsonObject entryData) {
        return new IPBanEntry(entryData);
    }

    public boolean isBanned(SocketAddress address) {
        String s = this.addressToString(address);
        return this.hasEntry(s);
    }

    public IPBanEntry getBanEntry(SocketAddress address) {
        String s = this.addressToString(address);
        return (IPBanEntry)this.getEntry(s);
    }

    private String addressToString(SocketAddress address) {
        String s = address.toString();
        if (s.contains("/")) {
            s = s.substring(s.indexOf(47) + 1);
        }
        if (s.contains(":")) {
            s = s.substring(0, s.indexOf(58));
        }
        return s;
    }
}

