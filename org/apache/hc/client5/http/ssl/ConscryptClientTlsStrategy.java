/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.ssl.H2TlsSupport
 *  org.conscrypt.Conscrypt
 */
package org.apache.hc.client5.http.ssl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.apache.hc.client5.http.ssl.AbstractClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.ssl.H2TlsSupport;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContexts;
import org.conscrypt.Conscrypt;

@Contract(threading=ThreadingBehavior.STATELESS)
public class ConscryptClientTlsStrategy
extends AbstractClientTlsStrategy {
    public static TlsStrategy getDefault() {
        return new ConscryptClientTlsStrategy(SSLContexts.createDefault(), HttpsSupport.getDefaultHostnameVerifier());
    }

    public static TlsStrategy getSystemDefault() {
        return new ConscryptClientTlsStrategy(SSLContexts.createSystemDefault(), HttpsSupport.getSystemProtocols(), HttpsSupport.getSystemCipherSuits(), SSLBufferMode.STATIC, HttpsSupport.getDefaultHostnameVerifier());
    }

    public ConscryptClientTlsStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, SSLBufferMode sslBufferManagement, HostnameVerifier hostnameVerifier) {
        super(sslContext, supportedProtocols, supportedCipherSuites, sslBufferManagement, hostnameVerifier);
    }

    public ConscryptClientTlsStrategy(SSLContext sslcontext, HostnameVerifier hostnameVerifier) {
        this(sslcontext, null, null, SSLBufferMode.STATIC, hostnameVerifier);
    }

    public ConscryptClientTlsStrategy(SSLContext sslcontext) {
        this(sslcontext, HttpsSupport.getDefaultHostnameVerifier());
    }

    @Override
    void applyParameters(SSLEngine sslEngine, SSLParameters sslParameters, String[] appProtocols) {
        if (Conscrypt.isConscrypt((SSLEngine)sslEngine)) {
            sslEngine.setSSLParameters(sslParameters);
            Conscrypt.setApplicationProtocols((SSLEngine)sslEngine, (String[])appProtocols);
        } else {
            H2TlsSupport.setApplicationProtocols((SSLParameters)sslParameters, (String[])appProtocols);
            sslEngine.setSSLParameters(sslParameters);
        }
    }

    @Override
    TlsDetails createTlsDetails(SSLEngine sslEngine) {
        if (Conscrypt.isConscrypt((SSLEngine)sslEngine)) {
            return new TlsDetails(sslEngine.getSession(), Conscrypt.getApplicationProtocol((SSLEngine)sslEngine));
        }
        return null;
    }

    public static boolean isSupported() {
        try {
            Class<?> clazz = Class.forName("org.conscrypt.Conscrypt");
            Method method = clazz.getMethod("isAvailable", new Class[0]);
            return (Boolean)method.invoke(null, new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return false;
        }
    }
}

