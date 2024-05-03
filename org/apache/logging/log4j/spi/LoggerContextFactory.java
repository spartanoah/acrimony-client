/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.spi;

import java.net.URI;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.Terminable;

public interface LoggerContextFactory {
    default public void shutdown(String fqcn, ClassLoader loader, boolean currentContext, boolean allContexts) {
        LoggerContext ctx;
        if (this.hasContext(fqcn, loader, currentContext) && (ctx = this.getContext(fqcn, loader, null, currentContext)) instanceof Terminable) {
            ((Terminable)((Object)ctx)).terminate();
        }
    }

    default public boolean hasContext(String fqcn, ClassLoader loader, boolean currentContext) {
        return false;
    }

    public LoggerContext getContext(String var1, ClassLoader var2, Object var3, boolean var4);

    public LoggerContext getContext(String var1, ClassLoader var2, Object var3, boolean var4, URI var5, String var6);

    public void removeContext(LoggerContext var1);

    default public boolean isClassLoaderDependent() {
        return true;
    }
}

