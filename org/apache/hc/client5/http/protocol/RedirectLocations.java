/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RedirectLocations {
    private final Set<URI> unique = new HashSet<URI>();
    private final List<URI> all = new ArrayList<URI>();

    public boolean contains(URI uri) {
        return this.unique.contains(uri);
    }

    public void add(URI uri) {
        this.unique.add(uri);
        this.all.add(uri);
    }

    public List<URI> getAll() {
        return new ArrayList<URI>(this.all);
    }

    public URI get(int index) {
        return this.all.get(index);
    }

    public int size() {
        return this.all.size();
    }

    public void clear() {
        this.unique.clear();
        this.all.clear();
    }
}

