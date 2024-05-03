/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.Clearable;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public class ReusableSimpleMessage
implements ReusableMessage,
CharSequence,
ParameterVisitable,
Clearable {
    private static final long serialVersionUID = -9199974506498249809L;
    private CharSequence charSequence;

    public void set(String message) {
        this.charSequence = message;
    }

    public void set(CharSequence charSequence) {
        this.charSequence = charSequence;
    }

    @Override
    public String getFormattedMessage() {
        return String.valueOf(this.charSequence);
    }

    @Override
    public String getFormat() {
        return this.charSequence instanceof String ? (String)this.charSequence : null;
    }

    @Override
    public Object[] getParameters() {
        return Constants.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        buffer.append(this.charSequence);
    }

    @Override
    public Object[] swapParameters(Object[] emptyReplacement) {
        return emptyReplacement;
    }

    @Override
    public short getParameterCount() {
        return 0;
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> action, S state) {
    }

    @Override
    public Message memento() {
        return new SimpleMessage(this.charSequence);
    }

    @Override
    public int length() {
        return this.charSequence == null ? 0 : this.charSequence.length();
    }

    @Override
    public char charAt(int index) {
        return this.charSequence.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.charSequence.subSequence(start, end);
    }

    @Override
    public void clear() {
        this.charSequence = null;
    }
}

