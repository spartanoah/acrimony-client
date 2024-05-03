/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.internal.SuppressJava6Requirement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
final class Java8SslUtils {
    private Java8SslUtils() {
    }

    static List<String> getSniHostNames(SSLParameters sslParameters) {
        List<SNIServerName> names = sslParameters.getServerNames();
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> strings = new ArrayList<String>(names.size());
        for (SNIServerName serverName : names) {
            if (serverName instanceof SNIHostName) {
                strings.add(((SNIHostName)serverName).getAsciiName());
                continue;
            }
            throw new IllegalArgumentException("Only " + SNIHostName.class.getName() + " instances are supported, but found: " + serverName);
        }
        return strings;
    }

    static void setSniHostNames(SSLParameters sslParameters, List<String> names) {
        sslParameters.setServerNames(Java8SslUtils.getSniHostNames(names));
    }

    static List getSniHostNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SNIHostName> sniServerNames = new ArrayList<SNIHostName>(names.size());
        for (String name : names) {
            sniServerNames.add(new SNIHostName(name));
        }
        return sniServerNames;
    }

    static List getSniHostName(byte[] hostname) {
        if (hostname == null || hostname.length == 0) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SNIHostName(hostname));
    }

    static boolean getUseCipherSuitesOrder(SSLParameters sslParameters) {
        return sslParameters.getUseCipherSuitesOrder();
    }

    static void setUseCipherSuitesOrder(SSLParameters sslParameters, boolean useOrder) {
        sslParameters.setUseCipherSuitesOrder(useOrder);
    }

    static void setSNIMatchers(SSLParameters sslParameters, Collection<?> matchers) {
        sslParameters.setSNIMatchers(matchers);
    }

    static boolean checkSniHostnameMatch(Collection<?> matchers, byte[] hostname) {
        if (matchers != null && !matchers.isEmpty()) {
            SNIHostName name = new SNIHostName(hostname);
            for (SNIMatcher matcher : matchers) {
                if (matcher.getType() != 0 || !matcher.matches(name)) continue;
                return true;
            }
            return false;
        }
        return true;
    }
}

