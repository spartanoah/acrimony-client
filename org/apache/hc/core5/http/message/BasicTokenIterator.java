/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.BitSet;
import java.util.Iterator;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.AbstractHeaderElementIterator;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.TextUtils;

public class BasicTokenIterator
extends AbstractHeaderElementIterator<String> {
    private static final BitSet COMMA = TokenParser.INIT_BITSET(44);
    private final TokenParser parser = TokenParser.INSTANCE;

    public BasicTokenIterator(Iterator<Header> headerIterator) {
        super(headerIterator);
    }

    @Override
    String parseHeaderElement(CharSequence buf, ParserCursor cursor) {
        int pos;
        String token = this.parser.parseToken(buf, cursor, COMMA);
        if (!cursor.atEnd() && buf.charAt(pos = cursor.getPos()) == ',') {
            cursor.updatePos(pos + 1);
        }
        return !TextUtils.isBlank(token) ? token : null;
    }
}

