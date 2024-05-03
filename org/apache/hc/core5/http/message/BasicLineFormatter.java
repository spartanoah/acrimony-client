/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicLineFormatter
implements LineFormatter {
    public static final BasicLineFormatter INSTANCE = new BasicLineFormatter();

    void formatProtocolVersion(CharArrayBuffer buffer, ProtocolVersion version) {
        buffer.append(version.format());
    }

    @Override
    public void formatRequestLine(CharArrayBuffer buffer, RequestLine reqline) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(reqline, "Request line");
        buffer.append(reqline.getMethod());
        buffer.append(' ');
        buffer.append(reqline.getUri());
        buffer.append(' ');
        this.formatProtocolVersion(buffer, reqline.getProtocolVersion());
    }

    @Override
    public void formatStatusLine(CharArrayBuffer buffer, StatusLine statusLine) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(statusLine, "Status line");
        this.formatProtocolVersion(buffer, statusLine.getProtocolVersion());
        buffer.append(' ');
        buffer.append(Integer.toString(statusLine.getStatusCode()));
        buffer.append(' ');
        String reasonPhrase = statusLine.getReasonPhrase();
        if (reasonPhrase != null) {
            buffer.append(reasonPhrase);
        }
    }

    @Override
    public void formatHeader(CharArrayBuffer buffer, Header header) {
        Args.notNull(buffer, "Char array buffer");
        Args.notNull(header, "Header");
        buffer.append(header.getName());
        buffer.append(": ");
        String value = header.getValue();
        if (value != null) {
            buffer.ensureCapacity(buffer.length() + value.length());
            for (int valueIndex = 0; valueIndex < value.length(); ++valueIndex) {
                char valueChar = value.charAt(valueIndex);
                if (valueChar == '\r' || valueChar == '\n' || valueChar == '\f' || valueChar == '\u000b') {
                    valueChar = ' ';
                }
                buffer.append(valueChar);
            }
        }
    }
}

