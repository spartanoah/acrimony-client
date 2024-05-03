/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.util;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class CaseInsensitiveNameSet
extends AbstractSet<String> {
    private final Set<String> _namesToMatch;

    private CaseInsensitiveNameSet(Set<String> namesToMatch) {
        this._namesToMatch = namesToMatch;
    }

    public static CaseInsensitiveNameSet construct(Set<String> names0) {
        HashSet<String> namesToMatch = new HashSet<String>(names0);
        for (String name : names0) {
            namesToMatch.add(name.toLowerCase());
        }
        return new CaseInsensitiveNameSet(namesToMatch);
    }

    @Override
    public boolean contains(Object key0) {
        String key = (String)key0;
        if (this._namesToMatch.contains(key)) {
            return true;
        }
        String lc = key.toLowerCase();
        return lc != key && this._namesToMatch.contains(lc);
    }

    @Override
    public Iterator<String> iterator() {
        return this._namesToMatch.iterator();
    }

    @Override
    public int size() {
        return this._namesToMatch.size();
    }
}

