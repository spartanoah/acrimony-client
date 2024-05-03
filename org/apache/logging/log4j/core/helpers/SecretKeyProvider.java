/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    public SecretKey getSecretKey();
}

