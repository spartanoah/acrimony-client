/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Iterator;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.AbstractHeaderElementIterator;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.HeaderValueParser;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.util.Args;

public class BasicHeaderElementIterator
extends AbstractHeaderElementIterator<HeaderElement> {
    private final HeaderValueParser parser;

    public BasicHeaderElementIterator(Iterator<Header> headerIterator, HeaderValueParser parser) {
        super(headerIterator);
        this.parser = Args.notNull(parser, "Parser");
    }

    public BasicHeaderElementIterator(Iterator<Header> headerIterator) {
        this(headerIterator, BasicHeaderValueParser.INSTANCE);
    }

    @Override
    HeaderElement parseHeaderElement(CharSequence buf, ParserCursor cursor) {
        HeaderElement e = this.parser.parseHeaderElement(buf, cursor);
        if (!e.getName().isEmpty() || e.getValue() != null) {
            return e;
        }
        return null;
    }
}

