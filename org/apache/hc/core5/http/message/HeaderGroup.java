/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicListHeaderIterator;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.LangUtils;

public class HeaderGroup
implements MessageHeaders,
Serializable {
    private static final long serialVersionUID = 2608834160639271617L;
    private static final Header[] EMPTY = new Header[0];
    private final List<Header> headers = new ArrayList<Header>(16);

    public void clear() {
        this.headers.clear();
    }

    public void addHeader(Header header) {
        if (header == null) {
            return;
        }
        this.headers.add(header);
    }

    public boolean removeHeader(Header header) {
        if (header == null) {
            return false;
        }
        for (int i = 0; i < this.headers.size(); ++i) {
            Header current = this.headers.get(i);
            if (!this.headerEquals(header, current)) continue;
            this.headers.remove(current);
            return true;
        }
        return false;
    }

    private boolean headerEquals(Header header1, Header header2) {
        return header2 == header1 || header2.getName().equalsIgnoreCase(header1.getName()) && LangUtils.equals(header1.getValue(), header2.getValue());
    }

    public boolean removeHeaders(Header header) {
        if (header == null) {
            return false;
        }
        boolean removed = false;
        Iterator<Header> iterator = this.headerIterator();
        while (iterator.hasNext()) {
            Header current = iterator.next();
            if (!this.headerEquals(header, current)) continue;
            iterator.remove();
            removed = true;
        }
        return removed;
    }

    public void setHeader(Header header) {
        if (header == null) {
            return;
        }
        for (int i = 0; i < this.headers.size(); ++i) {
            Header current = this.headers.get(i);
            if (!current.getName().equalsIgnoreCase(header.getName())) continue;
            this.headers.set(i, header);
            return;
        }
        this.headers.add(header);
    }

    public void setHeaders(Header ... headers) {
        this.clear();
        if (headers == null) {
            return;
        }
        Collections.addAll(this.headers, headers);
    }

    public Header getCondensedHeader(String name) {
        Header[] hdrs = this.getHeaders(name);
        if (hdrs.length == 0) {
            return null;
        }
        if (hdrs.length == 1) {
            return hdrs[0];
        }
        CharArrayBuffer valueBuffer = new CharArrayBuffer(128);
        valueBuffer.append(hdrs[0].getValue());
        for (int i = 1; i < hdrs.length; ++i) {
            valueBuffer.append(", ");
            valueBuffer.append(hdrs[i].getValue());
        }
        return new BasicHeader(name.toLowerCase(Locale.ROOT), valueBuffer.toString());
    }

    @Override
    public Header[] getHeaders(String name) {
        ArrayList<Header> headersFound = null;
        for (int i = 0; i < this.headers.size(); ++i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            if (headersFound == null) {
                headersFound = new ArrayList<Header>();
            }
            headersFound.add(header);
        }
        return headersFound != null ? headersFound.toArray(new Header[headersFound.size()]) : EMPTY;
    }

    @Override
    public Header getFirstHeader(String name) {
        for (int i = 0; i < this.headers.size(); ++i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            return header;
        }
        return null;
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        int count = 0;
        Header singleHeader = null;
        for (int i = 0; i < this.headers.size(); ++i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            singleHeader = header;
            ++count;
        }
        if (count > 1) {
            throw new ProtocolException("multiple '%s' headers found", name);
        }
        return singleHeader;
    }

    @Override
    public Header getLastHeader(String name) {
        for (int i = this.headers.size() - 1; i >= 0; --i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            return header;
        }
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return this.headers.toArray(new Header[this.headers.size()]);
    }

    @Override
    public boolean containsHeader(String name) {
        for (int i = 0; i < this.headers.size(); ++i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int countHeaders(String name) {
        int count = 0;
        for (int i = 0; i < this.headers.size(); ++i) {
            Header header = this.headers.get(i);
            if (!header.getName().equalsIgnoreCase(name)) continue;
            ++count;
        }
        return count;
    }

    @Override
    public Iterator<Header> headerIterator() {
        return new BasicListHeaderIterator(this.headers, null);
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return new BasicListHeaderIterator(this.headers, name);
    }

    public boolean removeHeaders(String name) {
        if (name == null) {
            return false;
        }
        boolean removed = false;
        Iterator<Header> iterator = this.headerIterator();
        while (iterator.hasNext()) {
            Header header = iterator.next();
            if (!header.getName().equalsIgnoreCase(name)) continue;
            iterator.remove();
            removed = true;
        }
        return removed;
    }

    public String toString() {
        return this.headers.toString();
    }
}

