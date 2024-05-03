/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.dataformat.yaml.YAMLMapper
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.logging.log4j.core.jackson.Log4jYamlModule;

public class Log4jYamlObjectMapper
extends YAMLMapper {
    private static final long serialVersionUID = 1L;

    public Log4jYamlObjectMapper() {
        this(false, true, false);
    }

    public Log4jYamlObjectMapper(boolean encodeThreadContextAsList, boolean includeStacktrace, boolean stacktraceAsString) {
        this.registerModule(new Log4jYamlModule(encodeThreadContextAsList, includeStacktrace, stacktraceAsString));
        this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
}

