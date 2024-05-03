/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.EventFactory
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventFactory;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.MementoMessage;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.StringMap;

public class RingBufferLogEvent
implements LogEvent,
ReusableMessage,
CharSequence,
ParameterVisitable {
    public static final Factory FACTORY = new Factory();
    private static final long serialVersionUID = 8462119088943934758L;
    private static final Message EMPTY = new SimpleMessage("");
    private boolean populated;
    private int threadPriority;
    private long threadId;
    private final MutableInstant instant = new MutableInstant();
    private long nanoTime;
    private short parameterCount;
    private boolean includeLocation;
    private boolean endOfBatch = false;
    private Level level;
    private String threadName;
    private String loggerName;
    private Message message;
    private String messageFormat;
    private StringBuilder messageText;
    private Object[] parameters;
    private transient Throwable thrown;
    private ThrowableProxy thrownProxy;
    private StringMap contextData = ContextDataFactory.createContextData();
    private Marker marker;
    private String fqcn;
    private StackTraceElement location;
    private ThreadContext.ContextStack contextStack;
    private transient AsyncLogger asyncLogger;

    public void setValues(AsyncLogger anAsyncLogger, String aLoggerName, Marker aMarker, String theFqcn, Level aLevel, Message msg, Throwable aThrowable, StringMap mutableContextData, ThreadContext.ContextStack aContextStack, long threadId, String threadName, int threadPriority, StackTraceElement aLocation, Clock clock, NanoClock nanoClock) {
        this.threadPriority = threadPriority;
        this.threadId = threadId;
        this.level = aLevel;
        this.threadName = threadName;
        this.loggerName = aLoggerName;
        this.setMessage(msg);
        this.initTime(clock);
        this.nanoTime = nanoClock.nanoTime();
        this.thrown = aThrowable;
        this.thrownProxy = null;
        this.marker = aMarker;
        this.fqcn = theFqcn;
        this.location = aLocation;
        this.contextData = mutableContextData;
        this.contextStack = aContextStack;
        this.asyncLogger = anAsyncLogger;
        this.populated = true;
    }

    private void initTime(Clock clock) {
        if (this.message instanceof TimestampMessage) {
            this.instant.initFromEpochMilli(((TimestampMessage)((Object)this.message)).getTimestamp(), 0);
        } else {
            this.instant.initFrom(clock);
        }
    }

    @Override
    public LogEvent toImmutable() {
        return this.createMemento();
    }

    private void setMessage(Message msg) {
        if (msg instanceof ReusableMessage) {
            ReusableMessage reusable = (ReusableMessage)msg;
            reusable.formatTo(this.getMessageTextForWriting());
            this.messageFormat = reusable.getFormat();
            this.parameters = reusable.swapParameters(this.parameters == null ? new Object[10] : this.parameters);
            this.parameterCount = reusable.getParameterCount();
        } else {
            this.message = InternalAsyncUtil.makeMessageImmutable(msg);
        }
    }

    private StringBuilder getMessageTextForWriting() {
        if (this.messageText == null) {
            this.messageText = new StringBuilder(Constants.INITIAL_REUSABLE_MESSAGE_SIZE);
        }
        this.messageText.setLength(0);
        return this.messageText;
    }

    public void execute(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
        this.asyncLogger.actualAsyncLog(this);
    }

    public boolean isPopulated() {
        return this.populated;
    }

    @Override
    public boolean isEndOfBatch() {
        return this.endOfBatch;
    }

    @Override
    public void setEndOfBatch(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
    }

    @Override
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    @Override
    public void setIncludeLocation(boolean includeLocation) {
        this.includeLocation = includeLocation;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public Marker getMarker() {
        return this.marker;
    }

    @Override
    public String getLoggerFqcn() {
        return this.fqcn;
    }

    @Override
    public Level getLevel() {
        if (this.level == null) {
            this.level = Level.OFF;
        }
        return this.level;
    }

    @Override
    public Message getMessage() {
        if (this.message == null) {
            return this.messageText == null ? EMPTY : this;
        }
        return this.message;
    }

    @Override
    public String getFormattedMessage() {
        return this.messageText != null ? this.messageText.toString() : (this.message == null ? null : this.message.getFormattedMessage());
    }

    @Override
    public String getFormat() {
        return this.messageFormat;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters == null ? null : Arrays.copyOf(this.parameters, (int)this.parameterCount);
    }

    @Override
    public Throwable getThrowable() {
        return this.getThrown();
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        buffer.append((CharSequence)this.messageText);
    }

    @Override
    public Object[] swapParameters(Object[] emptyReplacement) {
        Object[] result = this.parameters;
        this.parameters = emptyReplacement;
        return result;
    }

    @Override
    public short getParameterCount() {
        return this.parameterCount;
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> action, S state) {
        if (this.parameters != null) {
            for (short i = 0; i < this.parameterCount; i = (short)(i + 1)) {
                action.accept(this.parameters[i], i, state);
            }
        }
    }

    @Override
    public Message memento() {
        if (this.message == null) {
            this.message = new MementoMessage(String.valueOf(this.messageText), this.messageFormat, this.getParameters());
        }
        return this.message;
    }

    @Override
    public int length() {
        return this.messageText.length();
    }

    @Override
    public char charAt(int index) {
        return this.messageText.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.messageText.subSequence(start, end);
    }

    @Override
    public Throwable getThrown() {
        if (this.thrown == null && this.thrownProxy != null) {
            this.thrown = this.thrownProxy.getThrowable();
        }
        return this.thrown;
    }

    @Override
    public ThrowableProxy getThrownProxy() {
        if (this.thrownProxy == null && this.thrown != null) {
            this.thrownProxy = new ThrowableProxy(this.thrown);
        }
        return this.thrownProxy;
    }

    @Override
    public ReadOnlyStringMap getContextData() {
        return this.contextData;
    }

    void setContextData(StringMap contextData) {
        this.contextData = contextData;
    }

    @Override
    public Map<String, String> getContextMap() {
        return this.contextData.toMap();
    }

    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }

    @Override
    public long getThreadId() {
        return this.threadId;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public int getThreadPriority() {
        return this.threadPriority;
    }

    @Override
    public StackTraceElement getSource() {
        return this.location;
    }

    @Override
    public long getTimeMillis() {
        return this.message instanceof TimestampMessage ? ((TimestampMessage)((Object)this.message)).getTimestamp() : this.instant.getEpochMillisecond();
    }

    @Override
    public Instant getInstant() {
        return this.instant;
    }

    @Override
    public long getNanoTime() {
        return this.nanoTime;
    }

    public void clear() {
        this.populated = false;
        this.asyncLogger = null;
        this.loggerName = null;
        this.marker = null;
        this.fqcn = null;
        this.level = null;
        this.message = null;
        this.messageFormat = null;
        this.thrown = null;
        this.thrownProxy = null;
        this.contextStack = null;
        this.location = null;
        if (this.contextData != null) {
            if (this.contextData.isFrozen()) {
                this.contextData = null;
            } else {
                this.contextData.clear();
            }
        }
        if (Constants.ENABLE_THREADLOCALS) {
            StringBuilders.trimToMaxSize(this.messageText, Constants.MAX_REUSABLE_MESSAGE_SIZE);
            if (this.parameters != null) {
                Arrays.fill(this.parameters, null);
            }
        } else {
            this.messageText = null;
            this.parameters = null;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.getThrownProxy();
        out.defaultWriteObject();
    }

    public LogEvent createMemento() {
        return new Log4jLogEvent.Builder(this).build();
    }

    public void initializeBuilder(Log4jLogEvent.Builder builder) {
        builder.setContextData(this.contextData).setContextStack(this.contextStack).setEndOfBatch(this.endOfBatch).setIncludeLocation(this.includeLocation).setLevel(this.getLevel()).setLoggerFqcn(this.fqcn).setLoggerName(this.loggerName).setMarker(this.marker).setMessage(this.memento()).setNanoTime(this.nanoTime).setSource(this.location).setThreadId(this.threadId).setThreadName(this.threadName).setThreadPriority(this.threadPriority).setThrown(this.getThrown()).setThrownProxy(this.thrownProxy).setInstant(this.instant);
    }

    private static class Factory
    implements EventFactory<RingBufferLogEvent> {
        private Factory() {
        }

        public RingBufferLogEvent newInstance() {
            return new RingBufferLogEvent();
        }
    }
}

