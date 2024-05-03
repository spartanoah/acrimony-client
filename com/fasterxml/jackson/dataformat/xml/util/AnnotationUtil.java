/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.AnnotationIntrospector$XmlExtensions
 */
package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;

public class AnnotationUtil {
    public static String findNamespaceAnnotation(MapperConfig<?> config, AnnotationIntrospector ai, AnnotatedMember prop) {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            String ns;
            if (!(intr instanceof AnnotationIntrospector.XmlExtensions) || (ns = ((AnnotationIntrospector.XmlExtensions)intr).findNamespace(config, (Annotated)prop)) == null) continue;
            return ns;
        }
        return null;
    }

    public static Boolean findIsAttributeAnnotation(MapperConfig<?> config, AnnotationIntrospector ai, AnnotatedMember prop) {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            Boolean b;
            if (!(intr instanceof AnnotationIntrospector.XmlExtensions) || (b = ((AnnotationIntrospector.XmlExtensions)intr).isOutputAsAttribute(config, (Annotated)prop)) == null) continue;
            return b;
        }
        return null;
    }

    public static Boolean findIsTextAnnotation(MapperConfig<?> config, AnnotationIntrospector ai, AnnotatedMember prop) {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            Boolean b;
            if (!(intr instanceof AnnotationIntrospector.XmlExtensions) || (b = ((AnnotationIntrospector.XmlExtensions)intr).isOutputAsText(config, (Annotated)prop)) == null) continue;
            return b;
        }
        return null;
    }

    public static Boolean findIsCDataAnnotation(MapperConfig<?> config, AnnotationIntrospector ai, AnnotatedMember prop) {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            Boolean b;
            if (!(intr instanceof AnnotationIntrospector.XmlExtensions) || (b = ((AnnotationIntrospector.XmlExtensions)intr).isOutputAsCData(config, (Annotated)prop)) == null) continue;
            return b;
        }
        return null;
    }
}

