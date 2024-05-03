/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.multiplayer;

import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

public class ServerAddress {
    private final String ipAddress;
    private final int serverPort;

    private ServerAddress(String p_i1192_1_, int p_i1192_2_) {
        this.ipAddress = p_i1192_1_;
        this.serverPort = p_i1192_2_;
    }

    public String getIP() {
        return IDN.toASCII(this.ipAddress);
    }

    public int getPort() {
        return this.serverPort;
    }

    public static ServerAddress func_78860_a(String p_78860_0_) {
        int j;
        int i;
        if (p_78860_0_ == null) {
            return null;
        }
        String[] astring = p_78860_0_.split(":");
        if (p_78860_0_.startsWith("[") && (i = p_78860_0_.indexOf("]")) > 0) {
            String s = p_78860_0_.substring(1, i);
            String s1 = p_78860_0_.substring(i + 1).trim();
            if (s1.startsWith(":") && s1.length() > 0) {
                s1 = s1.substring(1);
                astring = new String[]{s, s1};
            } else {
                astring = new String[]{s};
            }
        }
        if (astring.length > 2) {
            astring = new String[]{p_78860_0_};
        }
        String s2 = astring[0];
        int n = j = astring.length > 1 ? ServerAddress.parseIntWithDefault(astring[1], 25565) : 25565;
        if (j == 25565) {
            String[] astring1 = ServerAddress.getServerAddress(s2);
            s2 = astring1[0];
            j = ServerAddress.parseIntWithDefault(astring1[1], 25565);
        }
        return new ServerAddress(s2, j);
    }

    private static String[] getServerAddress(String p_78863_0_) {
        try {
            String s = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            InitialDirContext dircontext = new InitialDirContext(hashtable);
            Attributes attributes = dircontext.getAttributes("_minecraft._tcp." + p_78863_0_, new String[]{"SRV"});
            String[] astring = attributes.get("srv").get().toString().split(" ", 4);
            return new String[]{astring[3], astring[2]};
        } catch (Throwable var6) {
            return new String[]{p_78863_0_, Integer.toString(25565)};
        }
    }

    private static int parseIntWithDefault(String p_78862_0_, int p_78862_1_) {
        try {
            return Integer.parseInt(p_78862_0_.trim());
        } catch (Exception var3) {
            return p_78862_1_;
        }
    }
}

