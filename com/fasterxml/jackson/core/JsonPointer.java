/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.io.NumberInput;

public class JsonPointer {
    public static final char SEPARATOR = '/';
    protected static final JsonPointer EMPTY = new JsonPointer();
    protected final JsonPointer _nextSegment;
    protected volatile JsonPointer _head;
    protected final String _asString;
    protected final String _matchingPropertyName;
    protected final int _matchingElementIndex;

    protected JsonPointer() {
        this._nextSegment = null;
        this._matchingPropertyName = "";
        this._matchingElementIndex = -1;
        this._asString = "";
    }

    protected JsonPointer(String fullString, String segment, JsonPointer next) {
        this._asString = fullString;
        this._nextSegment = next;
        this._matchingPropertyName = segment;
        this._matchingElementIndex = JsonPointer._parseIndex(segment);
    }

    protected JsonPointer(String fullString, String segment, int matchIndex, JsonPointer next) {
        this._asString = fullString;
        this._nextSegment = next;
        this._matchingPropertyName = segment;
        this._matchingElementIndex = matchIndex;
    }

    public static JsonPointer compile(String input) throws IllegalArgumentException {
        if (input == null || input.length() == 0) {
            return EMPTY;
        }
        if (input.charAt(0) != '/') {
            throw new IllegalArgumentException("Invalid input: JSON Pointer expression must start with '/': \"" + input + "\"");
        }
        return JsonPointer._parseTail(input);
    }

    public static JsonPointer valueOf(String input) {
        return JsonPointer.compile(input);
    }

    public static JsonPointer empty() {
        return EMPTY;
    }

    public static JsonPointer forPath(JsonStreamContext context, boolean includeRoot) {
        if (context == null) {
            return EMPTY;
        }
        if (!(context.hasPathSegment() || includeRoot && context.inRoot() && context.hasCurrentIndex())) {
            context = context.getParent();
        }
        JsonPointer tail = null;
        while (context != null) {
            if (context.inObject()) {
                String seg = context.getCurrentName();
                if (seg == null) {
                    seg = "";
                }
                tail = new JsonPointer(JsonPointer._fullPath(tail, seg), seg, tail);
            } else if (context.inArray() || includeRoot) {
                int ix = context.getCurrentIndex();
                String ixStr = String.valueOf(ix);
                tail = new JsonPointer(JsonPointer._fullPath(tail, ixStr), ixStr, ix, tail);
            }
            context = context.getParent();
        }
        if (tail == null) {
            return EMPTY;
        }
        return tail;
    }

    private static String _fullPath(JsonPointer tail, String segment) {
        if (tail == null) {
            StringBuilder sb = new StringBuilder(segment.length() + 1);
            sb.append('/');
            JsonPointer._appendEscaped(sb, segment);
            return sb.toString();
        }
        String tailDesc = tail._asString;
        StringBuilder sb = new StringBuilder(segment.length() + 1 + tailDesc.length());
        sb.append('/');
        JsonPointer._appendEscaped(sb, segment);
        sb.append(tailDesc);
        return sb.toString();
    }

    private static void _appendEscaped(StringBuilder sb, String segment) {
        int end = segment.length();
        for (int i = 0; i < end; ++i) {
            char c = segment.charAt(i);
            if (c == '/') {
                sb.append("~1");
                continue;
            }
            if (c == '~') {
                sb.append("~0");
                continue;
            }
            sb.append(c);
        }
    }

    public boolean matches() {
        return this._nextSegment == null;
    }

    public String getMatchingProperty() {
        return this._matchingPropertyName;
    }

    public int getMatchingIndex() {
        return this._matchingElementIndex;
    }

    public boolean mayMatchProperty() {
        return this._matchingPropertyName != null;
    }

    public boolean mayMatchElement() {
        return this._matchingElementIndex >= 0;
    }

    public JsonPointer last() {
        JsonPointer next;
        JsonPointer current = this;
        if (current == EMPTY) {
            return null;
        }
        while ((next = current._nextSegment) != EMPTY) {
            current = next;
        }
        return current;
    }

