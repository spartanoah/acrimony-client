/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.LoggingException;

public class AppenderLoggingException
extends LoggingException {
    private static final long serialVersionUID = 6545990597472958303L;

    public AppenderLoggingException(String message) {
        super(message);
    }

    public AppenderLoggingException(String format, Object ... args) {
        super(String.format(format, args));
    }

    public AppenderLoggingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppenderLoggingException(Throwable cause) {
        super(cause);
    }

    public AppenderLoggingException(Throwable cause, String format, Object ... args) {
        super(String.format(format, args), cause);
    }
}

