/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.jaxb;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlAnnotationIntrospector;

@Deprecated
public class XmlJaxbAnnotationIntrospector
extends NopAnnotationIntrospector
implements XmlAnnotationIntrospector {
    private static final long serialVersionUID = 1L;

    @Deprecated
    public XmlJaxbAnnotationIntrospector() {
    }

    public XmlJaxbAnnotationIntrospector(TypeFactory typeFactory) {
    }

    public String findNamespace(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    public Boolean isOutputAsAttribute(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    public Boolean isOutputAsText(MapperConfig<?> config, Annotated ann) {
        return null;
    }

    public Boolean isOutputAsCData(MapperConfig<?> config, Annotated ann) {
        return null;
    }
}

