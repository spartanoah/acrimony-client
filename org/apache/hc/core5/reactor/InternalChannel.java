/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.util.Timeout;

abstract class InternalChannel
implements ModalCloseable {
    InternalChannel() {
    }

    abstract void onIOEvent(int var1) throws IOException;

    abstract void onTimeout(Timeout var1) throws IOException;

    abstract void onException(Exception var1);

    abstract Timeout getTimeout();

    abstract long getLastEventTime();

    final void handleIOEvent(int ops) {
        try {
            this.onIOEvent(ops);
        } catch (CancelledKeyException ex) {
            this.close(CloseMode.GRACEFUL);
        } catch (Exception ex) {
            this.onException(ex);
            this.close(CloseMode.IMMEDIATE);
        }
    }

    final boolean checkTimeout(long currentTimeMillis) {
        Timeout timeout = this.getTimeout();
        if (!timeout.isDisabled()) {
            long timeoutMillis = timeout.toMilliseconds();
            long deadlineMillis = this.getLastEventTime() + timeoutMillis;
            if (currentTimeMillis > deadlineMillis) {
                try {
                    this.onTimeout(timeout);
                } catch (CancelledKeyException ex) {
                    this.close(CloseMode.GRACEFUL);
                } catch (Exception ex) {
                    this.onException(ex);
                    this.close(CloseMode.IMMEDIATE);
                }
                return false;
            }
        }
        return true;
    }
}

