/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.HpackUtil;
import io.netty.util.internal.ObjectUtil;

class HpackHeaderField {
    static final int HEADER_ENTRY_OVERHEAD = 32;
    final CharSequence name;
    final CharSequence value;

    static long sizeOf(CharSequence name, CharSequence value) {
        return name.length() + value.length() + 32;
    }

    HpackHeaderField(CharSequence name, CharSequence value) {
        this.name = ObjectUtil.checkNotNull(name, "name");
        this.value = ObjectUtil.checkNotNull(value, "value");
    }

    final int size() {
        return this.name.length() + this.value.length() + 32;
    }

    public final boolean equalsForTest(HpackHeaderField other) {
        return HpackUtil.equalsVariableTime(this.name, other.name) && HpackUtil.equalsVariableTime(this.value, other.value);
    }

    public String toString() {
        return this.name + ": " + this.value;
    }
}

