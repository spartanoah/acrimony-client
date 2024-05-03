/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import org.apache.hc.client5.http.psl.DomainType;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.ssl.DistinguishedNameParser;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;
import org.apache.hc.client5.http.ssl.SubjectName;
import org.apache.hc.client5.http.utils.DnsUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class DefaultHostnameVerifier
implements HttpClientHostnameVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHostnameVerifier.class);
    private final PublicSuffixMatcher publicSuffixMatcher;

    public DefaultHostnameVerifier(PublicSuffixMatcher publicSuffixMatcher) {
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public DefaultHostnameVerifier() {
        this(null);
    }

    @Override
    public boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certs = session.getPeerCertificates();
            X509Certificate x509 = (X509Certificate)certs[0];
            this.verify(host, x509);
            return true;
        } catch (SSLException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(ex.getMessage(), ex);
            }
            return false;
        }
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        HostNameType hostType = DefaultHostnameVerifier.determineHostFormat(host);
        List<SubjectName> subjectAlts = DefaultHostnameVerifier.getSubjectAltNames(cert);
        if (subjectAlts != null && !subjectAlts.isEmpty()) {
            switch (hostType) {
                case IPv4: {
                    DefaultHostnameVerifier.matchIPAddress(host, subjectAlts);
                    break;
                }
                case IPv6: {
                    DefaultHostnameVerifier.matchIPv6Address(host, subjectAlts);
                    break;
                }
                default: {
                    DefaultHostnameVerifier.matchDNSName(host, subjectAlts, this.publicSuffixMatcher);
                    break;
                }
            }
        } else {
            X500Principal subjectPrincipal = cert.getSubjectX500Principal();
            String cn = DefaultHostnameVerifier.extractCN(subjectPrincipal.getName("RFC2253"));
            if (cn == null) {
                throw new SSLException("Certificate subject for <" + host + "> doesn't contain " + "a common name and does not have alternative names");
            }
            DefaultHostnameVerifier.matchCN(host, cn, this.publicSuffixMatcher);
        }
    }

    static void matchIPAddress(String host, List<SubjectName> subjectAlts) throws SSLException {
        for (int i = 0; i < subjectAlts.size(); ++i) {
            SubjectName subjectAlt = subjectAlts.get(i);
            if (subjectAlt.getType() != 7 || !host.equals(subjectAlt.getValue())) continue;
            return;
        }
        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any " + "of the subject alternative names: " + subjectAlts);
    }

    static void matchIPv6Address(String host, List<SubjectName> subjectAlts) throws SSLException {
        String normalisedHost = DefaultHostnameVerifier.normaliseAddress(host);
        for (int i = 0; i < subjectAlts.size(); ++i) {
            String normalizedSubjectAlt;
            SubjectName subjectAlt = subjectAlts.get(i);
            if (subjectAlt.getType() != 7 || !normalisedHost.equals(normalizedSubjectAlt = DefaultHostnameVerifier.normaliseAddress(subjectAlt.getValue()))) continue;
            return;
        }
        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any " + "of the subject alternative names: " + subjectAlts);
    }

    static void matchDNSName(String host, List<SubjectName> subjectAlts, PublicSuffixMatcher publicSuffixMatcher) throws SSLException {
        String normalizedHost = DnsUtils.normalize(host);
        for (int i = 0; i < subjectAlts.size(); ++i) {
            String normalizedSubjectAlt;
            SubjectName subjectAlt = subjectAlts.get(i);
            if (subjectAlt.getType() != 2 || !DefaultHostnameVerifier.matchIdentityStrict(normalizedHost, normalizedSubjectAlt = DnsUtils.normalize(subjectAlt.getValue()), publicSuffixMatcher)) continue;
            return;
        }
        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any " + "of the subject alternative names: " + subjectAlts);
    }

    static void matchCN(String host, String cn, PublicSuffixMatcher publicSuffixMatcher) throws SSLException {
        String normalizedCn;
        String normalizedHost = DnsUtils.normalize(host);
        if (!DefaultHostnameVerifier.matchIdentityStrict(normalizedHost, normalizedCn = DnsUtils.normalize(cn), publicSuffixMatcher)) {
            throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match " + "common name of the certificate subject: " + cn);
        }
    }

    static boolean matchDomainRoot(String host, String domainRoot) {
        if (domainRoot == null) {
            return false;
        }
        return host.endsWith(domainRoot) && (host.length() == domainRoot.length() || host.charAt(host.length() - domainRoot.length() - 1) == '.');
    }

    private static boolean matchIdentity(String host, String identity, PublicSuffixMatcher publicSuffixMatcher, DomainType domainType, boolean strict) {
        if (publicSuffixMatcher != null && host.contains(".") && !DefaultHostnameVerifier.matchDomainRoot(host, publicSuffixMatcher.getDomainRoot(identity, domainType))) {
            return false;
        }
        int asteriskIdx = identity.indexOf(42);
        if (asteriskIdx != -1) {
            String remainder;
            String prefix = identity.substring(0, asteriskIdx);
            String suffix = identity.substring(asteriskIdx + 1);
            if (!prefix.isEmpty() && !host.startsWith(prefix)) {
                return false;
            }
            if (!suffix.isEmpty() && !host.endsWith(suffix)) {
                return false;
            }
            return !strict || !(remainder = host.substring(prefix.length(), host.length() - suffix.length())).contains(".");
        }
        return host.equalsIgnoreCase(identity);
    }

    static boolean matchIdentity(String host, String identity, PublicSuffixMatcher publicSuffixMatcher) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, publicSuffixMatcher, null, false);
    }

    static boolean matchIdentity(String host, String identity) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, null, null, false);
    }

    static boolean matchIdentityStrict(String host, String identity, PublicSuffixMatcher publicSuffixMatcher) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, publicSuffixMatcher, null, true);
    }

    static boolean matchIdentityStrict(String host, String identity) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, null, null, true);
    }

    static boolean matchIdentity(String host, String identity, PublicSuffixMatcher publicSuffixMatcher, DomainType domainType) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, publicSuffixMatcher, domainType, false);
    }

    static boolean matchIdentityStrict(String host, String identity, PublicSuffixMatcher publicSuffixMatcher, DomainType domainType) {
        return DefaultHostnameVerifier.matchIdentity(host, identity, publicSuffixMatcher, domainType, true);
    }

    static String extractCN(String subjectPrincipal) throws SSLException {
        if (subjectPrincipal == null) {
            return null;
        }
        List<NameValuePair> attributes = DistinguishedNameParser.INSTANCE.parse(subjectPrincipal);
        for (NameValuePair attribute : attributes) {
            if (TextUtils.isBlank(attribute.getName()) || attribute.getValue() == null) {
                throw new SSLException(subjectPrincipal + " is not a valid X500 distinguished name");
            }
            if (!attribute.getName().equalsIgnoreCase("cn")) continue;
            return attribute.getValue();
        }
        return null;
    }

    static HostNameType determineHostFormat(String host) {
        if (InetAddressUtils.isIPv4Address(host)) {
            return HostNameType.IPv4;
        }
        String s = host;
        if (s.startsWith("[") && s.endsWith("]")) {
            s = host.substring(1, host.length() - 1);
        }
        if (InetAddressUtils.isIPv6Address(s)) {
            return HostNameType.IPv6;
        }
        return HostNameType.DNS;
    }

    static List<SubjectName> getSubjectAltNames(X509Certificate cert) {
        try {
            Collection<List<?>> entries = cert.getSubjectAlternativeNames();
            if (entries == null) {
                return Collections.emptyList();
            }
            ArrayList<SubjectName> result = new ArrayList<SubjectName>();
            for (List<?> entry : entries) {
                Integer type = entry.size() >= 2 ? (Integer)entry.get(0) : null;
                if (type == null || type != 2 && type != 7) continue;
                Object o = entry.get(1);
                if (o instanceof String) {
                    result.add(new SubjectName((String)o, type));
                    continue;
                }
                if (!(o instanceof byte[])) continue;
            }
            return result;
        } catch (CertificateParsingException ignore) {
            return Collections.emptyList();
        }
    }

    static String normaliseAddress(String hostname) {
        if (hostname == null) {
            return hostname;
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(hostname);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException unexpected) {
            return hostname;
        }
    }

    static enum HostNameType {
        IPv4(7),
        IPv6(7),
        DNS(2);

        final int subjectType;

        private HostNameType(int subjectType) {
            this.subjectType = subjectType;
        }
    }
}

