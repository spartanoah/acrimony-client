/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.core.jackson.Initializers;

final class Log4jYamlModule
extends SimpleModule {
    private static final long serialVersionUID = 1L;
    private final boolean encodeThreadContextAsList;
    private final boolean includeStacktrace;
    private final boolean stacktraceAsString;

    Log4jYamlModule(boolean encodeThreadContextAsList, boolean includeStacktrace, boolean stacktraceAsString) {
        super(Log4jYamlModule.class.getName(), new Version(2, 0, 0, null, null, null));
        this.encodeThreadContextAsList = encodeThreadContextAsList;
        this.includeStacktrace = includeStacktrace;
        this.stacktraceAsString = stacktraceAsString;
        new Initializers.SimpleModuleInitializer().initialize(this, false);
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        if (this.encodeThreadContextAsList) {
            new Initializers.SetupContextInitializer().setupModule(context, this.includeStacktrace, this.stacktraceAsString);
        } else {
            new Initializers.SetupContextJsonInitializer().setupModule(context, this.includeStacktrace, this.stacktraceAsString);
        }
    }
}

