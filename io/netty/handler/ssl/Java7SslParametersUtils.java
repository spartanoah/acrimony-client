/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.internal.SuppressJava6Requirement;
import java.security.AlgorithmConstraints;
import javax.net.ssl.SSLParameters;

final class Java7SslParametersUtils {
    private Java7SslParametersUtils() {
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    static void setAlgorithmConstraints(SSLParameters sslParameters, Object algorithmConstraints) {
        sslParameters.setAlgorithmConstraints((AlgorithmConstraints)algorithmConstraints);
    }
}

