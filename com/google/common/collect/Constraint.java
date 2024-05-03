/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
interface Constraint<E> {
    public E checkElement(E var1);

    public String toString();
}

