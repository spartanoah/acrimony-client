/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.patchy;

import com.google.common.base.Predicate;
import com.mojang.patchy.BlockedServers;
import com.mojang.patchy.BlockingICF;
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

public class BlockingICFB
implements InitialContextFactoryBuilder {
    private final Predicate<String> blockList;

    public BlockingICFB(Predicate<String> blockList) {
        this.blockList = blockList;
    }

    public static void install() {
        try {
            System.getProperties().setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
            NamingManager.setInitialContextFactoryBuilder(new BlockingICFB(new Predicate<String>(){

                @Override
                public boolean apply(String input) {
                    return BlockedServers.isBlockedServer(input);
                }
            }));
        } catch (Throwable e) {
            System.out.println("Block failed :(");
            e.printStackTrace();
        }
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> env) throws NamingException {
        String className = (String)env.get("java.naming.factory.initial");
        try {
            InitialContextFactory original = (InitialContextFactory)Class.forName(className).newInstance();
            if ("com.sun.jndi.dns.DnsContextFactory".equals(className)) {
                return new BlockingICF(this.blockList, original);
            }
            return original;
        } catch (Exception e) {
            NoInitialContextException ne = new NoInitialContextException("Cannot instantiate class: " + className);
            ne.setRootCause(e);
            throw ne;
        }
    }
}

