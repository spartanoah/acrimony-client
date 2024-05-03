/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;

public class DefaultIndenter
extends DefaultPrettyPrinter.NopIndenter {
    private static final long serialVersionUID = 1L;
    public static final String SYS_LF;
    public static final DefaultIndenter SYSTEM_LINEFEED_INSTANCE;
    private static final int INDENT_LEVELS = 16;
    private final char[] indents;
    private final int charsPerLevel;
    private final String eol;

    public DefaultIndenter() {
        this("  ", SYS_LF);
    }

    public DefaultIndenter(String indent, String eol) {
        this.charsPerLevel = indent.length();
        this.indents = new char[indent.length() * 16];
        int offset = 0;
        for (int i = 0; i < 16; ++i) {
            indent.getChars(0, indent.length(), this.indents, offset);
            offset += indent.length();
        }
        this.eol = eol;
    }

    public DefaultIndenter withLinefeed(String lf) {
        if (lf.equals(this.eol)) {
            return this;
        }
        return new DefaultIndenter(this.getIndent(), lf);
    }

    public DefaultIndenter withIndent(String indent) {
        if (indent.equals(this.getIndent())) {
            return this;
        }
        return new DefaultIndenter(indent, this.eol);
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public void writeIndentation(JsonGenerator jg, int level) throws IOException {
        jg.writeRaw(this.eol);
        if (level > 0) {
            level *= this.charsPerLevel;
            while (level > this.indents.length) {
                jg.writeRaw(this.indents, 0, this.indents.length);
                level -= this.indents.length;
            }
            jg.writeRaw(this.indents, 0, level);
        }
    }

    public String getEol() {
        return this.eol;
    }

    public String getIndent() {
        return new String(this.indents, 0, this.charsPerLevel);
    }

    static {
        String lf;
        try {
            lf = System.getProperty("line.separator");
        } catch (Throwable t) {
            lf = "\n";
        }
        SYS_LF = lf;
        SYSTEM_LINEFEED_INSTANCE = new DefaultIndenter("  ", SYS_LF);
    }
}

