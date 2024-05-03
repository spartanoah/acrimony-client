/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.BitSet;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class TokenParser {
    public static final char DQUOTE = '\"';
    public static final char ESCAPE = '\\';
    public static final TokenParser INSTANCE = new TokenParser();

    public static BitSet INIT_BITSET(int ... b) {
        BitSet bitset = new BitSet();
        for (int aB : b) {
            bitset.set(aB);
        }
        return bitset;
    }

    public static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    public String parseToken(CharSequence buf, ParserCursor cursor, BitSet delimiters) {
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = buf.charAt(cursor.getPos());
            if (delimiters != null && delimiters.get(current)) break;
            if (TokenParser.isWhitespace(current)) {
                this.skipWhiteSpace(buf, cursor);
                whitespace = true;
                continue;
            }
            if (whitespace && dst.length() > 0) {
                dst.append(' ');
            }
            this.copyContent(buf, cursor, delimiters, dst);
            whitespace = false;
        }
        return dst.toString();
    }

    public String parseValue(CharSequence buf, ParserCursor cursor, BitSet delimiters) {
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        StringBuilder dst = new StringBuilder();
        boolean whitespace = false;
        while (!cursor.atEnd()) {
            char current = buf.charAt(cursor.getPos());
            if (delimiters != null && delimiters.get(current)) break;
            if (TokenParser.isWhitespace(current)) {
                this.skipWhiteSpace(buf, cursor);
                whitespace = true;
                continue;
            }
            if (current == '\"') {
                if (whitespace && dst.length() > 0) {
                    dst.append(' ');
                }
                this.copyQuotedContent(buf, cursor, dst);
                whitespace = false;
                continue;
            }
            if (whitespace && dst.length() > 0) {
                dst.append(' ');
            }
            this.copyUnquotedContent(buf, cursor, delimiters, dst);
            whitespace = false;
        }
        return dst.toString();
    }

    public void skipWhiteSpace(CharSequence buf, ParserCursor cursor) {
        char current;
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo && TokenParser.isWhitespace(current = buf.charAt(i)); ++i) {
            ++pos;
        }
        cursor.updatePos(pos);
    }

    public void copyContent(CharSequence buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        Args.notNull(dst, "String builder");
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; ++i) {
            char current = buf.charAt(i);
            if (delimiters != null && delimiters.get(current) || TokenParser.isWhitespace(current)) break;
            ++pos;
            dst.append(current);
        }
        cursor.updatePos(pos);
    }

    public void copyUnquotedContent(CharSequence buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        Args.notNull(dst, "String builder");
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo; ++i) {
            char current = buf.charAt(i);
            if (delimiters != null && delimiters.get(current) || TokenParser.isWhitespace(current) || current == '\"') break;
            ++pos;
            dst.append(current);
        }
        cursor.updatePos(pos);
    }

    public void copyQuotedContent(CharSequence buf, ParserCursor cursor, StringBuilder dst) {
        Args.notNull(buf, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        Args.notNull(dst, "String builder");
        if (cursor.atEnd()) {
            return;
        }
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        char current = buf.charAt(pos);
        if (current != '\"') {
            return;
        }
        ++pos;
        boolean escaped = false;
        int i = ++indexFrom;
        while (i < indexTo) {
            current = buf.charAt(i);
            if (escaped) {
                if (current != '\"' && current != '\\') {
                    dst.append('\\');
                }
                dst.append(current);
                escaped = false;
            } else {
                if (current == '\"') {
                    ++pos;
                    break;
                }
                if (current == '\\') {
                    escaped = true;
                } else if (current != '\r' && current != '\n') {
                    dst.append(current);
                }
            }
            ++i;
            ++pos;
        }
        cursor.updatePos(pos);
    }
}

