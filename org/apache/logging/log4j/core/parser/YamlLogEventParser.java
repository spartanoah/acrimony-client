/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.jackson.Log4jYamlObjectMapper;
import org.apache.logging.log4j.core.parser.AbstractJacksonLogEventParser;

public class YamlLogEventParser
extends AbstractJacksonLogEventParser {
    public YamlLogEventParser() {
        super((ObjectMapper)((Object)new Log4jYamlObjectMapper()));
    }
}

