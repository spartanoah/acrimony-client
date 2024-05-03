/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.util.Iterator;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolException;

public interface MessageHeaders {
    public boolean containsHeader(String var1);

    public int countHeaders(String var1);

    public Header getFirstHeader(String var1);

    public Header getHeader(String var1) throws ProtocolException;

    public Header[] getHeaders();

    public Header[] getHeaders(String var1);

    public Header getLastHeader(String var1);

    public Iterator<Header> headerIterator();

    public Iterator<Header> headerIterator(String var1);
}

