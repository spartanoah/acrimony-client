/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.selector;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextShutdownAware;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class ClassLoaderContextSelector
implements ContextSelector,
LoggerContextShutdownAware {
    private static final AtomicReference<LoggerContext> DEFAULT_CONTEXT = new AtomicReference();
    protected static final StatusLogger LOGGER = StatusLogger.getLogger();
    protected static final ConcurrentMap<String, AtomicReference<WeakReference<LoggerContext>>> CONTEXT_MAP = new ConcurrentHashMap<String, AtomicReference<WeakReference<LoggerContext>>>();

    @Override
    public void shutdown(String fqcn, ClassLoader loader, boolean currentContext, boolean allContexts) {
        LoggerContext ctx = null;
        if (currentContext) {
            ctx = ContextAnchor.THREAD_CONTEXT.get();
        } else if (loader != null) {
            ctx = this.findContext(loader);
        } else {
            Class<?> clazz = StackLocatorUtil.getCallerClass(fqcn);
            if (clazz != null) {
                ctx = this.findContext(clazz.getClassLoader());
            }
            if (ctx == null) {
                ctx = ContextAnchor.THREAD_CONTEXT.get();
            }
        }
        if (ctx != null) {
            ctx.stop(50L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void contextShutdown(org.apache.logging.log4j.spi.LoggerContext loggerContext) {
        if (loggerContext instanceof LoggerContext) {
            this.removeContext((LoggerContext)loggerContext);
        }
    }

    @Override
    public boolean hasContext(String fqcn, ClassLoader loader, boolean currentContext) {
        Class<?> clazz;
        LoggerContext ctx = currentContext ? ContextAnchor.THREAD_CONTEXT.get() : (loader != null ? this.findContext(loader) : ((clazz = StackLocatorUtil.getCallerClass(fqcn)) != null ? this.findContext(clazz.getClassLoader()) : ContextAnchor.THREAD_CONTEXT.get()));
        return ctx != null && ctx.isStarted();
    }

    private LoggerContext findContext(ClassLoader loaderOrNull) {
        ClassLoader loader = loaderOrNull != null ? loaderOrNull : ClassLoader.getSystemClassLoader();
        String name = this.toContextMapKey(loader);
        AtomicReference ref = (AtomicReference)CONTEXT_MAP.get(name);
        if (ref != null) {
            WeakReference weakRef = (WeakReference)ref.get();
            return (LoggerContext)weakRef.get();
        }
        return null;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext) {
        return this.getContext(fqcn, loader, currentContext, null);
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext, URI configLocation) {
        return this.getContext(fqcn, loader, null, currentContext, configLocation);
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Map.Entry<String, Object> entry, boolean currentContext, URI configLocation) {
        if (currentContext) {
            LoggerContext ctx = ContextAnchor.THREAD_CONTEXT.get();
            if (ctx != null) {
                return ctx;
            }
            return this.getDefault();
        }
        if (loader != null) {
            return this.locateContext(loader, entry, configLocation);
        }
        Class<?> clazz = StackLocatorUtil.getCallerClass(fqcn);
        if (clazz != null) {
            return this.locateContext(clazz.getClassLoader(), entry, configLocation);
        }
        LoggerContext lc = ContextAnchor.THREAD_CONTEXT.get();
        if (lc != null) {
            return lc;
        }
        return this.getDefault();
    }

    @Override
    public void removeContext(LoggerContext context) {
        for (Map.Entry entry : CONTEXT_MAP.entrySet()) {
            LoggerContext ctx = (LoggerContext)((WeakReference)((AtomicReference)entry.getValue()).get()).get();
            if (ctx != context) continue;
            CONTEXT_MAP.remove(entry.getKey());
        }
    }

    @Override
    public boolean isClassLoaderDependent() {
        return true;
    }

    @Override
    public List<LoggerContext> getLoggerContexts() {
        ArrayList<LoggerContext> list = new ArrayList<LoggerContext>();
        Collection coll = CONTEXT_MAP.values();
        for (AtomicReference ref : coll) {
            LoggerContext ctx = (LoggerContext)((WeakReference)ref.get()).get();
            if (ctx == null) continue;
            list.add(ctx);
        }
        return Collections.unmodifiableList(list);
    }

    private LoggerContext locateContext(ClassLoader loaderOrNull, Map.Entry<String, Object> entry, URI configLocation) {
        ClassLoader loader = loaderOrNull != null ? loaderOrNull : ClassLoader.getSystemClassLoader();
        String name = this.toContextMapKey(loader);
        AtomicReference ref = (AtomicReference)CONTEXT_MAP.get(name);
        if (ref == null) {
            LoggerContext newContext;
            if (configLocation == null) {
                for (ClassLoader parent = loader.getParent(); parent != null; parent = parent.getParent()) {
                    WeakReference r;
                    LoggerContext ctx;
                    ref = (AtomicReference)CONTEXT_MAP.get(this.toContextMapKey(parent));
                    if (ref == null || (ctx = (LoggerContext)(r = (WeakReference)ref.get()).get()) == null) continue;
                    return ctx;
                }
            }
            LoggerContext ctx = this.createContext(name, configLocation);
            if (entry != null) {
                ctx.putObject(entry.getKey(), entry.getValue());
            }
            if ((newContext = (LoggerContext)((WeakReference)CONTEXT_MAP.computeIfAbsent(name, k -> new AtomicReference<WeakReference<LoggerContext>>(new WeakReference<LoggerContext>(ctx))).get()).get()) == ctx) {
                ctx.addShutdownListener(this);
            }
            return newContext;
        }
        WeakReference weakRef = (WeakReference)ref.get();
        LoggerContext ctx = (LoggerContext)weakRef.get();
        if (ctx != null) {
            if (entry != null && ctx.getObject(entry.getKey()) == null) {
                ctx.putObject(entry.getKey(), entry.getValue());
            }
            if (ctx.getConfigLocation() == null && configLocation != null) {
                LOGGER.debug("Setting configuration to {}", (Object)configLocation);
                ctx.setConfigLocation(configLocation);
            } else if (ctx.getConfigLocation() != null && configLocation != null && !ctx.getConfigLocation().equals(configLocation)) {
                LOGGER.warn("locateContext called with URI {}. Existing LoggerContext has URI {}", (Object)configLocation, (Object)ctx.getConfigLocation());
            }
            return ctx;
        }
        ctx = this.createContext(name, configLocation);
        if (entry != null) {
            ctx.putObject(entry.getKey(), entry.getValue());
        }
        ref.compareAndSet(weakRef, new WeakReference<LoggerContext>(ctx));
        return ctx;
    }

    protected LoggerContext createContext(String name, URI configLocation) {
        return new LoggerContext(name, null, configLocation);
    }

    protected String toContextMapKey(ClassLoader loader) {
        return Integer.toHexString(System.identityHashCode(loader));
    }

    protected LoggerContext getDefault() {
        LoggerContext ctx = DEFAULT_CONTEXT.get();
        if (ctx != null) {
            return ctx;
        }
        DEFAULT_CONTEXT.compareAndSet(null, this.createContext(this.defaultContextName(), null));
        return DEFAULT_CONTEXT.get();
    }

    protected String defaultContextName() {
        return "Default";
    }
}

