/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import javax.net.ssl.SSLEngine;
import org.apache.hc.core5.net.NamedEndpoint;

public interface SSLSessionInitializer {
    public void initialize(NamedEndpoint var1, SSLEngine var2);
}

