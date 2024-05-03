/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver;

import io.netty.resolver.AddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.Closeable;
import java.net.SocketAddress;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AddressResolverGroup<T extends SocketAddress>
implements Closeable {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AddressResolverGroup.class);
    private final Map<EventExecutor, AddressResolver<T>> resolvers = new IdentityHashMap<EventExecutor, AddressResolver<T>>();
    private final Map<EventExecutor, GenericFutureListener<Future<Object>>> executorTerminationListeners = new IdentityHashMap<EventExecutor, GenericFutureListener<Future<Object>>>();

    protected AddressResolverGroup() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AddressResolver<T> getResolver(final EventExecutor executor) {
        AddressResolver<T> r;
        ObjectUtil.checkNotNull(executor, "executor");
        if (executor.isShuttingDown()) {
            throw new IllegalStateException("executor not accepting a task");
        }
        Map<EventExecutor, AddressResolver<T>> map = this.resolvers;
        synchronized (map) {
            r = this.resolvers.get(executor);
            if (r == null) {
                AddressResolver<T> newResolver;
                try {
                    newResolver = this.newResolver(executor);
                } catch (Exception e) {
                    throw new IllegalStateException("failed to create a new resolver", e);
                }
                this.resolvers.put(executor, newResolver);
                FutureListener<Object> terminationListener = new FutureListener<Object>(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void operationComplete(Future<Object> future) {
                        Map map = AddressResolverGroup.this.resolvers;
                        synchronized (map) {
                            AddressResolverGroup.this.resolvers.remove(executor);
                            AddressResolverGroup.this.executorTerminationListeners.remove(executor);
                        }
                        newResolver.close();
                    }
                };
                this.executorTerminationListeners.put(executor, (GenericFutureListener<Future<Object>>)terminationListener);
                executor.terminationFuture().addListener(terminationListener);
                r = newResolver;
            }
        }
        return r;
    }

    protected abstract AddressResolver<T> newResolver(EventExecutor var1) throws Exception;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        Object[] objectArray = this.resolvers;
        synchronized (this.resolvers) {
            AddressResolver[] rArray = this.resolvers.values().toArray(new AddressResolver[0]);
            this.resolvers.clear();
            Map.Entry[] listeners = this.executorTerminationListeners.entrySet().toArray(new Map.Entry[0]);
            this.executorTerminationListeners.clear();
            // ** MonitorExit[var3_1] (shouldn't be in output)
            for (Map.Entry entry : listeners) {
                ((EventExecutor)entry.getKey()).terminationFuture().removeListener((GenericFutureListener)entry.getValue());
            }
            for (AddressResolver r : rArray) {
                try {
                    r.close();
                } catch (Throwable t) {
                    logger.warn("Failed to close a resolver:", t);
                }
            }
            return;
        }
    }
}

