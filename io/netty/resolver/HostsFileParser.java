/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver;

import io.netty.resolver.HostsFileEntries;
import io.netty.resolver.HostsFileEntriesProvider;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HostsFileParser {
    public static HostsFileEntries parseSilently() {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parseSilently());
    }

    public static HostsFileEntries parseSilently(Charset ... charsets) {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parseSilently(charsets));
    }

    public static HostsFileEntries parse() throws IOException {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parse());
    }

    public static HostsFileEntries parse(File file) throws IOException {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parse(file, new Charset[0]));
    }

    public static HostsFileEntries parse(File file, Charset ... charsets) throws IOException {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parse(file, charsets));
    }

    public static HostsFileEntries parse(Reader reader) throws IOException {
        return HostsFileParser.hostsFileEntries(HostsFileEntriesProvider.parser().parse(reader));
    }

    private HostsFileParser() {
    }

    private static HostsFileEntries hostsFileEntries(HostsFileEntriesProvider provider) {
        return provider == HostsFileEntriesProvider.EMPTY ? HostsFileEntries.EMPTY : new HostsFileEntries(HostsFileParser.toMapWithSingleValue(provider.ipv4Entries()), HostsFileParser.toMapWithSingleValue(provider.ipv6Entries()));
    }

    private static Map<String, ?> toMapWithSingleValue(Map<String, List<InetAddress>> fromMapWithListValue) {
        HashMap<String, InetAddress> result = new HashMap<String, InetAddress>(fromMapWithListValue.size());
        for (Map.Entry<String, List<InetAddress>> entry : fromMapWithListValue.entrySet()) {
            List<InetAddress> value = entry.getValue();
            if (value.isEmpty()) continue;
            result.put(entry.getKey(), value.get(0));
        }
        return result;
    }
}

