/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormatVisitor;

public interface JsonNumberFormatVisitor
extends JsonValueFormatVisitor {
    public void numberType(JsonParser.NumberType var1);

    public static class Base
    extends JsonValueFormatVisitor.Base
    implements JsonNumberFormatVisitor {
        @Override
        public void numberType(JsonParser.NumberType type) {
        }
    }
}

