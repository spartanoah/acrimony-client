/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class XmlSerializerProvider
extends DefaultSerializerProvider {
    private static final long serialVersionUID = 1L;
    protected final XmlRootNameLookup _rootNameLookup;

    public XmlSerializerProvider(XmlRootNameLookup rootNames) {
        this._rootNameLookup = rootNames;
    }

    public XmlSerializerProvider(XmlSerializerProvider src, SerializationConfig config, SerializerFactory f) {
        super(src, config, f);
        this._rootNameLookup = src._rootNameLookup;
    }

    protected XmlSerializerProvider(XmlSerializerProvider src) {
        super(src);
        this._rootNameLookup = new XmlRootNameLookup();
    }

    @Override
    public DefaultSerializerProvider copy() {
        return new XmlSerializerProvider(this);
    }

    @Override
    public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
        return new XmlSerializerProvider(this, config, jsf);
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value) throws IOException {
        boolean asArray;
        this._generator = gen;
        if (value == null) {
            this._serializeXmlNull(gen);
            return;
        }
        Class<?> cls = value.getClass();
        ToXmlGenerator xgen = this._asXmlGenerator(gen);
        if (xgen == null) {
            asArray = false;
        } else {
            if (this._shouldUnwrapObjectNode(xgen, value)) {
                this._serializeUnwrappedObjectNode(xgen, value, null);
                return;
            }
            QName rootName = this._rootNameFromConfig();
            if (rootName == null) {
                rootName = this._rootNameLookup.findRootName(cls, this._config);
            }
            this._initWithRootName(xgen, rootName);
            asArray = TypeUtil.isIndexedType(cls);
            if (asArray) {
                this._startRootArray(xgen, rootName);
            }
        }
        JsonSerializer<Object> ser = this.findTypedValueSerializer(cls, true, null);
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) {
            throw this._wrapAsIOE(gen, e);
        }
        if (asArray) {
            gen.writeEndObject();
        }
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType) throws IOException {
        this.serializeValue(gen, value, rootType, null);
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType, JsonSerializer<Object> ser) throws IOException {
        boolean asArray;
        ToXmlGenerator xgen;
        this._generator = gen;
        if (value == null) {
            this._serializeXmlNull(gen);
            return;
        }
        if (rootType != null && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            this._reportIncompatibleRootType(value, rootType);
        }
        if ((xgen = this._asXmlGenerator(gen)) == null) {
            asArray = false;
        } else {
            if (this._shouldUnwrapObjectNode(xgen, value)) {
                this._serializeUnwrappedObjectNode(xgen, value, ser);
                return;
            }
            QName rootName = this._rootNameFromConfig();
            if (rootName == null) {
                rootName = rootType == null ? this._rootNameLookup.findRootName(value.getClass(), this._config) : this._rootNameLookup.findRootName(rootType, this._config);
            }
            this._initWithRootName(xgen, rootName);
            boolean bl = asArray = rootType == null ? TypeUtil.isIndexedType(value.getClass()) : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                this._startRootArray(xgen, rootName);
            }
        }
        if (ser == null) {
            ser = this.findTypedValueSerializer(rootType, true, null);
        }
        try {
            ser.serialize(value, gen, this);
        } catch (Exception e) {
            throw this._wrapAsIOE(gen, e);
        }
        if (asArray) {
            gen.writeEndObject();
        }
    }

    @Override
    public void serializePolymorphic(JsonGenerator gen, Object value, JavaType rootType, JsonSerializer<Object> valueSer, TypeSerializer typeSer) throws IOException {
        boolean asArray;
        ToXmlGenerator xgen;
        this._generator = gen;
        if (value == null) {
            this._serializeXmlNull(gen);
            return;
        }
        if (rootType != null && !rootType.getRawClass().isAssignableFrom(value.getClass())) {
            this._reportIncompatibleRootType(value, rootType);
        }
        if ((xgen = this._asXmlGenerator(gen)) == null) {
            asArray = false;
        } else {
            QName rootName = this._rootNameFromConfig();
            if (rootName == null) {
                rootName = rootType == null ? this._rootNameLookup.findRootName(value.getClass(), this._config) : this._rootNameLookup.findRootName(rootType, this._config);
            }
            this._initWithRootName(xgen, rootName);
            boolean bl = asArray = rootType == null ? TypeUtil.isIndexedType(value.getClass()) : TypeUtil.isIndexedType(rootType);
            if (asArray) {
                this._startRootArray(xgen, rootName);
            }
        }
        if (valueSer == null) {
            valueSer = rootType != null && rootType.isContainerType() ? this.findValueSerializer(rootType, null) : this.findValueSerializer(value.getClass(), null);
        }
        try {
            valueSer.serializeWithType(value, gen, this, typeSer);
        } catch (Exception e) {
            throw this._wrapAsIOE(gen, e);
        }
        if (asArray) {
            gen.writeEndObject();
        }
    }

    protected void _serializeXmlNull(JsonGenerator gen) throws IOException {
        QName rootName = this._rootNameFromConfig();
        if (rootName == null) {
            rootName = XmlRootNameLookup.ROOT_NAME_FOR_NULL;
        }
        if (gen instanceof ToXmlGenerator) {
            this._initWithRootName((ToXmlGenerator)gen, rootName);
        }
        super.serializeValue(gen, null);
    }

    protected void _startRootArray(ToXmlGenerator xgen, QName rootName) throws IOException {
        xgen.writeStartObject();
        xgen.writeFieldName("item");
    }

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws IOException {
        if (!xgen.setNextNameIfMissing(rootName) && xgen.inRoot()) {
            xgen.setNextName(rootName);
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        if (ns != null && ns.length() > 0) {
            try {
                xgen.getStaxWriter().setDefaultNamespace(ns);
            } catch (XMLStreamException e) {
                StaxUtil.throwAsGenerationException(e, xgen);
            }
        }
    }

    protected QName _rootNameFromConfig() {
        PropertyName name = this._config.getFullRootName();
        if (name == null) {
            return null;
        }
        String ns = name.getNamespace();
        if (ns == null || ns.isEmpty()) {
            return new QName(name.getSimpleName());
        }
        return new QName(ns, name.getSimpleName());
    }

    protected boolean _shouldUnwrapObjectNode(ToXmlGenerator xgen, Object value) {
        return xgen.isEnabled(ToXmlGenerator.Feature.UNWRAP_ROOT_OBJECT_NODE) && value instanceof ObjectNode && ((ObjectNode)value).size() == 1;
    }

    protected void _serializeUnwrappedObjectNode(ToXmlGenerator xgen, Object value, JsonSerializer<Object> ser) throws IOException {
        ObjectNode root = (ObjectNode)value;
        Map.Entry<String, JsonNode> entry = root.fields().next();
        JsonNode newRoot = entry.getValue();
        this._initWithRootName(xgen, new QName(entry.getKey()));
        if (ser == null) {
            ser = this.findTypedValueSerializer(newRoot.getClass(), true, null);
        }
        try {
            ser.serialize(newRoot, xgen, this);
        } catch (Exception e) {
            throw this._wrapAsIOE(xgen, e);
        }
    }

    protected ToXmlGenerator _asXmlGenerator(JsonGenerator gen) throws JsonMappingException {
        if (!(gen instanceof ToXmlGenerator)) {
            if (gen instanceof TokenBuffer) {
                return null;
            }
            throw JsonMappingException.from(gen, "XmlMapper does not work with generators of type other than `ToXmlGenerator`; got: `" + gen.getClass().getName() + "`");
        }
        return (ToXmlGenerator)gen;
    }

    protected IOException _wrapAsIOE(JsonGenerator g, Exception e) {
        if (e instanceof IOException) {
            return (IOException)e;
        }
        String msg = e.getMessage();
        if (msg == null) {
            msg = "[no message for " + e.getClass().getName() + "]";
        }
        return new JsonMappingException((Closeable)g, msg, (Throwable)e);
    }
}

