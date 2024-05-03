/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.resolver.dns.UnixResolverOptions;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class UnixResolverDnsServerAddressStreamProvider
implements DnsServerAddressStreamProvider {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnixResolverDnsServerAddressStreamProvider.class);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final String RES_OPTIONS = System.getenv("RES_OPTIONS");
    private static final String ETC_RESOLV_CONF_FILE = "/etc/resolv.conf";
    private static final String ETC_RESOLVER_DIR = "/etc/resolver";
    private static final String NAMESERVER_ROW_LABEL = "nameserver";
    private static final String SORTLIST_ROW_LABEL = "sortlist";
    private static final String OPTIONS_ROW_LABEL = "options ";
    private static final String OPTIONS_ROTATE_FLAG = "rotate";
    private static final String DOMAIN_ROW_LABEL = "domain";
    private static final String SEARCH_ROW_LABEL = "search";
    private static final String PORT_ROW_LABEL = "port";
    private final DnsServerAddresses defaultNameServerAddresses;
    private final Map<String, DnsServerAddresses> domainToNameServerStreamMap;

    static DnsServerAddressStreamProvider parseSilently() {
        try {
            UnixResolverDnsServerAddressStreamProvider nameServerCache = new UnixResolverDnsServerAddressStreamProvider(ETC_RESOLV_CONF_FILE, ETC_RESOLVER_DIR);
            return nameServerCache.mayOverrideNameServers() ? nameServerCache : DefaultDnsServerAddressStreamProvider.INSTANCE;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("failed to parse {} and/or {}", ETC_RESOLV_CONF_FILE, ETC_RESOLVER_DIR, e);
            }
            return DefaultDnsServerAddressStreamProvider.INSTANCE;
        }
    }

    public UnixResolverDnsServerAddressStreamProvider(File etcResolvConf, File ... etcResolverFiles) throws IOException {
        Map<String, DnsServerAddresses> etcResolvConfMap = UnixResolverDnsServerAddressStreamProvider.parse(ObjectUtil.checkNotNull(etcResolvConf, "etcResolvConf"));
        boolean useEtcResolverFiles = etcResolverFiles != null && etcResolverFiles.length != 0;
        this.domainToNameServerStreamMap = useEtcResolverFiles ? UnixResolverDnsServerAddressStreamProvider.parse(etcResolverFiles) : etcResolvConfMap;
        DnsServerAddresses defaultNameServerAddresses = etcResolvConfMap.get(etcResolvConf.getName());
        if (defaultNameServerAddresses == null) {
            Collection<DnsServerAddresses> values = etcResolvConfMap.values();
            if (values.isEmpty()) {
                throw new IllegalArgumentException(etcResolvConf + " didn't provide any name servers");
            }
            this.defaultNameServerAddresses = values.iterator().next();
        } else {
            this.defaultNameServerAddresses = defaultNameServerAddresses;
        }
        if (useEtcResolverFiles) {
            this.domainToNameServerStreamMap.putAll(etcResolvConfMap);
        }
    }

    public UnixResolverDnsServerAddressStreamProvider(String etcResolvConf, String etcResolverDir) throws IOException {
        this(etcResolvConf == null ? null : new File(etcResolvConf), etcResolverDir == null ? null : new File(etcResolverDir).listFiles());
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        int i;
        while ((i = hostname.indexOf(46, 1)) >= 0 && i != hostname.length() - 1) {
            DnsServerAddresses addresses = this.domainToNameServerStreamMap.get(hostname);
            if (addresses != null) {
                return addresses.stream();
            }
            hostname = hostname.substring(i + 1);
        }
        return this.defaultNameServerAddresses.stream();
    }

    private boolean mayOverrideNameServers() {
        return !this.domainToNameServerStreamMap.isEmpty() || this.defaultNameServerAddresses.stream().next() != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Map<String, DnsServerAddresses> parse(File ... etcResolverFiles) throws IOException {
        HashMap<String, DnsServerAddresses> domainToNameServerStreamMap = new HashMap<String, DnsServerAddresses>(etcResolverFiles.length << 1);
        boolean rotateGlobal = RES_OPTIONS != null && RES_OPTIONS.contains(OPTIONS_ROTATE_FLAG);
        for (File etcResolverFile : etcResolverFiles) {
            if (!etcResolverFile.isFile()) continue;
            FileReader fr = new FileReader(etcResolverFile);
            BufferedReader br = null;
            try {
                String line;
                br = new BufferedReader(fr);
                ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>(2);
                String domainName = etcResolverFile.getName();
                boolean rotate = rotateGlobal;
                int port = 53;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    try {
                        int i;
                        char c;
                        if (line.isEmpty() || (c = line.charAt(0)) == '#' || c == ';') continue;
                        if (!rotate && line.startsWith(OPTIONS_ROW_LABEL)) {
                            rotate = line.contains(OPTIONS_ROTATE_FLAG);
                            continue;
                        }
                        if (line.startsWith(NAMESERVER_ROW_LABEL)) {
                            String maybeIP;
                            i = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)NAMESERVER_ROW_LABEL.length());
                            if (i < 0) {
                                throw new IllegalArgumentException("error parsing label nameserver in file " + etcResolverFile + ". value: " + line);
                            }
                            int x = StringUtil.indexOfWhiteSpace((CharSequence)line, (int)i);
                            if (x == -1) {
                                maybeIP = line.substring(i);
                            } else {
                                int idx = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)x);
                                if (idx == -1 || line.charAt(idx) != '#') {
                                    throw new IllegalArgumentException("error parsing label nameserver in file " + etcResolverFile + ". value: " + line);
                                }
                                maybeIP = line.substring(i, x);
                            }
                            if (!NetUtil.isValidIpV4Address(maybeIP) && !NetUtil.isValidIpV6Address(maybeIP)) {
                                i = maybeIP.lastIndexOf(46);
                                if (i + 1 >= maybeIP.length()) {
                                    throw new IllegalArgumentException("error parsing label nameserver in file " + etcResolverFile + ". invalid IP value: " + line);
                                }
                                port = Integer.parseInt(maybeIP.substring(i + 1));
                                maybeIP = maybeIP.substring(0, i);
                            }
                            addresses.add(SocketUtils.socketAddress(maybeIP, port));
                            continue;
                        }
                        if (line.startsWith(DOMAIN_ROW_LABEL)) {
                            i = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)DOMAIN_ROW_LABEL.length());
                            if (i < 0) {
                                throw new IllegalArgumentException("error parsing label domain in file " + etcResolverFile + " value: " + line);
                            }
                            domainName = line.substring(i);
                            if (!addresses.isEmpty()) {
                                UnixResolverDnsServerAddressStreamProvider.putIfAbsent(domainToNameServerStreamMap, domainName, addresses, rotate);
                            }
                            addresses = new ArrayList(2);
                            continue;
                        }
                        if (line.startsWith(PORT_ROW_LABEL)) {
                            i = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)PORT_ROW_LABEL.length());
                            if (i < 0) {
                                throw new IllegalArgumentException("error parsing label port in file " + etcResolverFile + " value: " + line);
                            }
                            port = Integer.parseInt(line.substring(i));
                            continue;
                        }
                        if (!line.startsWith(SORTLIST_ROW_LABEL)) continue;
                        logger.info("row type {} not supported. Ignoring line: {}", (Object)SORTLIST_ROW_LABEL, (Object)line);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Could not parse entry. Ignoring line: {}", (Object)line, (Object)e);
                    }
                }
                if (addresses.isEmpty()) continue;
                UnixResolverDnsServerAddressStreamProvider.putIfAbsent(domainToNameServerStreamMap, domainName, addresses, rotate);
            } finally {
                if (br == null) {
                    fr.close();
                } else {
                    br.close();
                }
            }
        }
        return domainToNameServerStreamMap;
    }

    private static void putIfAbsent(Map<String, DnsServerAddresses> domainToNameServerStreamMap, String domainName, List<InetSocketAddress> addresses, boolean rotate) {
        DnsServerAddresses addrs = rotate ? DnsServerAddresses.rotational(addresses) : DnsServerAddresses.sequential(addresses);
        UnixResolverDnsServerAddressStreamProvider.putIfAbsent(domainToNameServerStreamMap, domainName, addrs);
    }

    private static void putIfAbsent(Map<String, DnsServerAddresses> domainToNameServerStreamMap, String domainName, DnsServerAddresses addresses) {
        DnsServerAddresses existingAddresses = domainToNameServerStreamMap.put(domainName, addresses);
        if (existingAddresses != null) {
            domainToNameServerStreamMap.put(domainName, existingAddresses);
            if (logger.isDebugEnabled()) {
                logger.debug("Domain name {} already maps to addresses {} so new addresses {} will be discarded", domainName, existingAddresses, addresses);
            }
        }
    }

    static UnixResolverOptions parseEtcResolverOptions() throws IOException {
        return UnixResolverDnsServerAddressStreamProvider.parseEtcResolverOptions(new File(ETC_RESOLV_CONF_FILE));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static UnixResolverOptions parseEtcResolverOptions(File etcResolvConf) throws IOException {
        UnixResolverOptions.Builder optionsBuilder = UnixResolverOptions.newBuilder();
        FileReader fr = new FileReader(etcResolvConf);
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(OPTIONS_ROW_LABEL)) continue;
                UnixResolverDnsServerAddressStreamProvider.parseResOptions(line.substring(OPTIONS_ROW_LABEL.length()), optionsBuilder);
                break;
            }
        } finally {
            if (br == null) {
                fr.close();
            } else {
                br.close();
            }
        }
        if (RES_OPTIONS != null) {
            UnixResolverDnsServerAddressStreamProvider.parseResOptions(RES_OPTIONS, optionsBuilder);
        }
        return optionsBuilder.build();
    }

    private static void parseResOptions(String line, UnixResolverOptions.Builder builder) {
        String[] opts;
        for (String opt : opts = WHITESPACE_PATTERN.split(line)) {
            try {
                if (opt.startsWith("ndots:")) {
                    builder.setNdots(UnixResolverDnsServerAddressStreamProvider.parseResIntOption(opt, "ndots:"));
                    continue;
                }
                if (opt.startsWith("attempts:")) {
                    builder.setAttempts(UnixResolverDnsServerAddressStreamProvider.parseResIntOption(opt, "attempts:"));
                    continue;
                }
                if (!opt.startsWith("timeout:")) continue;
                builder.setTimeout(UnixResolverDnsServerAddressStreamProvider.parseResIntOption(opt, "timeout:"));
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    private static int parseResIntOption(String opt, String fullLabel) {
        String optValue = opt.substring(fullLabel.length());
        return Integer.parseInt(optValue);
    }

    static List<String> parseEtcResolverSearchDomains() throws IOException {
        return UnixResolverDnsServerAddressStreamProvider.parseEtcResolverSearchDomains(new File(ETC_RESOLV_CONF_FILE));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static List<String> parseEtcResolverSearchDomains(File etcResolvConf) throws IOException {
        String localDomain = null;
        ArrayList<String> searchDomains = new ArrayList<String>();
        FileReader fr = new FileReader(etcResolvConf);
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                int i;
                if (localDomain == null && line.startsWith(DOMAIN_ROW_LABEL)) {
                    i = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)DOMAIN_ROW_LABEL.length());
                    if (i < 0) continue;
                    localDomain = line.substring(i);
                    continue;
                }
                if (!line.startsWith(SEARCH_ROW_LABEL) || (i = StringUtil.indexOfNonWhiteSpace((CharSequence)line, (int)SEARCH_ROW_LABEL.length())) < 0) continue;
                String[] domains = WHITESPACE_PATTERN.split(line.substring(i));
                Collections.addAll(searchDomains, domains);
            }
        } finally {
            if (br == null) {
                fr.close();
            } else {
                br.close();
            }
        }
        return localDomain != null && searchDomains.isEmpty() ? Collections.singletonList(localDomain) : searchDomains;
    }
}

