/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import org.apache.hc.core5.concurrent.Cancellable;

public interface Command
extends Cancellable {

    public static enum Priority {
        NORMAL,
        IMMEDIATE;

    }
}

