/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;
import java.io.IOException;
import java.util.BitSet;
import java.util.Set;
import javax.xml.namespace.QName;

public abstract class XmlBeanSerializerBase
extends BeanSerializerBase {
    public static final String KEY_XML_INFO = new String("xmlInfo");
    protected final int _attributeCount;
    protected final int _textPropertyIndex;
    protected final QName[] _xmlNames;
    protected final BitSet _cdata;

    public XmlBeanSerializerBase(BeanSerializerBase src) {
        super(src);
        int attrCount = 0;
        for (BeanPropertyWriter bpw : this._props) {
            if (!XmlBeanSerializerBase._isAttribute(bpw)) continue;
            attrCount = XmlBeanSerializerBase._orderAttributesFirst(this._props, this._filteredProps);
            break;
        }
        this._attributeCount = attrCount;
        BitSet cdata = null;
        int len = this._props.length;
        for (int i = 0; i < len; ++i) {
            BeanPropertyWriter bpw;
            bpw = this._props[i];
            if (!XmlBeanSerializerBase._isCData(bpw)) continue;
            if (cdata == null) {
                cdata = new BitSet(len);
            }
            cdata.set(i);
        }
        this._cdata = cdata;
        this._xmlNames = new QName[this._props.length];
        int textIndex = -1;
        int len2 = this._props.length;
        for (int i = 0; i < len2; ++i) {
            BeanPropertyWriter bpw = this._props[i];
            XmlInfo info = (XmlInfo)bpw.getInternalSetting(KEY_XML_INFO);
            String ns = null;
            if (info != null) {
                ns = info.getNamespace();
                if (textIndex < 0 && info.isText()) {
                    textIndex = i;
                }
            }
            this._xmlNames[i] = new QName(ns == null ? "" : ns, bpw.getName());
        }
        this._textPropertyIndex = textIndex;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super((BeanSerializerBase)src, objectIdWriter);
        this._attributeCount = src._attributeCount;
        this._textPropertyIndex = src._textPropertyIndex;
        this._xmlNames = src._xmlNames;
        this._cdata = src._cdata;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super((BeanSerializerBase)src, objectIdWriter, filterId);
        this._attributeCount = src._attributeCount;
        this._textPropertyIndex = src._textPropertyIndex;
        this._xmlNames = src._xmlNames;
        this._cdata = src._cdata;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, Set<String> toIgnore, Set<String> toInclude) {
        super((BeanSerializerBase)src, toIgnore, toInclude);
        this._attributeCount = src._attributeCount;
        this._textPropertyIndex = src._textPropertyIndex;
        this._xmlNames = src._xmlNames;
        this._cdata = src._cdata;
    }

    public XmlBeanSerializerBase(XmlBeanSerializerBase src, NameTransformer transformer) {
        super((BeanSerializerBase)src, transformer);
        this._attributeCount = src._attributeCount;
        this._textPropertyIndex = src._textPropertyIndex;
        this._xmlNames = src._xmlNames;
        this._cdata = src._cdata;
    }

    protected XmlBeanSerializerBase(XmlBeanSerializerBase src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super((BeanSerializerBase)src, properties, filteredProperties);
        this._attributeCount = src._attributeCount;
        this._textPropertyIndex = src._textPropertyIndex;
        this._xmlNames = src._xmlNames;
        this._cdata = src._cdata;
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator gen0, SerializerProvider provider) throws IOException {
        int i;
        if (!(gen0 instanceof ToXmlGenerator)) {
            super.serializeFields(bean, gen0, provider);
            return;
        }
        ToXmlGenerator xgen = (ToXmlGenerator)gen0;
        BeanPropertyWriter[] props = this._filteredProps != null && provider.getActiveView() != null ? this._filteredProps : this._props;
        int attrCount = this._attributeCount;
        boolean isAttribute = xgen._nextIsAttribute;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        int textIndex = this._textPropertyIndex;
        QName[] xmlNames = this._xmlNames;
        BitSet cdata = this._cdata;
        try {
            int len = props.length;
            for (i = 0; i < len; ++i) {
                if (!(i != attrCount || isAttribute && this.isUnwrappingSerializer())) {
                    xgen.setNextIsAttribute(false);
                }
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop != null) {
                    if (cdata != null && cdata.get(i)) {
                        xgen.setNextIsCData(true);
                        prop.serializeAsField(bean, xgen, provider);
                        xgen.setNextIsCData(false);
                    } else {
                        prop.serializeAsField(bean, xgen, provider);
                    }
                }
                if (i != textIndex) continue;
                xgen.setNextIsUnwrapped(false);
            }
            if (this._anyGetterWriter != null) {
                xgen.setNextIsAttribute(false);
                this._anyGetterWriter.getAndSerialize(bean, xgen, provider);
            }
        } catch (Exception e) {
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, (Throwable)e, bean, name);
        } catch (StackOverflowError e) {
            JsonMappingException mapE = JsonMappingException.from(gen0, "Infinite recursion (StackOverflowError)");
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }

    @Override
    protected void serializeFieldsFiltered(Object bean, JsonGenerator gen0, SerializerProvider provider) throws IOException {
        int i;
        if (!(gen0 instanceof ToXmlGenerator)) {
            super.serializeFieldsFiltered(bean, gen0, provider);
            return;
        }
        ToXmlGenerator xgen = (ToXmlGenerator)gen0;
        BeanPropertyWriter[] props = this._filteredProps != null && provider.getActiveView() != null ? this._filteredProps : this._props;
        PropertyFilter filter = this.findPropertyFilter(provider, this._propertyFilterId, bean);
        if (filter == null) {
            this.serializeFields(bean, gen0, provider);
            return;
        }
        boolean isAttribute = xgen._nextIsAttribute;
        int attrCount = this._attributeCount;
        if (attrCount > 0) {
            xgen.setNextIsAttribute(true);
        }
        int textIndex = this._textPropertyIndex;
        QName[] xmlNames = this._xmlNames;
        BitSet cdata = this._cdata;
        try {
            int len = props.length;
            for (i = 0; i < len; ++i) {
                if (!(i != attrCount || isAttribute && this.isUnwrappingSerializer())) {
                    xgen.setNextIsAttribute(false);
                }
                if (i == textIndex) {
                    xgen.setNextIsUnwrapped(true);
                }
                xgen.setNextName(xmlNames[i]);
                BeanPropertyWriter prop = props[i];
                if (prop == null) continue;
                if (cdata != null && cdata.get(i)) {
                    xgen.setNextIsCData(true);
                    filter.serializeAsField(bean, xgen, provider, prop);
                    xgen.setNextIsCData(false);
                    continue;
                }
                filter.serializeAsField(bean, xgen, provider, prop);
            }
            if (this._anyGetterWriter != null) {
                xgen.setNextIsAttribute(false);
                this._anyGetterWriter.getAndFilter(bean, xgen, provider, filter);
            }
        } catch (Exception e) {
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, (Throwable)e, bean, name);
        } catch (StackOverflowError e) {
            JsonMappingException mapE = JsonMappingException.from(gen0, "Infinite recursion (StackOverflowError)", (Throwable)e);
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        if (this._objectIdWriter != null) {
            this._serializeWithObjectId(bean, gen, provider, typeSer);
            return;
        }
        if (typeSer.getTypeInclusion() == JsonTypeInfo.As.PROPERTY) {
            ToXmlGenerator xgen = (ToXmlGenerator)gen;
            xgen.setNextIsAttribute(true);
            super.serializeWithType(bean, gen, provider, typeSer);
            if (this._attributeCount == 0) {
                xgen.setNextIsAttribute(false);
            }
        } else {
            super.serializeWithType(bean, gen, provider, typeSer);
        }
    }

    @Override
    protected void _serializeObjectId(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer, WritableObjectId objectId) throws IOException {
        if (typeSer.getTypeInclusion() == JsonTypeInfo.As.PROPERTY) {
            ToXmlGenerator xgen = (ToXmlGenerator)gen;
            xgen.setNextIsAttribute(true);
            super._serializeObjectId(bean, gen, provider, typeSer, objectId);
            if (this._attributeCount == 0) {
                xgen.setNextIsAttribute(false);
            }
        } else {
            super._serializeObjectId(bean, gen, provider, typeSer, objectId);
        }
    }

    protected static boolean _isAttribute(BeanPropertyWriter bpw) {
        XmlInfo info = (XmlInfo)bpw.getInternalSetting(KEY_XML_INFO);
        return info != null && info.isAttribute();
    }

    protected static boolean _isCData(BeanPropertyWriter bpw) {
        XmlInfo info = (XmlInfo)bpw.getInternalSetting(KEY_XML_INFO);
        return info != null && info.isCData();
    }

    protected static int _orderAttributesFirst(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        int attrCount = 0;
        int len = properties.length;
        for (int i = 0; i < len; ++i) {
            BeanPropertyWriter bpw = properties[i];
            if (!XmlBeanSerializerBase._isAttribute(bpw)) continue;
            int moveBy = i - attrCount;
            if (moveBy > 0) {
                System.arraycopy(properties, attrCount, properties, attrCount + 1, moveBy);
                properties[attrCount] = bpw;
                if (filteredProperties != null) {
                    BeanPropertyWriter fbpw = filteredProperties[i];
                    System.arraycopy(filteredProperties, attrCount, filteredProperties, attrCount + 1, moveBy);
                    filteredProperties[attrCount] = fbpw;
                }
            }
            ++attrCount;
        }
        return attrCount;
    }
}

