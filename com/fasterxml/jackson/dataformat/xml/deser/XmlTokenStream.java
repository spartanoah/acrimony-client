/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.io.ContentReference
 *  org.codehaus.stax2.XMLStreamLocation2
 *  org.codehaus.stax2.XMLStreamReader2
 *  org.codehaus.stax2.ri.Stax2ReaderAdapter
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.io.ContentReference;
import com.fasterxml.jackson.dataformat.xml.deser.ElementWrapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.codehaus.stax2.XMLStreamLocation2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.ri.Stax2ReaderAdapter;

public class XmlTokenStream {
    public static final int XML_START_ELEMENT = 1;
    public static final int XML_END_ELEMENT = 2;
    public static final int XML_ATTRIBUTE_NAME = 3;
    public static final int XML_ATTRIBUTE_VALUE = 4;
    public static final int XML_TEXT = 5;
    public static final int XML_DELAYED_START_ELEMENT = 6;
    public static final int XML_ROOT_TEXT = 7;
    public static final int XML_END = 8;
    private static final int REPLAY_START_DUP = 1;
    private static final int REPLAY_END = 2;
    private static final int REPLAY_START_DELAYED = 3;
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    protected final XMLStreamReader2 _xmlReader;
    protected final ContentReference _sourceReference;
    protected int _formatFeatures;
    protected boolean _cfgProcessXsiNil;
    protected int _currentState;
    protected int _attributeCount;
    protected boolean _xsiNilFound;
    protected boolean _startElementAfterText;
    protected int _nextAttributeIndex;
    protected String _localName;
    protected String _namespaceURI;
    protected String _textValue;
    protected boolean _repeatCurrentToken;
    protected int _repeatElement;
    protected ElementWrapper _currentWrapper;
    protected String _nextLocalName;
    protected String _nextNamespaceURI;

    public XmlTokenStream(XMLStreamReader xmlReader, ContentReference sourceRef, int formatFeatures) {
        this._sourceReference = sourceRef;
        this._formatFeatures = formatFeatures;
        this._cfgProcessXsiNil = FromXmlParser.Feature.PROCESS_XSI_NIL.enabledIn(this._formatFeatures);
        this._xmlReader = Stax2ReaderAdapter.wrapIfNecessary((XMLStreamReader)xmlReader);
    }

    public int initialize() throws XMLStreamException {
        boolean startElementNext;
        if (this._xmlReader.getEventType() != 1) {
            throw new IllegalArgumentException("Invalid XMLStreamReader passed: should be pointing to START_ELEMENT (1), instead got " + this._xmlReader.getEventType());
        }
        this._localName = this._xmlReader.getLocalName();
        this._namespaceURI = this._xmlReader.getNamespaceURI();
        this._checkXsiAttributes();
        if (this._xsiNilFound || this._attributeCount > 0) {
            this._currentState = 1;
            return 1;
        }
        String text = this._collectUntilTag();
        if (text == null) {
            this._textValue = null;
            this._startElementAfterText = false;
            this._currentState = 7;
            return 7;
        }
        boolean bl = startElementNext = this._xmlReader.getEventType() == 1;
        if (startElementNext) {
            if (XmlTokenStream._allWs(text)) {
                this._textValue = null;
                this._currentState = 6;
                return 6;
            }
            this._textValue = text;
            this._currentState = 6;
            return 6;
        }
        this._startElementAfterText = false;
        this._textValue = text;
        this._currentState = 7;
        return 7;
    }

    public XMLStreamReader2 getXmlReader() {
        return this._xmlReader;
    }

    protected void setFormatFeatures(int f) {
        this._formatFeatures = f;
        this._cfgProcessXsiNil = FromXmlParser.Feature.PROCESS_XSI_NIL.enabledIn(f);
    }

    public int next() throws XMLStreamException {
        if (this._repeatCurrentToken) {
            this._repeatCurrentToken = false;
            return this._currentState;
        }
        if (this._repeatElement != 0) {
            this._currentState = this._handleRepeatElement();
            return this._currentState;
        }
        return this._next();
    }

    public void skipEndElement() throws IOException, XMLStreamException {
        int type = this.next();
        if (type != 2) {
            throw new IOException(String.format("Internal error: Expected END_ELEMENT, got event of type %s", this._stateDesc(type)));
        }
    }

    public int getCurrentToken() {
        return this._currentState;
    }

    public String getText() {
        return this._textValue;
    }

    public String getLocalName() {
        return this._localName;
    }

    public String getNamespaceURI() {
        return this._namespaceURI;
    }

    public boolean hasXsiNil() {
        return this._xsiNilFound;
    }

    public void closeCompletely() throws XMLStreamException {
        this._xmlReader.closeCompletely();
    }

    public void close() throws XMLStreamException {
        this._xmlReader.close();
    }

    public JsonLocation getCurrentLocation() {
        return this._extractLocation(this._xmlReader.getLocationInfo().getCurrentLocation());
    }

