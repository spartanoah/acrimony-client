/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLInitializationException;

public final class SSLContexts {
    private SSLContexts() {
    }

    public static SSLContext createDefault() throws SSLInitializationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            return sslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        }
    }

    public static SSLContext createSystemDefault() throws SSLInitializationException {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException ex) {
            return SSLContexts.createDefault();
        }
    }

    public static SSLContextBuilder custom() {
        return SSLContextBuilder.create();
    }
}

