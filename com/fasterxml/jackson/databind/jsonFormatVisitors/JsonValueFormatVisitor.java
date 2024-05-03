/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import java.util.Set;

public interface JsonValueFormatVisitor {
    public void format(JsonValueFormat var1);

    public void enumTypes(Set<String> var1);

    public static class Base
    implements JsonValueFormatVisitor {
        @Override
        public void format(JsonValueFormat format) {
        }

        @Override
        public void enumTypes(Set<String> enums) {
        }
    }
}

