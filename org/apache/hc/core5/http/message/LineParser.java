/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface LineParser {
    public RequestLine parseRequestLine(CharArrayBuffer var1) throws ParseException;

    public StatusLine parseStatusLine(CharArrayBuffer var1) throws ParseException;

    public Header parseHeader(CharArrayBuffer var1) throws ParseException;
}

