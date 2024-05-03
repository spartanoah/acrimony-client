/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.AbstractConstant;
import io.netty.util.ConstantPool;
import io.netty.util.internal.ObjectUtil;

public class SslContextOption<T>
extends AbstractConstant<SslContextOption<T>> {
    private static final ConstantPool<SslContextOption<Object>> pool = new ConstantPool<SslContextOption<Object>>(){

        @Override
        protected SslContextOption<Object> newConstant(int id, String name) {
            return new SslContextOption<Object>(id, name);
        }
    };

    public static <T> SslContextOption<T> valueOf(String name) {
        return pool.valueOf(name);
    }

    public static <T> SslContextOption<T> valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return pool.valueOf(firstNameComponent, secondNameComponent);
    }

    public static boolean exists(String name) {
        return pool.exists(name);
    }

    private SslContextOption(int id, String name) {
        super(id, name);
    }

    protected SslContextOption(String name) {
        this(pool.nextId(), name);
    }

    public void validate(T value) {
        ObjectUtil.checkNotNull(value, "value");
    }
}

