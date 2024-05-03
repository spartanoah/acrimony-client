/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.LongCounter;
import io.netty.util.internal.SuppressJava6Requirement;
import java.util.concurrent.atomic.LongAdder;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
final class LongAdderCounter
extends LongAdder
implements LongCounter {
    LongAdderCounter() {
    }

    @Override
    public long value() {
        return this.longValue();
    }
}

