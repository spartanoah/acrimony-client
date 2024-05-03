/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.json.JsonReadContext;

public class TokenBufferReadContext
extends JsonStreamContext {
    protected final JsonStreamContext _parent;
    protected final JsonLocation _startLocation;
    protected String _currentName;
    protected Object _currentValue;

    protected TokenBufferReadContext(JsonStreamContext base, Object srcRef) {
        super(base);
        this._parent = base.getParent();
        this._currentName = base.getCurrentName();
        this._currentValue = base.getCurrentValue();
        if (base instanceof JsonReadContext) {
            JsonReadContext rc = (JsonReadContext)base;
            this._startLocation = rc.getStartLocation(srcRef);
        } else {
            this._startLocation = JsonLocation.NA;
        }
    }

    protected TokenBufferReadContext(JsonStreamContext base, JsonLocation startLoc) {
        super(base);
        this._parent = base.getParent();
        this._currentName = base.getCurrentName();
        this._currentValue = base.getCurrentValue();
        this._startLocation = startLoc;
    }

    protected TokenBufferReadContext() {
        super(0, -1);
        this._parent = null;
        this._startLocation = JsonLocation.NA;
    }

    protected TokenBufferReadContext(TokenBufferReadContext parent, int type, int index) {
        super(type, index);
        this._parent = parent;
        this._startLocation = parent._startLocation;
    }

    @Override
    public Object getCurrentValue() {
        return this._currentValue;
    }

    @Override
    public void setCurrentValue(Object v) {
        this._currentValue = v;
    }

    public static TokenBufferReadContext createRootContext(JsonStreamContext origContext) {
        if (origContext == null) {
            return new TokenBufferReadContext();
        }
        return new TokenBufferReadContext(origContext, null);
    }

    public TokenBufferReadContext createChildArrayContext() {
        ++this._index;
        return new TokenBufferReadContext(this, 1, -1);
    }

    public TokenBufferReadContext createChildObjectContext() {
        ++this._index;
        return new TokenBufferReadContext(this, 2, -1);
    }

    public TokenBufferReadContext parentOrCopy() {
        if (this._parent instanceof TokenBufferReadContext) {
            return (TokenBufferReadContext)this._parent;
        }
        if (this._parent == null) {
            return new TokenBufferReadContext();
        }
        return new TokenBufferReadContext(this._parent, this._startLocation);
    }

    @Override
    public String getCurrentName() {
        return this._currentName;
    }

    @Override
    public boolean hasCurrentName() {
        return this._currentName != null;
    }

    @Override
    public JsonStreamContext getParent() {
        return this._parent;
    }

    public void setCurrentName(String name) throws JsonProcessingException {
        this._currentName = name;
    }

    public void updateForValue() {
        ++this._index;
    }
}

