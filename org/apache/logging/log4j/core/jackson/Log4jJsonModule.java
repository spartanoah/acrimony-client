/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.core.jackson.Initializers;

class Log4jJsonModule
extends SimpleModule {
    private static final long serialVersionUID = 1L;
    private final boolean encodeThreadContextAsList;
    private final boolean includeStacktrace;
    private final boolean stacktraceAsString;
    private final boolean objectMessageAsJsonObject;

    Log4jJsonModule(boolean encodeThreadContextAsList, boolean includeStacktrace, boolean stacktraceAsString, boolean objectMessageAsJsonObject) {
        super(Log4jJsonModule.class.getName(), new Version(2, 0, 0, null, null, null));
        this.encodeThreadContextAsList = encodeThreadContextAsList;
        this.includeStacktrace = includeStacktrace;
        this.stacktraceAsString = stacktraceAsString;
        this.objectMessageAsJsonObject = objectMessageAsJsonObject;
        new Initializers.SimpleModuleInitializer().initialize(this, objectMessageAsJsonObject);
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

