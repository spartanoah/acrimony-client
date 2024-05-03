/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.xml.deser.WrapperHandlingDeserializer;
import com.fasterxml.jackson.dataformat.xml.deser.XmlTextDeserializer;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XmlBeanDeserializerModifier
extends BeanDeserializerModifier
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String _cfgNameForTextValue = "";

    public XmlBeanDeserializerModifier(String nameForTextValue) {
        this._cfgNameForTextValue = nameForTextValue;
    }

    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int changed = 0;
        int propCount = propDefs.size();
        for (int i = 0; i < propCount; ++i) {
            String localName;
            BeanPropertyDefinition prop = propDefs.get(i);
            AnnotatedMember acc = prop.getPrimaryMember();
            if (acc == null) continue;
            Boolean b = AnnotationUtil.findIsTextAnnotation(config, intr, acc);
            if (b != null && b.booleanValue()) {
                BeanPropertyDefinition newProp = prop.withSimpleName(this._cfgNameForTextValue);
                if (newProp == prop) continue;
                propDefs.set(i, newProp);
                continue;
            }
            PropertyName wrapperName = prop.getWrapperName();
            if (wrapperName == null || wrapperName == PropertyName.NO_NAME || (localName = wrapperName.getSimpleName()) == null || localName.length() <= 0 || localName.equals(prop.getName())) continue;
            if (changed == 0) {
                propDefs = new ArrayList<BeanPropertyDefinition>(propDefs);
            }
            ++changed;
            propDefs.set(i, prop.withSimpleName(localName));
        }
        return propDefs;
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deser0) {
        SettableBeanProperty textProp;
        if (!(deser0 instanceof BeanDeserializerBase)) {
            return deser0;
        }
        BeanDeserializerBase deser = (BeanDeserializerBase)deser0;
        ValueInstantiator inst = deser.getValueInstantiator();
        if (!inst.canCreateFromString() && (textProp = this._findSoleTextProp(config, deser.properties())) != null) {
            return new XmlTextDeserializer(deser, textProp);
        }
        return new WrapperHandlingDeserializer(deser);
    }

    private SettableBeanProperty _findSoleTextProp(DeserializationConfig config, Iterator<SettableBeanProperty> propIt) {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        SettableBeanProperty textProp = null;
        while (propIt.hasNext()) {
            SettableBeanProperty prop = propIt.next();
            AnnotatedMember m = prop.getMember();
            if (m != null) {
                PropertyName n = prop.getFullName();
                if (this._cfgNameForTextValue.equals(n.getSimpleName())) {
                    textProp = prop;
                    continue;
                }
                Boolean b = AnnotationUtil.findIsAttributeAnnotation(config, ai, m);
                if (b != null && b.booleanValue()) continue;
            }
            return null;
        }
        return textProp;
    }
}

