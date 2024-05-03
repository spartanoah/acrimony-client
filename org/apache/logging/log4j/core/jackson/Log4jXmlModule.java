/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import org.apache.logging.log4j.core.jackson.Initializers;

final class Log4jXmlModule
extends JacksonXmlModule {
    private static final long serialVersionUID = 1L;
    private final boolean includeStacktrace;
    private final boolean stacktraceAsString;

    Log4jXmlModule(boolean includeStacktrace, boolean stacktraceAsString) {
        this.includeStacktrace = includeStacktrace;
        this.stacktraceAsString = stacktraceAsString;
        new Initializers.SimpleModuleInitializer().initialize(this, false);
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        new Initializers.SetupContextInitializer().setupModule(context, this.includeStacktrace, this.stacktraceAsString);
    }
}

