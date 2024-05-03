/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.AnnotationIntrospector$XmlExtensions
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

public interface XmlAnnotationIntrospector
extends AnnotationIntrospector.XmlExtensions {

    public static class Pair
    extends AnnotationIntrospectorPair
    implements XmlAnnotationIntrospector {
        private static final long serialVersionUID = 1L;
        protected final AnnotationIntrospector.XmlExtensions _xmlPrimary;
        protected final AnnotationIntrospector.XmlExtensions _xmlSecondary;

        public Pair(AnnotationIntrospector p, AnnotationIntrospector s) {
            super(p, s);
            this._xmlPrimary = p instanceof AnnotationIntrospector.XmlExtensions ? (AnnotationIntrospector.XmlExtensions)p : null;
            this._xmlSecondary = s instanceof AnnotationIntrospector.XmlExtensions ? (AnnotationIntrospector.XmlExtensions)s : null;
        }

        public static Pair instance(AnnotationIntrospector a1, AnnotationIntrospector a2) {
            return new Pair(a1, a2);
        }

        public String findNamespace(MapperConfig<?> config, Annotated ann) {
            String value;
            String string = value = this._xmlPrimary == null ? null : this._xmlPrimary.findNamespace(config, ann);
            if (value == null && this._xmlSecondary != null) {
                value = this._xmlSecondary.findNamespace(config, ann);
            }
            return value;
        }

        public Boolean isOutputAsAttribute(MapperConfig<?> config, Annotated ann) {
            Boolean value;
            Boolean bl = value = this._xmlPrimary == null ? null : this._xmlPrimary.isOutputAsAttribute(config, ann);
            if (value == null && this._xmlSecondary != null) {
                value = this._xmlSecondary.isOutputAsAttribute(config, ann);
            }
            return value;
        }

        public Boolean isOutputAsText(MapperConfig<?> config, Annotated ann) {
            Boolean value;
            Boolean bl = value = this._xmlPrimary == null ? null : this._xmlPrimary.isOutputAsText(config, ann);
            if (value == null && this._xmlSecondary != null) {
                value = this._xmlSecondary.isOutputAsText(config, ann);
            }
            return value;
        }

        public Boolean isOutputAsCData(MapperConfig<?> config, Annotated ann) {
            Boolean value;
            Boolean bl = value = this._xmlPrimary == null ? null : this._xmlPrimary.isOutputAsCData(config, ann);
            if (value == null && this._xmlSecondary != null) {
                value = this._xmlSecondary.isOutputAsCData(config, ann);
            }
            return value;
        }
    }
}

