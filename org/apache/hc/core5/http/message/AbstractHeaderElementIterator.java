/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.util.Args;

abstract class AbstractHeaderElementIterator<T>
implements Iterator<T> {
    private final Iterator<Header> headerIt;
    private T currentElement = null;
    private CharSequence buffer = null;
    private ParserCursor cursor = null;

    AbstractHeaderElementIterator(Iterator<Header> headerIterator) {
        this.headerIt = Args.notNull(headerIterator, "Header iterator");
    }

    private void bufferHeaderValue() {
        this.cursor = null;
        this.buffer = null;
        while (this.headerIt.hasNext()) {
            Header h = this.headerIt.next();
            if (h instanceof FormattedHeader) {
                this.buffer = ((FormattedHeader)h).getBuffer();
                this.cursor = new ParserCursor(0, this.buffer.length());
                this.cursor.updatePos(((FormattedHeader)h).getValuePos());
                break;
            }
            String value = h.getValue();
            if (value == null) continue;
            this.buffer = value;
            this.cursor = new ParserCursor(0, value.length());
            break;
        }
    }

    abstract T parseHeaderElement(CharSequence var1, ParserCursor var2);

    private void parseNextElement() {
        while (this.headerIt.hasNext() || this.cursor != null) {
            if (this.cursor == null || this.cursor.atEnd()) {
                this.bufferHeaderValue();
            }
            if (this.cursor == null) continue;
            while (!this.cursor.atEnd()) {
                T e = this.parseHeaderElement(this.buffer, this.cursor);
                if (e == null) continue;
                this.currentElement = e;
                return;
            }
            if (!this.cursor.atEnd()) continue;
            this.cursor = null;
            this.buffer = null;
        }
    }

    @Override
    public boolean hasNext() {
        if (this.currentElement == null) {
            this.parseNextElement();
        }
        return this.currentElement != null;
    }

    @Override
    public T next() throws NoSuchElementException {
        if (this.currentElement == null) {
            this.parseNextElement();
        }
        if (this.currentElement == null) {
            throw new NoSuchElementException("No more header elements available");
        }
        T element = this.currentElement;
        this.currentElement = null;
        return element;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Remove not supported");
    }
}

