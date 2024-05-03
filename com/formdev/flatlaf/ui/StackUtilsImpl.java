/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.lang.StackWalker
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.StackUtils;
import java.util.function.BiPredicate;

class StackUtilsImpl
extends StackUtils {
    StackUtilsImpl() {
    }

    @Override
    boolean wasInvokedFromImpl(BiPredicate<String, String> predicate, int limit) {
        return (Boolean)StackWalker.getInstance().walk(stream -> {
            if (limit > 0) {
                stream = stream.limit(limit + 2);
            }
            return stream.anyMatch(f -> predicate.test(f.getClassName(), f.getMethodName()));
        });
    }
}

