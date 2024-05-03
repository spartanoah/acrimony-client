/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.parser;

import org.apache.logging.log4j.core.jackson.Log4jXmlObjectMapper;
import org.apache.logging.log4j.core.parser.AbstractJacksonLogEventParser;

public class XmlLogEventParser
extends AbstractJacksonLogEventParser {
    public XmlLogEventParser() {
        super(new Log4jXmlObjectMapper());
    }
}

