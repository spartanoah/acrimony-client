/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.parser;

import org.apache.logging.log4j.core.jackson.Log4jJsonObjectMapper;
import org.apache.logging.log4j.core.parser.AbstractJacksonLogEventParser;

public class JsonLogEventParser
extends AbstractJacksonLogEventParser {
    public JsonLogEventParser() {
        super(new Log4jJsonObjectMapper());
    }
}

