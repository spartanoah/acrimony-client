/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.filter.TokenFilter;
import java.io.IOException;

public class TokenFilterContext
extends JsonStreamContext {
    protected final TokenFilterContext _parent;
    protected TokenFilterContext _child;
    protected String _currentName;
    protected TokenFilter _filter;
    protected boolean _startHandled;
    protected boolean _needToHandleName;

    protected TokenFilterContext(int type, TokenFilterContext parent, TokenFilter filter, boolean startHandled) {
        this._type = type;
        this._parent = parent;
        this._filter = filter;
        this._index = -1;
        this._startHandled = startHandled;
        this._needToHandleName = false;
    }

    protected TokenFilterContext reset(int type, TokenFilter filter, boolean startWritten) {
        this._type = type;
        this._filter = filter;
        this._index = -1;
        this._currentName = null;
        this._startHandled = startWritten;
        this._needToHandleName = false;
        return this;
    }

    public static TokenFilterContext createRootContext(TokenFilter filter) {
        return new TokenFilterContext(0, null, filter, true);
    }

    public TokenFilterContext createChildArrayContext(TokenFilter filter, boolean writeStart) {
        TokenFilterContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new TokenFilterContext(1, this, filter, writeStart);
            return ctxt;
        }
        return ctxt.reset(1, filter, writeStart);
    }

    public TokenFilterContext createChildObjectContext(TokenFilter filter, boolean writeStart) {
        TokenFilterContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new TokenFilterContext(2, this, filter, writeStart);
            return ctxt;
        }
        return ctxt.reset(2, filter, writeStart);
    }

    public TokenFilter setFieldName(String name) throws JsonProcessingException {
        this._currentName = name;
        this._needToHandleName = true;
        return this._filter;
    }

    public TokenFilter checkValue(TokenFilter filter) {
        if (this._type == 2) {
            return filter;
        }
        int ix = ++this._index;
        if (this._type == 1) {
            return filter.includeElement(ix);
        }
        return filter.includeRootValue(ix);
    }

    public void writePath(JsonGenerator gen) throws IOException {
        if (this._filter == null || this._filter == TokenFilter.INCLUDE_ALL) {
            return;
        }
        if (this._parent != null) {
            this._parent._writePath(gen);
        }
        if (this._startHandled) {
            if (this._needToHandleName) {
                gen.writeFieldName(this._currentName);
            }
        } else {
            this._startHandled = true;
            if (this._type == 2) {
                gen.writeStartObject();
                gen.writeFieldName(this._currentName);
            } else if (this._type == 1) {
                gen.writeStartArray();
            }
        }
    }

    public void writeImmediatePath(JsonGenerator gen) throws IOException {
        if (this._filter == null || this._filter == TokenFilter.INCLUDE_ALL) {
            return;
        }
        if (this._startHandled) {
            if (this._needToHandleName) {
                gen.writeFieldName(this._currentName);
            }
        } else {
            this._startHandled = true;
            if (this._type == 2) {
                gen.writeStartObject();
                if (this._needToHandleName) {
                    gen.writeFieldName(this._currentName);
                }
            } else if (this._type == 1) {
                gen.writeStartArray();
            }
        }
    }

    private void _writePath(JsonGenerator gen) throws IOException {
        if (this._filter == null || this._filter == TokenFilter.INCLUDE_ALL) {
            return;
        }
        if (this._parent != null) {
            this._parent._writePath(gen);
        }
        if (this._startHandled) {
            if (this._needToHandleName) {
                this._needToHandleName = false;
                gen.writeFieldName(this._currentName);
            }
        } else {
            this._startHandled = true;
            if (this._type == 2) {
                gen.writeStartObject();
                if (this._needToHandleName) {
                    this._needToHandleName = false;
                    gen.writeFieldName(this._currentName);
                }
            } else if (this._type == 1) {
                gen.writeStartArray();
            }
        }
    }

    public TokenFilterContext closeArray(JsonGenerator gen) throws IOException {
        if (this._startHandled) {
            gen.writeEndArray();
        }
        if (this._filter != null && this._filter != TokenFilter.INCLUDE_ALL) {
            this._filter.filterFinishArray();
        }
        return this._parent;
    }

    public TokenFilterContext closeObject(JsonGenerator gen) throws IOException {
        if (this._startHandled) {
            gen.writeEndObject();
        }
        if (this._filter != null && this._filter != TokenFilter.INCLUDE_ALL) {
            this._filter.filterFinishObject();
        }
        return this._parent;
    }

    public void skipParentChecks() {
        this._filter = null;
        TokenFilterContext ctxt = this._parent;
        while (ctxt != null) {
            this._parent._filter = null;
            ctxt = ctxt._parent;
        }
    }

    @Override
    public Object getCurrentValue() {
        return null;
    }

    @Override
    public void setCurrentValue(Object v) {
    }

    @Override
    public final TokenFilterContext getParent() {
        return this._parent;
    }

    @Override
    public final String getCurrentName() {
        return this._currentName;
    }

    @Override
    public boolean hasCurrentName() {
        return this._currentName != null;
    }

    public TokenFilter getFilter() {
        return this._filter;
    }

    public boolean isStartHandled() {
        return this._startHandled;
    }

    public JsonToken nextTokenToRead() {
        if (!this._startHandled) {
            this._startHandled = true;
            if (this._type == 2) {
                return JsonToken.START_OBJECT;
            }
            return JsonToken.START_ARRAY;
        }
        if (this._needToHandleName && this._type == 2) {
            this._needToHandleName = false;
            return JsonToken.FIELD_NAME;
        }
        return null;
    }

    public TokenFilterContext findChildOf(TokenFilterContext parent) {
        if (this._parent == parent) {
            return this;
        }
        TokenFilterContext curr = this._parent;
        while (curr != null) {
            TokenFilterContext p = curr._parent;
            if (p == parent) {
                return curr;
            }
            curr = p;
        }
        return null;
    }

    protected void appendDesc(StringBuilder sb) {
        if (this._parent != null) {
            this._parent.appendDesc(sb);
        }
        if (this._type == 2) {
            sb.append('{');
            if (this._currentName != null) {
                sb.append('\"');
                sb.append(this._currentName);
                sb.append('\"');
            } else {
                sb.append('?');
            }
            sb.append('}');
        } else if (this._type == 1) {
            sb.append('[');
            sb.append(this.getCurrentIndex());
            sb.append(']');
        } else {
            sb.append("/");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        this.appendDesc(sb);
        return sb.toString();
    }
}

