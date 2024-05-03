/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.filter.TokenFilter;

public class JsonPointerBasedFilter
extends TokenFilter {
    protected final JsonPointer _pathToMatch;

    public JsonPointerBasedFilter(String ptrExpr) {
        this(JsonPointer.compile(ptrExpr));
    }

    public JsonPointerBasedFilter(JsonPointer match) {
        this._pathToMatch = match;
    }

    @Override
    public TokenFilter includeElement(int index) {
        JsonPointer next = this._pathToMatch.matchElement(index);
        if (next == null) {
            return null;
        }
        if (next.matches()) {
            return TokenFilter.INCLUDE_ALL;
        }
        return new JsonPointerBasedFilter(next);
    }

    @Override
    public TokenFilter includeProperty(String name) {
        JsonPointer next = this._pathToMatch.matchProperty(name);
        if (next == null) {
            return null;
        }
        if (next.matches()) {
            return TokenFilter.INCLUDE_ALL;
        }
        return new JsonPointerBasedFilter(next);
    }

    @Override
    public TokenFilter filterStartArray() {
        return this;
    }

    @Override
    public TokenFilter filterStartObject() {
        return this;
    }

    @Override
    protected boolean _includeScalar() {
        return this._pathToMatch.matches();
    }

    @Override
    public String toString() {
        return "[JsonPointerFilter at: " + this._pathToMatch + "]";
    }
}

