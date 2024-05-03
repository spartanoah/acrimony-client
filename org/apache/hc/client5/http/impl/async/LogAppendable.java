/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import org.slf4j.Logger;

final class LogAppendable
implements Appendable {
    private final Logger log;
    private final String prefix;
    private final StringBuilder buffer;

    public LogAppendable(Logger log, String prefix) {
        this.log = log;
        this.prefix = prefix;
        this.buffer = new StringBuilder();
    }

    @Override
    public Appendable append(CharSequence text) throws IOException {
        return this.append(text, 0, text.length());
    }

    @Override
    public Appendable append(CharSequence text, int start, int end) throws IOException {
        for (int i = start; i < end; ++i) {
            this.append(text.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(char ch) throws IOException {
        if (ch == '\n') {
            this.log.debug("{} {}", (Object)this.prefix, (Object)this.buffer);
            this.buffer.setLength(0);
        } else if (ch != '\r') {
            this.buffer.append(ch);
        }
        return this;
    }

    public void flush() {
        if (this.buffer.length() > 0) {
            this.log.debug("{} {}", (Object)this.prefix, (Object)this.buffer);
            this.buffer.setLength(0);
        }
    }
}

