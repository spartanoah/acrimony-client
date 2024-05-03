/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.io.IOException;

public class UnsupportedSchemeException
extends IOException {
    private static final long serialVersionUID = 3597127619218687636L;

    public UnsupportedSchemeException(String message) {
        super(message);
    }
}