    public JsonPointer append(JsonPointer tail) {
        if (this == EMPTY) {
            return tail;
        }
        if (tail == EMPTY) {
            return this;
        }
        String currentJsonPointer = this._asString;
        if (currentJsonPointer.endsWith("/")) {
            currentJsonPointer = currentJsonPointer.substring(0, currentJsonPointer.length() - 1);
        }
        return JsonPointer.compile(currentJsonPointer + tail._asString);
    }

    public boolean matchesProperty(String name) {
        return this._nextSegment != null && this._matchingPropertyName.equals(name);
    }

    public JsonPointer matchProperty(String name) {
        if (this._nextSegment != null && this._matchingPropertyName.equals(name)) {
            return this._nextSegment;
        }
        return null;
    }

    public boolean matchesElement(int index) {
        return index == this._matchingElementIndex && index >= 0;
    }

    public JsonPointer matchElement(int index) {
        if (index != this._matchingElementIndex || index < 0) {
            return null;
        }
        return this._nextSegment;
    }

    public JsonPointer tail() {
        return this._nextSegment;
    }

    public JsonPointer head() {
        JsonPointer h = this._head;
        if (h == null) {
            if (this != EMPTY) {
                h = this._constructHead();
            }
            this._head = h;
        }
        return h;
    }

    public String toString() {
        return this._asString;
    }

    public int hashCode() {
        return this._asString.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof JsonPointer)) {
            return false;
        }
        return this._asString.equals(((JsonPointer)o)._asString);
    }

    private static final int _parseIndex(String str) {
        long l;
        int len = str.length();
        if (len == 0 || len > 10) {
            return -1;
        }
        char c = str.charAt(0);
        if (c <= '0') {
            return len == 1 && c == '0' ? 0 : -1;
        }
        if (c > '9') {
            return -1;
        }
        for (int i = 1; i < len; ++i) {
            c = str.charAt(i);
            if (c <= '9' && c >= '0') continue;
            return -1;
        }
        if (len == 10 && (l = NumberInput.parseLong(str)) > Integer.MAX_VALUE) {
            return -1;
        }
        return NumberInput.parseInt(str);
    }

    protected static JsonPointer _parseTail(String input) {
        int end = input.length();
        int i = 1;
        while (i < end) {
            char c = input.charAt(i);
            if (c == '/') {
                return new JsonPointer(input, input.substring(1, i), JsonPointer._parseTail(input.substring(i)));
            }
            if (c != '~' || ++i >= end) continue;
            return JsonPointer._parseQuotedTail(input, i);
        }
        return new JsonPointer(input, input.substring(1), EMPTY);
    }

    protected static JsonPointer _parseQuotedTail(String input, int i) {
        int end = input.length();
        StringBuilder sb = new StringBuilder(Math.max(16, end));
        if (i > 2) {
            sb.append(input, 1, i - 1);
        }
        JsonPointer._appendEscape(sb, input.charAt(i++));
        while (i < end) {
            char c = input.charAt(i);
            if (c == '/') {
                return new JsonPointer(input, sb.toString(), JsonPointer._parseTail(input.substring(i)));
            }
            if (c == '~' && ++i < end) {
                JsonPointer._appendEscape(sb, input.charAt(i++));
                continue;
            }
            sb.append(c);
        }
        return new JsonPointer(input, sb.toString(), EMPTY);
    }

    protected JsonPointer _constructHead() {
        JsonPointer last = this.last();
        if (last == this) {
            return EMPTY;
        }
        int suffixLength = last._asString.length();
        JsonPointer next = this._nextSegment;
        return new JsonPointer(this._asString.substring(0, this._asString.length() - suffixLength), this._matchingPropertyName, this._matchingElementIndex, next._constructHead(suffixLength, last));
    }

    protected JsonPointer _constructHead(int suffixLength, JsonPointer last) {
        if (this == last) {
            return EMPTY;
        }
        JsonPointer next = this._nextSegment;
        String str = this._asString;
        return new JsonPointer(str.substring(0, str.length() - suffixLength), this._matchingPropertyName, this._matchingElementIndex, next._constructHead(suffixLength, last));
    }

    private static void _appendEscape(StringBuilder sb, char c) {
        if (c == '0') {
            c = (char)126;
        } else if (c == '1') {
            c = (char)47;
        } else {
            sb.append('~');
        }
        sb.append(c);
    }
}

