/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.internal.EmptyArrays;
import java.util.Arrays;

final class OpenSslSessionId {
    private final byte[] id;
    private final int hashCode;
    static final OpenSslSessionId NULL_ID = new OpenSslSessionId(EmptyArrays.EMPTY_BYTES);

    OpenSslSessionId(byte[] id) {
        this.id = id;
        this.hashCode = Arrays.hashCode(id);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenSslSessionId)) {
            return false;
        }
        return Arrays.equals(this.id, ((OpenSslSessionId)o).id);
    }

    public String toString() {
        return "OpenSslSessionId{id=" + Arrays.toString(this.id) + '}';
    }

    public int hashCode() {
        return this.hashCode;
    }

    byte[] cloneBytes() {
        return (byte[])this.id.clone();
    }
}

