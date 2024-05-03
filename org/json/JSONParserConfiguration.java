/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import org.json.ParserConfiguration;

public class JSONParserConfiguration
extends ParserConfiguration {
    @Override
    protected JSONParserConfiguration clone() {
        return new JSONParserConfiguration();
    }

    public JSONParserConfiguration withMaxNestingDepth(int maxNestingDepth) {
        return (JSONParserConfiguration)super.withMaxNestingDepth(maxNestingDepth);
    }
}

