/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.pool;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.SocketModalCloseable;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.DisposalCallback;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Internal
public final class DefaultDisposalCallback<T extends SocketModalCloseable>
implements DisposalCallback<T> {
    private static final Timeout DEFAULT_CLOSE_TIMEOUT = Timeout.ofSeconds(1L);

    @Override
    public void execute(SocketModalCloseable closeable, CloseMode closeMode) {
        Timeout socketTimeout = closeable.getSocketTimeout();
        if (socketTimeout == null || socketTimeout.compareTo(TimeValue.ZERO_MILLISECONDS) <= 0 || socketTimeout.compareTo(DEFAULT_CLOSE_TIMEOUT) > 0) {
            closeable.setSocketTimeout(DEFAULT_CLOSE_TIMEOUT);
        }
        closeable.close(closeMode);
    }
}

