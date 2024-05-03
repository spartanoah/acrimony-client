/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.collect.Multiset;
import java.util.SortedSet;

interface SortedMultisetBridge<E>
extends Multiset<E> {
    @Override
    public SortedSet<E> elementSet();
}

