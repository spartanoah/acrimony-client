/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import org.apache.hc.core5.util.Args;

public class InetAddressUtils {
    private static final String IPV4_BASIC_PATTERN_STRING = "(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    private static final Pattern IPV4_MAPPED_IPV6_PATTERN = Pattern.compile("^::[fF]{4}:(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)::(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$");
    private static final char COLON_CHAR = ':';
    private static final int MAX_COLON_COUNT = 7;

    private InetAddressUtils() {
    }

    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv4MappedIPv64Address(String input) {
        return IPV4_MAPPED_IPV6_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(String input) {
        int colonCount = 0;
        for (int i = 0; i < input.length(); ++i) {
            if (input.charAt(i) != ':') continue;
            ++colonCount;
        }
        return colonCount <= 7 && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(String input) {
        return InetAddressUtils.isIPv6StdAddress(input) || InetAddressUtils.isIPv6HexCompressedAddress(input);
    }

    public static void formatAddress(StringBuilder buffer, SocketAddress socketAddress) {
        Args.notNull(buffer, "buffer");
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress socketaddr = (InetSocketAddress)socketAddress;
            InetAddress inetaddr = socketaddr.getAddress();
            if (inetaddr != null) {
                buffer.append(inetaddr.getHostAddress()).append(':').append(socketaddr.getPort());
            } else {
                buffer.append(socketAddress);
            }
        } else {
            buffer.append(socketAddress);
        }
    }

    public static String getCanonicalLocalHostName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getCanonicalHostName();
        } catch (UnknownHostException ex) {
            return "localhost";
        }
    }
}

