/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

class BasicListHeaderIterator
implements Iterator<Header> {
    private final List<Header> allHeaders;
    private int currentIndex;
    private int lastIndex;
    private final String headerName;

    public BasicListHeaderIterator(List<Header> headers, String name) {
        this.allHeaders = Args.notNull(headers, "Header list");
        this.headerName = name;
        this.currentIndex = this.findNext(-1);
        this.lastIndex = -1;
    }

    protected int findNext(int pos) {
        int from = pos;
        if (from < -1) {
            return -1;
        }
        int to = this.allHeaders.size() - 1;
        boolean found = false;
        while (!found && from < to) {
            found = this.filterHeader(++from);
        }
        return found ? from : -1;
    }

    private boolean filterHeader(int index) {
        if (this.headerName == null) {
            return true;
        }
        String name = this.allHeaders.get(index).getName();
        return this.headerName.equalsIgnoreCase(name);
    }

    @Override
    public boolean hasNext() {
        return this.currentIndex >= 0;
    }

    @Override
    public Header next() throws NoSuchElementException {
        int current = this.currentIndex;
        if (current < 0) {
            throw new NoSuchElementException("Iteration already finished.");
        }
        this.lastIndex = current;
        this.currentIndex = this.findNext(current);
        return this.allHeaders.get(current);
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        Asserts.check(this.lastIndex >= 0, "No header to remove");
        this.allHeaders.remove(this.lastIndex);
        this.lastIndex = -1;
        --this.currentIndex;
    }
}

