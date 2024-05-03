/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.logging;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

class Log4J2Logger
extends ExtendedLoggerWrapper
implements InternalLogger {
    private static final long serialVersionUID = 5485418394879791397L;
    private static final boolean VARARGS_ONLY = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

        @Override
        public Boolean run() {
            try {
                Logger.class.getMethod("debug", String.class, Object.class);
                return false;
            } catch (NoSuchMethodException ignore) {
                return true;
            } catch (SecurityException ignore) {
                return false;
            }
        }
    });

    Log4J2Logger(Logger logger) {
        super((ExtendedLogger)logger, logger.getName(), (MessageFactory)logger.getMessageFactory());
        if (VARARGS_ONLY) {
            throw new UnsupportedOperationException("Log4J2 version mismatch");
        }
    }

    @Override
    public String name() {
        return this.getName();
    }

    public void trace(Throwable t) {
        this.log(Level.TRACE, "Unexpected exception:", t);
    }

    public void debug(Throwable t) {
        this.log(Level.DEBUG, "Unexpected exception:", t);
    }

    public void info(Throwable t) {
        this.log(Level.INFO, "Unexpected exception:", t);
    }

    public void warn(Throwable t) {
        this.log(Level.WARN, "Unexpected exception:", t);
    }

    public void error(Throwable t) {
        this.log(Level.ERROR, "Unexpected exception:", t);
    }

    @Override
    public boolean isEnabled(InternalLogLevel level) {
        return this.isEnabled(Log4J2Logger.toLevel(level));
    }

    @Override
    public void log(InternalLogLevel level, String msg) {
        this.log(Log4J2Logger.toLevel(level), msg);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        this.log(Log4J2Logger.toLevel(level), format, arg);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        this.log(Log4J2Logger.toLevel(level), format, argA, argB);
    }

    @Override
    public void log(InternalLogLevel level, String format, Object ... arguments) {
        this.log(Log4J2Logger.toLevel(level), format, arguments);
    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable t) {
        this.log(Log4J2Logger.toLevel(level), msg, t);
    }

    public void log(InternalLogLevel level, Throwable t) {
        this.log(Log4J2Logger.toLevel(level), "Unexpected exception:", t);
    }

    private static Level toLevel(InternalLogLevel level) {
        switch (level) {
            case INFO: {
                return Level.INFO;
            }
            case DEBUG: {
                return Level.DEBUG;
            }
            case WARN: {
                return Level.WARN;
            }
            case ERROR: {
                return Level.ERROR;
            }
            case TRACE: {
                return Level.TRACE;
            }
        }
        throw new Error();
    }
}

