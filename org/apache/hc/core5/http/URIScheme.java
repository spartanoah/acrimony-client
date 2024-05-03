/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.util.Args;

public enum URIScheme {
    HTTP("http"),
    HTTPS("https");

    public final String id;

    private URIScheme(String id) {
        this.id = Args.notBlank(id, "id");
    }

    public String getId() {
        return this.id;
    }

    public boolean same(String scheme) {
        return this.id.equalsIgnoreCase(scheme);
    }

    public String toString() {
        return this.id;
    }
}

