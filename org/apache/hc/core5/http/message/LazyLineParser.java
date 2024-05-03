/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class LazyLineParser
extends BasicLineParser {
    public static final LazyLineParser INSTANCE = new LazyLineParser();

    @Override
    public Header parseHeader(CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        return new BufferedHeader(buffer, true);
    }
}

