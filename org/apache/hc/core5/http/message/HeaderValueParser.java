/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;

public interface HeaderValueParser {
    public HeaderElement[] parseElements(CharSequence var1, ParserCursor var2);

    public HeaderElement parseHeaderElement(CharSequence var1, ParserCursor var2);

    public NameValuePair[] parseParameters(CharSequence var1, ParserCursor var2);

    public NameValuePair parseNameValuePair(CharSequence var1, ParserCursor var2);
}

