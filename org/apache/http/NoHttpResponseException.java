/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http;

import java.io.IOException;

public class NoHttpResponseException
extends IOException {
    private static final long serialVersionUID = -7658940387386078766L;

    public NoHttpResponseException(String message) {
        super(message);
    }
}

