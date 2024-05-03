/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.selector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ContextSelector;

public class BasicContextSelector
implements ContextSelector {
    private static final LoggerContext CONTEXT = new LoggerContext("Default");

    @Override
    public void shutdown(String fqcn, ClassLoader loader, boolean currentContext, boolean allContexts) {
        LoggerContext ctx = this.getContext(fqcn, loader, currentContext);
        if (ctx != null && ctx.isStarted()) {
            ctx.stop(50L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean hasContext(String fqcn, ClassLoader loader, boolean currentContext) {
        LoggerContext ctx = this.getContext(fqcn, loader, currentContext);
        return ctx != null && ctx.isStarted();
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext) {
        LoggerContext ctx = ContextAnchor.THREAD_CONTEXT.get();
        return ctx != null ? ctx : CONTEXT;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext, URI configLocation) {
        LoggerContext ctx = ContextAnchor.THREAD_CONTEXT.get();
        return ctx != null ? ctx : CONTEXT;
    }

    public LoggerContext locateContext(String name, String configLocation) {
        return CONTEXT;
    }

    @Override
    public void removeContext(LoggerContext context) {
    }

    @Override
    public boolean isClassLoaderDependent() {
        return false;
    }

    @Override
    public List<LoggerContext> getLoggerContexts() {
        ArrayList<LoggerContext> list = new ArrayList<LoggerContext>();
        list.add(CONTEXT);
        return Collections.unmodifiableList(list);
    }
}

