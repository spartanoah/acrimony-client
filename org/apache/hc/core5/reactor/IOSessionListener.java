/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.reactor.IOSession;

@Internal
public interface IOSessionListener {
    public void connected(IOSession var1);

    public void startTls(IOSession var1);

    public void inputReady(IOSession var1);

    public void outputReady(IOSession var1);

    public void timeout(IOSession var1);

    public void exception(IOSession var1, Exception var2);

    public void disconnected(IOSession var1);
}

