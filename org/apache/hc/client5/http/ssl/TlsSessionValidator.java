/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;
import org.apache.hc.client5.http.ssl.SubjectName;
import org.slf4j.Logger;

final class TlsSessionValidator {
    private final Logger log;

    TlsSessionValidator(Logger log) {
        this.log = log;
    }

    void verifySession(String hostname, SSLSession sslsession, HostnameVerifier hostnameVerifier) throws SSLException {
        Certificate[] certs;
        if (this.log.isDebugEnabled()) {
            this.log.debug("Secure session established");
            this.log.debug(" negotiated protocol: {}", (Object)sslsession.getProtocol());
            this.log.debug(" negotiated cipher suite: {}", (Object)sslsession.getCipherSuite());
            try {
                certs = sslsession.getPeerCertificates();
                Certificate cert = certs[0];
                if (cert instanceof X509Certificate) {
                    X509Certificate x509 = (X509Certificate)cert;
                    X500Principal peer = x509.getSubjectX500Principal();
                    this.log.debug(" peer principal: {}", (Object)peer);
                    Collection<List<?>> altNames1 = x509.getSubjectAlternativeNames();
                    if (altNames1 != null) {
                        ArrayList<String> altNames = new ArrayList<String>();
                        for (List<?> aC : altNames1) {
                            if (aC.isEmpty()) continue;
                            altNames.add((String)aC.get(1));
                        }
                        this.log.debug(" peer alternative names: {}", (Object)altNames);
                    }
                    X500Principal issuer = x509.getIssuerX500Principal();
                    this.log.debug(" issuer principal: {}", (Object)issuer);
                    Collection<List<?>> altNames2 = x509.getIssuerAlternativeNames();
                    if (altNames2 != null) {
                        ArrayList<String> altNames = new ArrayList<String>();
                        for (List<?> aC : altNames2) {
                            if (aC.isEmpty()) continue;
                            altNames.add((String)aC.get(1));
                        }
                        this.log.debug(" issuer alternative names: {}", (Object)altNames);
                    }
                }
            } catch (Exception ignore) {
                // empty catch block
            }
        }
        if (hostnameVerifier != null) {
            certs = sslsession.getPeerCertificates();
            if (certs.length < 1) {
                throw new SSLPeerUnverifiedException("Peer certificate chain is empty");
            }
            Certificate peerCertificate = certs[0];
            if (!(peerCertificate instanceof X509Certificate)) {
                throw new SSLPeerUnverifiedException("Unexpected certificate type: " + peerCertificate.getType());
            }
            X509Certificate x509Certificate = (X509Certificate)peerCertificate;
            if (hostnameVerifier instanceof HttpClientHostnameVerifier) {
                ((HttpClientHostnameVerifier)hostnameVerifier).verify(hostname, x509Certificate);
            } else if (!hostnameVerifier.verify(hostname, sslsession)) {
                List<SubjectName> subjectAlts = DefaultHostnameVerifier.getSubjectAltNames(x509Certificate);
                throw new SSLPeerUnverifiedException("Certificate for <" + hostname + "> doesn't match any " + "of the subject alternative names: " + subjectAlts);
            }
        }
    }
}

