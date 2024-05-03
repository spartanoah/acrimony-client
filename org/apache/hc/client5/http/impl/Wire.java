/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.nio.ByteBuffer;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;

@Internal
public class Wire {
    private static final int MAX_STRING_BUILDER_SIZE = 2048;
    private static final ThreadLocal<StringBuilder> THREAD_LOCAL = new ThreadLocal();
    private final Logger log;
    private final String id;

    private static StringBuilder getStringBuilder() {
        StringBuilder result = THREAD_LOCAL.get();
        if (result == null) {
            result = new StringBuilder(2048);
            THREAD_LOCAL.set(result);
        }
        Wire.trimToMaxSize(result, 2048);
        result.setLength(0);
        return result;
    }

    private static void trimToMaxSize(StringBuilder stringBuilder, int maxSize) {
        if (stringBuilder != null && stringBuilder.capacity() > maxSize) {
            stringBuilder.setLength(maxSize);
            stringBuilder.trimToSize();
        }
    }

    public Wire(Logger log, String id) {
        this.log = log;
        this.id = id;
    }

    private void wire(String header, byte[] b, int pos, int off) {
        StringBuilder buffer = Wire.getStringBuilder();
        for (int i = 0; i < off; ++i) {
            byte ch = b[pos + i];
            if (ch == 13) {
                buffer.append("[\\r]");
                continue;
            }
            if (ch == 10) {
                buffer.append("[\\n]\"");
                buffer.insert(0, "\"");
                buffer.insert(0, header);
                this.log.debug("{} {}", (Object)this.id, (Object)buffer);
                buffer.setLength(0);
                continue;
            }
            if (ch < 32 || ch >= 127) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
                continue;
            }
            buffer.append((char)ch);
        }
        if (buffer.length() > 0) {
            buffer.append('\"');
            buffer.insert(0, '\"');
            buffer.insert(0, header);
            this.log.debug("{} {}", (Object)this.id, (Object)buffer);
        }
    }

    public boolean isEnabled() {
        return this.log.isDebugEnabled();
    }

    public void output(byte[] b, int pos, int off) {
        Args.notNull(b, "Output");
        this.wire(">> ", b, pos, off);
    }

    public void input(byte[] b, int pos, int off) {
        Args.notNull(b, "Input");
        this.wire("<< ", b, pos, off);
    }

    public void output(byte[] b) {
        Args.notNull(b, "Output");
        this.output(b, 0, b.length);
    }

    public void input(byte[] b) {
        Args.notNull(b, "Input");
        this.input(b, 0, b.length);
    }

    public void output(int b) {
        this.output(new byte[]{(byte)b});
    }

    public void input(int b) {
        this.input(new byte[]{(byte)b});
    }

    public void output(String s) {
        Args.notNull(s, "Output");
        this.output(s.getBytes());
    }

    public void input(String s) {
        Args.notNull(s, "Input");
        this.input(s.getBytes());
    }

    public void output(ByteBuffer b) {
        Args.notNull(b, "Output");
        if (b.hasArray()) {
            this.output(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            this.output(tmp);
        }
    }

    public void input(ByteBuffer b) {
        Args.notNull(b, "Input");
        if (b.hasArray()) {
            this.input(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            this.input(tmp);
        }
    }
}

