/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.pool.ConnPool;
import org.apache.hc.core5.pool.ConnPoolControl;

public interface ManagedConnPool<T, C extends ModalCloseable>
extends ConnPool<T, C>,
ConnPoolControl<T>,
ModalCloseable {
}

