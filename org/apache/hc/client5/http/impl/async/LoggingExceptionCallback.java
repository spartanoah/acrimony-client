/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import org.apache.hc.core5.function.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggingExceptionCallback
implements Callback<Exception> {
    static LoggingExceptionCallback INSTANCE = new LoggingExceptionCallback();
    private static final Logger LOG = LoggerFactory.getLogger("org.apache.hc.client5.http.impl.async");

    private LoggingExceptionCallback() {
    }

    @Override
    public void execute(Exception ex) {
        LOG.error(ex.getMessage(), ex);
    }
}

