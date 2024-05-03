/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.HeaderValueFormatter;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicHeaderValueFormatter
implements HeaderValueFormatter {
    public static final BasicHeaderValueFormatter INSTANCE = new BasicHeaderValueFormatter();
    private static final String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    private static final String UNSAFE_CHARS = "\"\\";

    @Override
    public void formatElements(CharArrayBuffer buffer, HeaderElement[] elems, boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(elems, "Header element array");
        for (int i = 0; i < elems.length; ++i) {
            if (i > 0) {
                buffer.append(", ");
            }
            this.formatHeaderElement(buffer, elems[i], quote);
        }
    }

    @Override
    public void formatHeaderElement(CharArrayBuffer buffer, HeaderElement elem, boolean quote) {
        int c;
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(elem, "Header element");
        buffer.append(elem.getName());
        String value = elem.getValue();
        if (value != null) {
            buffer.append('=');
            this.formatValue(buffer, value, quote);
        }
        if ((c = elem.getParameterCount()) > 0) {
            for (int i = 0; i < c; ++i) {
                buffer.append("; ");
                this.formatNameValuePair(buffer, elem.getParameter(i), quote);
            }
        }
    }

    @Override
    public void formatParameters(CharArrayBuffer buffer, NameValuePair[] nvps, boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(nvps, "Header parameter array");
        for (int i = 0; i < nvps.length; ++i) {
            if (i > 0) {
                buffer.append("; ");
            }
            this.formatNameValuePair(buffer, nvps[i], quote);
        }
    }

    @Override
    public void formatNameValuePair(CharArrayBuffer buffer, NameValuePair nvp, boolean quote) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(nvp, "Name / value pair");
        buffer.append(nvp.getName());
        String value = nvp.getValue();
        if (value != null) {
            buffer.append('=');
            this.formatValue(buffer, value, quote);
        }
    }

    void formatValue(CharArrayBuffer buffer, String value, boolean quote) {
        int i;
        boolean quoteFlag = quote;
        if (!quoteFlag) {
            for (i = 0; i < value.length() && !quoteFlag; ++i) {
                quoteFlag = this.isSeparator(value.charAt(i));
            }
        }
        if (quoteFlag) {
            buffer.append('\"');
        }
        for (i = 0; i < value.length(); ++i) {
            char ch = value.charAt(i);
            if (this.isUnsafe(ch)) {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        if (quoteFlag) {
            buffer.append('\"');
        }
    }

    boolean isSeparator(char ch) {
        return SEPARATORS.indexOf(ch) >= 0;
    }

    boolean isUnsafe(char ch) {
        return UNSAFE_CHARS.indexOf(ch) >= 0;
    }
}

