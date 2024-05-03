/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.Args;

public class BasicHeaderIterator
implements Iterator<Header> {
    private final Header[] allHeaders;
    private int currentIndex;
    private final String headerName;

    public BasicHeaderIterator(Header[] headers, String name) {
        this.allHeaders = Args.notNull(headers, "Header array");
        this.headerName = name;
        this.currentIndex = this.findNext(-1);
    }

    private int findNext(int pos) {
        int from = pos;
        if (from < -1) {
            return -1;
        }
        int to = this.allHeaders.length - 1;
        boolean found = false;
        while (!found && from < to) {
            found = this.filterHeader(++from);
        }
        return found ? from : -1;
    }

    private boolean filterHeader(int index) {
        return this.headerName == null || this.headerName.equalsIgnoreCase(this.allHeaders[index].getName());
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
        this.currentIndex = this.findNext(current);
        return this.allHeaders[current];
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing headers is not supported.");
    }
}

