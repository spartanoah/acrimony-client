/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;

public class StackTraceElementDeserializer
extends StdScalarDeserializer<StackTraceElement> {
    private static final long serialVersionUID = 1L;

    public StackTraceElementDeserializer() {
        super(StackTraceElement.class);
    }

    @Override
    public StackTraceElement deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.START_OBJECT) {
            String className = "";
            String methodName = "";
            String fileName = "";
            String moduleName = null;
            String moduleVersion = null;
            String classLoaderName = null;
            int lineNumber = -1;
            while ((t = p.nextValue()) != JsonToken.END_OBJECT) {
                String propName = p.getCurrentName();
                if ("className".equals(propName)) {
                    className = p.getText();
                } else if ("classLoaderName".equals(propName)) {
                    classLoaderName = p.getText();
                } else if ("fileName".equals(propName)) {
                    fileName = p.getText();
                } else if ("lineNumber".equals(propName)) {
                    lineNumber = t.isNumeric() ? p.getIntValue() : this._parseIntPrimitive(p, ctxt);
                } else if ("methodName".equals(propName)) {
                    methodName = p.getText();
                } else if (!"nativeMethod".equals(propName)) {
                    if ("moduleName".equals(propName)) {
                        moduleName = p.getText();
                    } else if ("moduleVersion".equals(propName)) {
                        moduleVersion = p.getText();
                    } else if (!"declaringClass".equals(propName) && !"format".equals(propName)) {
                        this.handleUnknownProperty(p, ctxt, this._valueClass, propName);
                    }
                }
                p.skipChildren();
            }
            return this.constructValue(ctxt, className, methodName, fileName, lineNumber, moduleName, moduleVersion, classLoaderName);
        }
        if (t == JsonToken.START_ARRAY && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            p.nextToken();
            StackTraceElement value = this.deserialize(p, ctxt);
            if (p.nextToken() != JsonToken.END_ARRAY) {
                this.handleMissingEndArrayForSingle(p, ctxt);
            }
            return value;
        }
        return (StackTraceElement)ctxt.handleUnexpectedToken(this._valueClass, p);
    }

    @Deprecated
    protected StackTraceElement constructValue(DeserializationContext ctxt, String className, String methodName, String fileName, int lineNumber, String moduleName, String moduleVersion) {
        return this.constructValue(ctxt, className, methodName, fileName, lineNumber, moduleName, moduleVersion, null);
    }

    protected StackTraceElement constructValue(DeserializationContext ctxt, String className, String methodName, String fileName, int lineNumber, String moduleName, String moduleVersion, String classLoaderName) {
        return new StackTraceElement(className, methodName, fileName, lineNumber);
    }
}