    public JsonLocation getTokenLocation() {
        return this._extractLocation(this._xmlReader.getLocationInfo().getStartLocation());
    }

    protected void repeatStartElement() {
        if (this._currentState != 1) {
            if (this._currentState == 2) {
                return;
            }
            throw new IllegalStateException("Current state not XML_START_ELEMENT but " + this._currentStateDesc());
        }
        this._currentWrapper = this._currentWrapper == null ? ElementWrapper.matchingWrapper(null, this._localName, this._namespaceURI) : ElementWrapper.matchingWrapper(this._currentWrapper.getParent(), this._localName, this._namespaceURI);
        this._repeatElement = 1;
    }

    protected void pushbackCurrentToken() {
        this._repeatCurrentToken = true;
    }

    protected void skipAttributes() {
        switch (this._currentState) {
            case 3: {
                this._attributeCount = 0;
                this._currentState = 1;
                break;
            }
            case 1: {
                break;
            }
            case 5: {
                break;
            }
            case 6: {
                break;
            }
            default: {
                throw new IllegalStateException("Current state not XML_START_ELEMENT or XML_ATTRIBUTE_NAME but " + this._currentStateDesc());
            }
        }
    }

    private final int _next() throws XMLStreamException {
        switch (this._currentState) {
            case 4: {
                ++this._nextAttributeIndex;
            }
            case 1: {
                boolean startElementNext;
                if (this._xsiNilFound) {
                    this._xsiNilFound = false;
                    this._xmlReader.skipElement();
                    return this._handleEndElement();
                }
                if (this._nextAttributeIndex < this._attributeCount) {
                    this._localName = this._xmlReader.getAttributeLocalName(this._nextAttributeIndex);
                    this._namespaceURI = this._xmlReader.getAttributeNamespace(this._nextAttributeIndex);
                    this._textValue = this._xmlReader.getAttributeValue(this._nextAttributeIndex);
                    this._currentState = 3;
                    return 3;
                }
                String text = this._collectUntilTag();
                boolean bl = startElementNext = this._xmlReader.getEventType() == 1;
                if (startElementNext) {
                    if (XmlTokenStream._allWs(text)) {
                        this._startElementAfterText = false;
                        return this._initStartElement();
                    }
                    this._startElementAfterText = true;
                    this._textValue = text;
                    this._currentState = 5;
                    return 5;
                }
                if (text != null) {
                    this._startElementAfterText = false;
                    this._textValue = text;
                    this._currentState = 5;
                    return 5;
                }
                this._startElementAfterText = false;
                return this._handleEndElement();
            }
            case 6: {
                if (this._textValue == null) {
                    return this._initStartElement();
                }
                this._startElementAfterText = true;
                this._currentState = 5;
                return 5;
            }
            case 3: {
                this._currentState = 4;
                return 4;
            }
            case 5: {
                if (this._startElementAfterText) {
                    this._startElementAfterText = false;
                    return this._initStartElement();
                }
                return this._handleEndElement();
            }
            case 7: {
                this.close();
                this._currentState = 8;
                return 8;
            }
            case 8: {
                return 8;
            }
        }
        switch (this._skipAndCollectTextUntilTag()) {
            case 8: {
                this.close();
                this._currentState = 8;
                return 8;
            }
            case 2: {
                if (!XmlTokenStream._allWs(this._textValue)) {
                    this._currentState = 5;
                    return 5;
                }
                return this._handleEndElement();
            }
        }
        if (!XmlTokenStream._allWs(this._textValue)) {
            this._startElementAfterText = true;
            this._currentState = 5;
            return 5;
        }
        return this._initStartElement();
    }

    private final String _collectUntilTag() throws XMLStreamException {
        if (this._xmlReader.isEmptyElement()) {
            this._xmlReader.next();
            if (FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL.enabledIn(this._formatFeatures)) {
                return null;
            }
            return "";
        }
        CharSequence chars = null;
        block5: while (true) {
            switch (this._xmlReader.next()) {
                case 1: {
                    return chars == null ? "" : chars.toString();
                }
                case 2: 
                case 8: {
                    return chars == null ? "" : chars.toString();
                }
                case 4: 
                case 12: {
                    String str = this._getText(this._xmlReader);
                    if (chars == null) {
                        chars = str;
                        continue block5;
                    }
                    if (chars instanceof String) {
                        chars = new StringBuilder(chars);
                    }
                    ((StringBuilder)chars).append(str);
                    continue block5;
                }
            }
        }
    }

    private final int _skipAndCollectTextUntilTag() throws XMLStreamException {
        CharSequence chars = null;
        while (this._xmlReader.hasNext()) {
            int type = this._xmlReader.next();
            switch (type) {
                case 1: 
                case 2: 
                case 8: {
                    this._textValue = chars == null ? "" : chars.toString();
                    return type;
                }
                case 4: 
                case 12: {
                    String str = this._getText(this._xmlReader);
                    if (chars == null) {
                        chars = str;
                        break;
                    }
                    if (chars instanceof String) {
                        chars = new StringBuilder(chars);
                    }
                    ((StringBuilder)chars).append(str);
                    break;
                }
            }
        }
        throw new IllegalStateException("Expected to find a tag, instead reached end of input");
    }

