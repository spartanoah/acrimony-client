/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.Recycler;
import io.netty.util.internal.ObjectUtil;

public abstract class ObjectPool<T> {
    ObjectPool() {
    }

    public abstract T get();

    public static <T> ObjectPool<T> newPool(ObjectCreator<T> creator) {
        return new RecyclerObjectPool<T>(ObjectUtil.checkNotNull(creator, "creator"));
    }

    private static final class RecyclerObjectPool<T>
    extends ObjectPool<T> {
        private final Recycler<T> recycler;

        RecyclerObjectPool(final ObjectCreator<T> creator) {
            this.recycler = new Recycler<T>(){

                @Override
                protected T newObject(Recycler.Handle<T> handle) {
                    return creator.newObject(handle);
                }
            };
        }

        @Override
        public T get() {
            return this.recycler.get();
        }
    }

    public static interface ObjectCreator<T> {
        public T newObject(Handle<T> var1);
    }

    public static interface Handle<T> {
        public void recycle(T var1);
    }
}

