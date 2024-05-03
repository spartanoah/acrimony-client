/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.message;

import java.util.NoSuchElementException;
import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
public class BasicHeaderElementIterator
implements HeaderElementIterator {
    private final HeaderIterator headerIt;
    private final HeaderValueParser parser;
    private HeaderElement currentElement = null;
    private CharArrayBuffer buffer = null;
    private ParserCursor cursor = null;

    public BasicHeaderElementIterator(HeaderIterator headerIterator, HeaderValueParser parser) {
        this.headerIt = Args.notNull(headerIterator, "Header iterator");
        this.parser = Args.notNull(parser, "Parser");
    }

    public BasicHeaderElementIterator(HeaderIterator headerIterator) {
        this(headerIterator, BasicHeaderValueParser.INSTANCE);
    }

    private void bufferHeaderValue() {
        this.cursor = null;
        this.buffer = null;
        while (this.headerIt.hasNext()) {
            Header h = this.headerIt.nextHeader();
            if (h instanceof FormattedHeader) {
                this.buffer = ((FormattedHeader)h).getBuffer();
                this.cursor = new ParserCursor(0, this.buffer.length());
                this.cursor.updatePos(((FormattedHeader)h).getValuePos());
                break;
            }
            String value = h.getValue();
            if (value == null) continue;
            this.buffer = new CharArrayBuffer(value.length());
            this.buffer.append(value);
            this.cursor = new ParserCursor(0, this.buffer.length());
            break;
        }
    }

    private void parseNextElement() {
        while (this.headerIt.hasNext() || this.cursor != null) {
            if (this.cursor == null || this.cursor.atEnd()) {
                this.bufferHeaderValue();
            }
            if (this.cursor == null) continue;
            while (!this.cursor.atEnd()) {
                HeaderElement e = this.parser.parseHeaderElement(this.buffer, this.cursor);
                if (e.getName().length() == 0 && e.getValue() == null) continue;
                this.currentElement = e;
                return;
            }
            if (!this.cursor.atEnd()) continue;
            this.cursor = null;
            this.buffer = null;
        }
    }

    public boolean hasNext() {
        if (this.currentElement == null) {
            this.parseNextElement();
        }
        return this.currentElement != null;
    }

    public HeaderElement nextElement() throws NoSuchElementException {
        if (this.currentElement == null) {
            this.parseNextElement();
        }
        if (this.currentElement == null) {
            throw new NoSuchElementException("No more header elements available");
        }
        HeaderElement element = this.currentElement;
        this.currentElement = null;
        return element;
    }

    public final Object next() throws NoSuchElementException {
        return this.nextElement();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Remove not supported");
    }
}

