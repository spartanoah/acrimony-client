/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.AnnotationIntrospector$XmlExtensions
 */
package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.Serializable;
import javax.xml.namespace.QName;

public class XmlRootNameLookup
implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final QName ROOT_NAME_FOR_NULL = new QName("null");
    protected final transient LRUMap<ClassKey, QName> _rootNames = new LRUMap(40, 200);

    protected Object readResolve() {
        if (this._rootNames == null) {
            return new XmlRootNameLookup();
        }
        return this;
    }

    public QName findRootName(JavaType rootType, MapperConfig<?> config) {
        return this.findRootName(rootType.getRawClass(), config);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public QName findRootName(Class<?> rootType, MapperConfig<?> config) {
        QName name;
        ClassKey key = new ClassKey(rootType);
        LRUMap<ClassKey, QName> lRUMap = this._rootNames;
        synchronized (lRUMap) {
            name = this._rootNames.get(key);
        }
        if (name != null) {
            return name;
        }
        name = this._findRootName(config, rootType);
        lRUMap = this._rootNames;
        synchronized (lRUMap) {
            this._rootNames.put(key, name);
        }
        return name;
    }

    protected QName _findRootName(MapperConfig<?> config, Class<?> rootType) {
        BeanDescription beanDesc = config.introspectClassAnnotations(rootType);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedClass ac = beanDesc.getClassInfo();
        String localName = null;
        String ns = null;
        PropertyName root = intr.findRootName(ac);
        if (root != null) {
            localName = root.getSimpleName();
            ns = root.getNamespace();
        }
        if (localName == null || localName.length() == 0) {
            localName = StaxUtil.sanitizeXmlTypeName(rootType.getSimpleName());
            return this._qname(ns, localName);
        }
        if (ns == null || ns.isEmpty()) {
            ns = this._findNamespace(config, intr, ac);
        }
        return this._qname(ns, localName);
    }

    private QName _qname(String ns, String localName) {
        if (ns == null) {
            ns = "";
        }
        return new QName(ns, localName);
    }

    private String _findNamespace(MapperConfig<?> config, AnnotationIntrospector ai, AnnotatedClass ann) {
        for (AnnotationIntrospector intr : ai.allIntrospectors()) {
            String ns;
            if (!(intr instanceof AnnotationIntrospector.XmlExtensions) || (ns = ((AnnotationIntrospector.XmlExtensions)intr).findNamespace(config, (Annotated)ann)) == null) continue;
            return ns;
        }
        return null;
    }
}

