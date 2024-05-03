/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.MessageConstraintException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.message.LazyLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.nio.NHttpMessageParser;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public abstract class AbstractMessageParser<T extends HttpMessage>
implements NHttpMessageParser<T> {
    private State state;
    private T message;
    private CharArrayBuffer lineBuf;
    private final List<CharArrayBuffer> headerBufs;
    private int emptyLineCount;
    private final LineParser lineParser;
    private final Http1Config messageConstraints;

    public AbstractMessageParser(LineParser lineParser, Http1Config messageConstraints) {
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
        this.messageConstraints = messageConstraints != null ? messageConstraints : Http1Config.DEFAULT;
        this.headerBufs = new ArrayList<CharArrayBuffer>();
        this.state = State.READ_HEAD_LINE;
    }

    LineParser getLineParser() {
        return this.lineParser;
    }

    @Override
    public void reset() {
        this.state = State.READ_HEAD_LINE;
        this.headerBufs.clear();
        this.emptyLineCount = 0;
        this.message = null;
    }

    protected abstract T createMessage(CharArrayBuffer var1) throws HttpException;

    private T parseHeadLine() throws IOException, HttpException {
        if (this.lineBuf.isEmpty()) {
            ++this.emptyLineCount;
            if (this.emptyLineCount >= this.messageConstraints.getMaxEmptyLineCount()) {
                throw new MessageConstraintException("Maximum empty line limit exceeded");
            }
            return null;
        }
        return this.createMessage(this.lineBuf);
    }

    private void parseHeader() throws IOException {
        CharArrayBuffer current = this.lineBuf;
        int count = this.headerBufs.size();
        if ((this.lineBuf.charAt(0) == ' ' || this.lineBuf.charAt(0) == '\t') && count > 0) {
            char ch;
            int i;
            CharArrayBuffer previous = this.headerBufs.get(count - 1);
            for (i = 0; i < current.length() && ((ch = current.charAt(i)) == ' ' || ch == '\t'); ++i) {
            }
            int maxLineLen = this.messageConstraints.getMaxLineLength();
            if (maxLineLen > 0 && previous.length() + 1 + current.length() - i > maxLineLen) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            previous.append(' ');
            previous.append(current, i, current.length() - i);
        } else {
            this.headerBufs.add(current);
            this.lineBuf = null;
        }
    }

    @Override
    public T parse(SessionInputBuffer sessionBuffer, boolean endOfStream) throws IOException, HttpException {
        Args.notNull(sessionBuffer, "Session input buffer");
        while (this.state != State.COMPLETED) {
            if (this.lineBuf == null) {
                this.lineBuf = new CharArrayBuffer(64);
            } else {
                this.lineBuf.clear();
            }
            boolean lineComplete = sessionBuffer.readLine(this.lineBuf, endOfStream);
            int maxLineLen = this.messageConstraints.getMaxLineLength();
            if (maxLineLen > 0 && (this.lineBuf.length() > maxLineLen || !lineComplete && sessionBuffer.length() > maxLineLen)) {
                throw new MessageConstraintException("Maximum line length limit exceeded");
            }
            if (!lineComplete) break;
            switch (this.state) {
                case READ_HEAD_LINE: {
                    this.message = this.parseHeadLine();
                    if (this.message == null) break;
                    this.state = State.READ_HEADERS;
                    break;
                }
                case READ_HEADERS: {
                    if (this.lineBuf.length() > 0) {
                        int maxHeaderCount = this.messageConstraints.getMaxHeaderCount();
                        if (maxHeaderCount > 0 && this.headerBufs.size() >= maxHeaderCount) {
                            throw new MessageConstraintException("Maximum header count exceeded");
                        }
                        this.parseHeader();
                        break;
                    }
                    this.state = State.COMPLETED;
                }
            }
            if (!endOfStream || sessionBuffer.hasData()) continue;
            this.state = State.COMPLETED;
        }
        if (this.state == State.COMPLETED) {
            for (CharArrayBuffer buffer : this.headerBufs) {
                this.message.addHeader(this.lineParser.parseHeader(buffer));
            }
            return this.message;
        }
        return null;
    }

    private static enum State {
        READ_HEAD_LINE,
        READ_HEADERS,
        COMPLETED;

    }
}

