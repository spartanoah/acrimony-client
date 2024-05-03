/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

final class DirContextUtils {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DirContextUtils.class);

    private DirContextUtils() {
    }

    static void addNameServers(List<InetSocketAddress> defaultNameServers, int defaultPort) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url", "dns://");
        try {
            InitialDirContext ctx = new InitialDirContext(env);
            String dnsUrls = (String)ctx.getEnvironment().get("java.naming.provider.url");
            if (dnsUrls != null && !dnsUrls.isEmpty()) {
                String[] servers;
                for (String server : servers = dnsUrls.split(" ")) {
                    try {
                        URI uri = new URI(server);
                        String host = new URI(server).getHost();
                        if (host == null || host.isEmpty()) {
                            logger.debug("Skipping a nameserver URI as host portion could not be extracted: {}", (Object)server);
                            continue;
                        }
                        int port = uri.getPort();
                        defaultNameServers.add(SocketUtils.socketAddress(uri.getHost(), port == -1 ? defaultPort : port));
                    } catch (URISyntaxException e) {
                        logger.debug("Skipping a malformed nameserver URI: {}", (Object)server, (Object)e);
                    }
                }
            }
        } catch (NamingException namingException) {
            // empty catch block
        }
    }
}

