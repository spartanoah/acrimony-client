/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import org.apache.hc.core5.util.Args;

final class SubjectName {
    static final int DNS = 2;
    static final int IP = 7;
    private final String value;
    private final int type;

    static SubjectName IP(String value) {
        return new SubjectName(value, 7);
    }

    static SubjectName DNS(String value) {
        return new SubjectName(value, 2);
    }

    SubjectName(String value, int type) {
        this.value = Args.notNull(value, "Value");
        this.type = Args.positive(type, "Type");
    }

    public int getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return this.value;
    }
}

