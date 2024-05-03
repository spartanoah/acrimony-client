/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanPropertyWriter;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerBase;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import com.fasterxml.jackson.dataformat.xml.util.XmlInfo;
import java.io.Serializable;
import java.util.List;

public class XmlBeanSerializerModifier
extends BeanSerializerModifier
implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int len = beanProperties.size();
        for (int i = 0; i < len; ++i) {
            BeanPropertyWriter bpw = beanProperties.get(i);
            AnnotatedMember member = bpw.getMember();
            String ns = AnnotationUtil.findNamespaceAnnotation(config, intr, member);
            Boolean isAttribute = AnnotationUtil.findIsAttributeAnnotation(config, intr, member);
            Boolean isText = AnnotationUtil.findIsTextAnnotation(config, intr, member);
            Boolean isCData = AnnotationUtil.findIsCDataAnnotation(config, intr, member);
            bpw.setInternalSetting(XmlBeanSerializerBase.KEY_XML_INFO, new XmlInfo(isAttribute, ns, isText, isCData));
            if (!TypeUtil.isIndexedType(bpw.getType())) continue;
            PropertyName wrappedName = PropertyName.construct(bpw.getName(), ns);
            PropertyName wrapperName = bpw.getWrapperName();
            if (wrapperName == null || wrapperName == PropertyName.NO_NAME) continue;
            String localName = wrapperName.getSimpleName();
            if (localName == null || localName.length() == 0) {
                wrapperName = wrappedName;
            }
            beanProperties.set(i, new XmlBeanPropertyWriter(bpw, wrapperName, wrappedName));
        }
        return beanProperties;
    }

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (!(serializer instanceof BeanSerializerBase)) {
            return serializer;
        }
        return new XmlBeanSerializer((BeanSerializerBase)serializer);
    }
}

