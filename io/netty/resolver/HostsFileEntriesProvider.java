/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver;

import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class HostsFileEntriesProvider {
    static final HostsFileEntriesProvider EMPTY = new HostsFileEntriesProvider(Collections.<String, List<InetAddress>>emptyMap(), Collections.<String, List<InetAddress>>emptyMap());
    private final Map<String, List<InetAddress>> ipv4Entries;
    private final Map<String, List<InetAddress>> ipv6Entries;

    public static Parser parser() {
        return ParserImpl.INSTANCE;
    }

    HostsFileEntriesProvider(Map<String, List<InetAddress>> ipv4Entries, Map<String, List<InetAddress>> ipv6Entries) {
        this.ipv4Entries = Collections.unmodifiableMap(new HashMap<String, List<InetAddress>>(ipv4Entries));
        this.ipv6Entries = Collections.unmodifiableMap(new HashMap<String, List<InetAddress>>(ipv6Entries));
    }

    public Map<String, List<InetAddress>> ipv4Entries() {
        return this.ipv4Entries;
    }

    public Map<String, List<InetAddress>> ipv6Entries() {
        return this.ipv6Entries;
    }

    private static final class ParserImpl
    implements Parser {
        private static final String WINDOWS_DEFAULT_SYSTEM_ROOT = "C:\\Windows";
        private static final String WINDOWS_HOSTS_FILE_RELATIVE_PATH = "\\system32\\drivers\\etc\\hosts";
        private static final String X_PLATFORMS_HOSTS_FILE_PATH = "/etc/hosts";
        private static final Pattern WHITESPACES = Pattern.compile("[ \t]+");
        private static final InternalLogger logger = InternalLoggerFactory.getInstance(Parser.class);
        static final ParserImpl INSTANCE = new ParserImpl();

        private ParserImpl() {
        }

        @Override
        public HostsFileEntriesProvider parse() throws IOException {
            return this.parse(ParserImpl.locateHostsFile(), Charset.defaultCharset());
        }

        @Override
        public HostsFileEntriesProvider parse(Charset ... charsets) throws IOException {
            return this.parse(ParserImpl.locateHostsFile(), charsets);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public HostsFileEntriesProvider parse(File file, Charset ... charsets) throws IOException {
            ObjectUtil.checkNotNull(file, "file");
            ObjectUtil.checkNotNull(charsets, "charsets");
            if (charsets.length == 0) {
                charsets = new Charset[]{Charset.defaultCharset()};
            }
            if (file.exists() && file.isFile()) {
                for (Charset charset : charsets) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(file), charset));
                    try {
                        HostsFileEntriesProvider entries = this.parse(reader);
                        if (entries == EMPTY) continue;
                        HostsFileEntriesProvider hostsFileEntriesProvider = entries;
                        return hostsFileEntriesProvider;
                    } finally {
                        reader.close();
                    }
                }
            }
            return EMPTY;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public HostsFileEntriesProvider parse(Reader reader) throws IOException {
            ObjectUtil.checkNotNull(reader, "reader");
            BufferedReader buff = new BufferedReader(reader);
            try {
                String line;
                HashMap<String, List<InetAddress>> ipv4Entries = new HashMap<String, List<InetAddress>>();
                HashMap<String, List<InetAddress>> ipv6Entries = new HashMap<String, List<InetAddress>>();
                while ((line = buff.readLine()) != null) {
                    byte[] ipBytes;
                    int commentPosition = line.indexOf(35);
                    if (commentPosition != -1) {
                        line = line.substring(0, commentPosition);
                    }
                    if ((line = line.trim()).isEmpty()) continue;
                    ArrayList<String> lineParts = new ArrayList<String>();
                    for (String s : WHITESPACES.split(line)) {
                        if (s.isEmpty()) continue;
                        lineParts.add(s);
                    }
                    if (lineParts.size() < 2 || (ipBytes = NetUtil.createByteArrayFromIpAddressString((String)lineParts.get(0))) == null) continue;
                    for (int i = 1; i < lineParts.size(); ++i) {
                        List<InetAddress> addresses;
                        String hostname = (String)lineParts.get(i);
                        String hostnameLower = hostname.toLowerCase(Locale.ENGLISH);
                        InetAddress address = InetAddress.getByAddress(hostname, ipBytes);
                        if (address instanceof Inet4Address) {
                            addresses = (ArrayList<InetAddress>)ipv4Entries.get(hostnameLower);
                            if (addresses == null) {
                                addresses = new ArrayList<InetAddress>();
                                ipv4Entries.put(hostnameLower, addresses);
                            }
                        } else {
                            addresses = (List)ipv6Entries.get(hostnameLower);
                            if (addresses == null) {
                                addresses = new ArrayList();
                                ipv6Entries.put(hostnameLower, addresses);
                            }
                        }
                        addresses.add(address);
                    }
                }
                HostsFileEntriesProvider hostsFileEntriesProvider = ipv4Entries.isEmpty() && ipv6Entries.isEmpty() ? EMPTY : new HostsFileEntriesProvider(ipv4Entries, ipv6Entries);
                return hostsFileEntriesProvider;
            } finally {
                try {
                    buff.close();
                } catch (IOException e) {
                    logger.warn("Failed to close a reader", e);
                }
            }
        }

        @Override
        public HostsFileEntriesProvider parseSilently() {
            return this.parseSilently(ParserImpl.locateHostsFile(), Charset.defaultCharset());
        }

        @Override
        public HostsFileEntriesProvider parseSilently(Charset ... charsets) {
            return this.parseSilently(ParserImpl.locateHostsFile(), charsets);
        }

        @Override
        public HostsFileEntriesProvider parseSilently(File file, Charset ... charsets) {
            try {
                return this.parse(file, charsets);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to load and parse hosts file at " + file.getPath(), e);
                }
                return EMPTY;
            }
        }

        private static File locateHostsFile() {
            File hostsFile;
            if (PlatformDependent.isWindows()) {
                hostsFile = new File(System.getenv("SystemRoot") + WINDOWS_HOSTS_FILE_RELATIVE_PATH);
                if (!hostsFile.exists()) {
                    hostsFile = new File("C:\\Windows\\system32\\drivers\\etc\\hosts");
                }
            } else {
                hostsFile = new File(X_PLATFORMS_HOSTS_FILE_PATH);
            }
            return hostsFile;
        }
    }

    public static interface Parser {
        public HostsFileEntriesProvider parse() throws IOException;

        public HostsFileEntriesProvider parse(Charset ... var1) throws IOException;

        public HostsFileEntriesProvider parse(File var1, Charset ... var2) throws IOException;

        public HostsFileEntriesProvider parse(Reader var1) throws IOException;

        public HostsFileEntriesProvider parseSilently();

        public HostsFileEntriesProvider parseSilently(Charset ... var1);

        public HostsFileEntriesProvider parseSilently(File var1, Charset ... var2);
    }
}

