/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.io.ContentReference
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.ContentReference;
import java.util.Set;

public final class XmlReadContext
extends JsonStreamContext {
    protected final XmlReadContext _parent;
    protected int _lineNr;
    protected int _columnNr;
    protected String _currentName;
    protected Object _currentValue;
    protected Set<String> _namesToWrap;
    protected String _wrappedName;
    protected XmlReadContext _child = null;

    public XmlReadContext(XmlReadContext parent, int type, int lineNr, int colNr) {
        this._type = type;
        this._parent = parent;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._index = -1;
    }

    protected final void reset(int type, int lineNr, int colNr) {
        this._type = type;
        this._index = -1;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._currentName = null;
        this._currentValue = null;
        this._namesToWrap = null;
    }

    @Override
    public Object getCurrentValue() {
        return this._currentValue;
    }

    @Override
    public void setCurrentValue(Object v) {
        this._currentValue = v;
    }

    public static XmlReadContext createRootContext(int lineNr, int colNr) {
        return new XmlReadContext(null, 0, lineNr, colNr);
    }

    public static XmlReadContext createRootContext() {
        return new XmlReadContext(null, 0, 1, 0);
    }

    public final XmlReadContext createChildArrayContext(int lineNr, int colNr) {
        ++this._index;
        XmlReadContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new XmlReadContext(this, 1, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(1, lineNr, colNr);
        return ctxt;
    }

    public final XmlReadContext createChildObjectContext(int lineNr, int colNr) {
        ++this._index;
        XmlReadContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new XmlReadContext(this, 2, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(2, lineNr, colNr);
        return ctxt;
    }

    @Override
    public final String getCurrentName() {
        return this._currentName;
    }

    @Override
    public boolean hasCurrentName() {
        return this._currentName != null;
    }

    @Override
    public final XmlReadContext getParent() {
        return this._parent;
    }

    public final JsonLocation startLocation(ContentReference srcRef) {
        long totalChars = -1L;
        return new JsonLocation(srcRef, totalChars, this._lineNr, this._columnNr);
    }

    public final void valueStarted() {
        ++this._index;
    }

    public void setCurrentName(String name) {
        this._currentName = name;
    }

    public void setNamesToWrap(Set<String> namesToWrap) {
        this._namesToWrap = namesToWrap;
    }

    public boolean shouldWrap(String localName) {
        return this._namesToWrap != null && this._namesToWrap.contains(localName);
    }

    protected void convertToArray() {
        this._type = 1;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(64);
        switch (this._type) {
            case 0: {
                sb.append("/");
                break;
            }
            case 1: {
                sb.append('[');
                sb.append(this.getCurrentIndex());
                sb.append(']');
                break;
            }
            case 2: {
                sb.append('{');
                if (this._currentName != null) {
                    sb.append('\"');
                    CharTypes.appendQuoted(sb, this._currentName);
                    sb.append('\"');
                } else {
                    sb.append('?');
                }
                sb.append('}');
            }
        }
        return sb.toString();
    }
}

