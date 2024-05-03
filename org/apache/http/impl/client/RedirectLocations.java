/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.http.annotation.NotThreadSafe;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@NotThreadSafe
public class RedirectLocations
extends AbstractList<Object> {
    private final Set<URI> unique = new HashSet<URI>();
    private final List<URI> all = new ArrayList<URI>();

    public boolean contains(URI uri) {
        return this.unique.contains(uri);
    }

    public void add(URI uri) {
        this.unique.add(uri);
        this.all.add(uri);
    }

    public boolean remove(URI uri) {
        boolean removed = this.unique.remove(uri);
        if (removed) {
            Iterator<URI> it = this.all.iterator();
            while (it.hasNext()) {
                URI current = it.next();
                if (!current.equals(uri)) continue;
                it.remove();
            }
        }
        return removed;
    }

    public List<URI> getAll() {
        return new ArrayList<URI>(this.all);
    }

    @Override
    public URI get(int index) {
        return this.all.get(index);
    }

    @Override
    public int size() {
        return this.all.size();
    }

    @Override
    public Object set(int index, Object element) {
        URI removed = this.all.set(index, (URI)element);
        this.unique.remove(removed);
        this.unique.add((URI)element);
        if (this.all.size() != this.unique.size()) {
            this.unique.addAll(this.all);
        }
        return removed;
    }

    @Override
    public void add(int index, Object element) {
        this.all.add(index, (URI)element);
        this.unique.add((URI)element);
    }

    @Override
    public URI remove(int index) {
        URI removed = this.all.remove(index);
        this.unique.remove(removed);
        if (this.all.size() != this.unique.size()) {
            this.unique.addAll(this.all);
        }
        return removed;
    }

    @Override
    public boolean contains(Object o) {
        return this.unique.contains(o);
    }
}

