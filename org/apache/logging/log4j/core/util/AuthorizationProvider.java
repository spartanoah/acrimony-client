/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.net.URLConnection;

public interface AuthorizationProvider {
    public void addAuthorization(URLConnection var1);
}