    private final String _getText(XMLStreamReader2 r) throws XMLStreamException {
        try {
            return r.getText();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof XMLStreamException) {
                throw (XMLStreamException)cause;
            }
            throw e;
        }
    }

    private final int _initStartElement() throws XMLStreamException {
        String ns = this._xmlReader.getNamespaceURI();
        String localName = this._xmlReader.getLocalName();
        this._checkXsiAttributes();
        if (this._currentWrapper != null) {
            if (this._currentWrapper.matchesWrapper(localName, ns)) {
                this._currentWrapper = this._currentWrapper.intermediateWrapper();
            } else {
                this._localName = this._currentWrapper.getWrapperLocalName();
                this._namespaceURI = this._currentWrapper.getWrapperNamespace();
                this._currentWrapper = this._currentWrapper.getParent();
                this._nextLocalName = localName;
                this._nextNamespaceURI = ns;
                this._repeatElement = 3;
                this._currentState = 2;
                return 2;
            }
        }
        this._localName = localName;
        this._namespaceURI = ns;
        this._currentState = 1;
        return 1;
    }

    private final void _checkXsiAttributes() {
        int count;
        this._attributeCount = count = this._xmlReader.getAttributeCount();
        if (count >= 1 && this._cfgProcessXsiNil && "nil".equals(this._xmlReader.getAttributeLocalName(0)) && XSI_NAMESPACE.equals(this._xmlReader.getAttributeNamespace(0))) {
            this._nextAttributeIndex = 1;
            this._xsiNilFound = "true".equals(this._xmlReader.getAttributeValue(0));
            return;
        }
        this._nextAttributeIndex = 0;
        this._xsiNilFound = false;
    }

    protected int _handleRepeatElement() throws XMLStreamException {
        int type = this._repeatElement;
        this._repeatElement = 0;
        if (type == 1) {
            this._currentWrapper = this._currentWrapper.intermediateWrapper();
            return 1;
        }
        if (type == 2) {
            this._localName = this._xmlReader.getLocalName();
            this._namespaceURI = this._xmlReader.getNamespaceURI();
            if (this._currentWrapper != null) {
                this._currentWrapper = this._currentWrapper.getParent();
            }
            return 2;
        }
        if (type == 3) {
            if (this._currentWrapper != null) {
                this._currentWrapper = this._currentWrapper.intermediateWrapper();
            }
            this._localName = this._nextLocalName;
            this._namespaceURI = this._nextNamespaceURI;
            this._nextLocalName = null;
            this._nextNamespaceURI = null;
            return 1;
        }
        throw new IllegalStateException("Unrecognized type to repeat: " + type);
    }

    private final int _handleEndElement() {
        if (this._currentWrapper != null) {
            ElementWrapper w = this._currentWrapper;
            if (w.isMatching()) {
                this._repeatElement = 2;
                this._localName = w.getWrapperLocalName();
                this._namespaceURI = w.getWrapperNamespace();
                this._currentWrapper = this._currentWrapper.getParent();
            } else {
                this._currentWrapper = this._currentWrapper.getParent();
                this._localName = "";
                this._namespaceURI = "";
            }
        } else {
            this._localName = "";
            this._namespaceURI = "";
        }
        this._currentState = 2;
        return 2;
    }

    private JsonLocation _extractLocation(XMLStreamLocation2 location) {
        if (location == null) {
            return new JsonLocation(this._sourceReference, -1L, -1, -1);
        }
        return new JsonLocation(this._sourceReference, (long)location.getCharacterOffset(), location.getLineNumber(), location.getColumnNumber());
    }

    protected static boolean _allWs(String str) {
        int len;
        int n = len = str == null ? 0 : str.length();
        if (len > 0) {
            for (int i = 0; i < len; ++i) {
                if (str.charAt(i) <= ' ') continue;
                return false;
            }
        }
        return true;
    }

    protected String _currentStateDesc() {
        return this._stateDesc(this._currentState);
    }

    protected String _stateDesc(int state) {
        switch (state) {
            case 1: {
                return "XML_START_ELEMENT";
            }
            case 2: {
                return "XML_END_ELEMENT";
            }
            case 3: {
                return "XML_ATTRIBUTE_NAME";
            }
            case 4: {
                return "XML_ATTRIBUTE_VALUE";
            }
            case 5: {
                return "XML_TEXT";
            }
            case 6: {
                return "XML_START_ELEMENT_DELAYED";
            }
            case 7: {
                return "XML_ROOT_TEXT";
            }
            case 8: {
                return "XML_END";
            }
        }
        return "N/A (" + this._currentState + ")";
    }
}

