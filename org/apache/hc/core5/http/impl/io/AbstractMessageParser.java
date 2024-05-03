/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.http.message.LazyLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public abstract class AbstractMessageParser<T extends HttpMessage>
implements HttpMessageParser<T> {
    private static final int HEAD_LINE = 0;
    private static final int HEADERS = 1;
    private final Http1Config http1Config;
    private final List<CharArrayBuffer> headerLines;
    private final CharArrayBuffer headLine;
    private final LineParser lineParser;
    private int state;
    private T message;

    public AbstractMessageParser(LineParser lineParser, Http1Config http1Config) {
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.headerLines = new ArrayList<CharArrayBuffer>();
        this.headLine = new CharArrayBuffer(128);
        this.state = 0;
    }

    LineParser getLineParser() {
        return this.lineParser;
    }

    public static Header[] parseHeaders(SessionInputBuffer inBuffer, InputStream inputStream, int maxHeaderCount, int maxLineLen, LineParser lineParser) throws HttpException, IOException {
        ArrayList<CharArrayBuffer> headerLines = new ArrayList<CharArrayBuffer>();
        return AbstractMessageParser.parseHeaders(inBuffer, inputStream, maxHeaderCount, maxLineLen, lineParser != null ? lineParser : LazyLineParser.INSTANCE, headerLines);
    }

    public static Header[] parseHeaders(SessionInputBuffer inBuffer, InputStream inputStream, int maxHeaderCount, int maxLineLen, LineParser parser, List<CharArrayBuffer> headerLines) throws HttpException, IOException {
        int i;
        block7: {
            Args.notNull(inBuffer, "Session input buffer");
            Args.notNull(inputStream, "Input stream");
            Args.notNull(parser, "Line parser");
            Args.notNull(headerLines, "Header line list");
            CharArrayBuffer current = null;
            CharArrayBuffer previous = null;
            do {
                if (current == null) {
                    current = new CharArrayBuffer(64);
                } else {
                    current.clear();
                }
                int readLen = inBuffer.readLine(current, inputStream);
                if (readLen == -1 || current.length() < 1) break block7;
                if ((current.charAt(0) == ' ' || current.charAt(0) == '\t') && previous != null) {
                    char ch;
                    for (i = 0; i < current.length() && ((ch = current.charAt(i)) == ' ' || ch == '\t'); ++i) {
                    }
                    if (maxLineLen > 0 && previous.length() + 1 + current.length() - i > maxLineLen) {
                        throw new MessageConstraintException("Maximum line length limit exceeded");
                    }
                    previous.append(' ');
                    previous.append(current, i, current.length() - i);
                    continue;
                }
                headerLines.add(current);
                previous = current;
                current = null;
            } while (maxHeaderCount <= 0 || headerLines.size() < maxHeaderCount);
            throw new MessageConstraintException("Maximum header count exceeded");
        }
        Header[] headers = new Header[headerLines.size()];
        for (i = 0; i < headerLines.size(); ++i) {
            CharArrayBuffer buffer = headerLines.get(i);
            headers[i] = parser.parseHeader(buffer);
        }
        return headers;
    }

    protected abstract T createMessage(CharArrayBuffer var1) throws IOException, HttpException;

    protected abstract IOException createConnectionClosedException();

    @Override
    public T parse(SessionInputBuffer buffer, InputStream inputStream) throws IOException, HttpException {
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(inputStream, "Input stream");
        int st = this.state;
        switch (st) {
            case 0: {
                for (int n = 0; n < this.http1Config.getMaxEmptyLineCount(); ++n) {
                    this.headLine.clear();
                    int i = buffer.readLine(this.headLine, inputStream);
                    if (i == -1) {
                        throw this.createConnectionClosedException();
                    }
                    if (this.headLine.length() <= 0) continue;
                    this.message = this.createMessage(this.headLine);
                    if (this.message != null) break;
                }
                if (this.message == null) {
                    throw new MessageConstraintException("Maximum empty line limit exceeded");
                }
                this.state = 1;
            }
            case 1: {
                Header[] headers = AbstractMessageParser.parseHeaders(buffer, inputStream, this.http1Config.getMaxHeaderCount(), this.http1Config.getMaxLineLength(), this.lineParser, this.headerLines);
                this.message.setHeaders(headers);
                T result = this.message;
                this.message = null;
                this.headerLines.clear();
                this.state = 0;
                return result;
            }
        }
        throw new IllegalStateException("Inconsistent parser state");
    }
}

