/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;

@Internal
public interface DisposalCallback<T extends ModalCloseable> {
    public void execute(T var1, CloseMode var2);
}

