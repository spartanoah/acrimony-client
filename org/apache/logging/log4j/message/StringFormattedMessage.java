/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

public class StringFormattedMessage
implements Message {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final long serialVersionUID = -665975803997290697L;
    private static final int HASHVAL = 31;
    private String messagePattern;
    private transient Object[] argArray;
    private String[] stringArgs;
    private transient String formattedMessage;
    private transient Throwable throwable;
    private final Locale locale;

    public StringFormattedMessage(Locale locale, String messagePattern, Object ... arguments) {
        this.locale = locale;
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        if (arguments != null && arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            this.throwable = (Throwable)arguments[arguments.length - 1];
        }
    }

    public StringFormattedMessage(String messagePattern, Object ... arguments) {
        this(Locale.getDefault(Locale.Category.FORMAT), messagePattern, arguments);
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage == null) {
            this.formattedMessage = this.formatMessage(this.messagePattern, this.argArray);
        }
        return this.formattedMessage;
    }

    @Override
    public String getFormat() {
        return this.messagePattern;
    }

    @Override
    public Object[] getParameters() {
        if (this.argArray != null) {
            return this.argArray;
        }
        return this.stringArgs;
    }

    protected String formatMessage(String msgPattern, Object ... args) {
        if (args != null && args.length == 0) {
            return msgPattern;
        }
        try {
            return String.format(this.locale, msgPattern, args);
        } catch (IllegalFormatException ife) {
            LOGGER.error("Unable to format msg: " + msgPattern, (Throwable)ife);
            return msgPattern;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StringFormattedMessage that = (StringFormattedMessage)o;
        if (this.messagePattern != null ? !this.messagePattern.equals(that.messagePattern) : that.messagePattern != null) {
            return false;
        }
        return Arrays.equals(this.stringArgs, that.stringArgs);
    }

    public int hashCode() {
        int result = this.messagePattern != null ? this.messagePattern.hashCode() : 0;
        result = 31 * result + (this.stringArgs != null ? Arrays.hashCode(this.stringArgs) : 0);
        return result;
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        this.getFormattedMessage();
        out.writeUTF(this.formattedMessage);
        out.writeUTF(this.messagePattern);
        out.writeInt(this.argArray.length);
        this.stringArgs = new String[this.argArray.length];
        int i = 0;
        for (Object obj : this.argArray) {
            String string;
            this.stringArgs[i] = string = String.valueOf(obj);
            out.writeUTF(string);
            ++i;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.formattedMessage = in.readUTF();
        this.messagePattern = in.readUTF();
        int length = in.readInt();
        this.stringArgs = new String[length];
        for (int i = 0; i < length; ++i) {
            this.stringArgs[i] = in.readUTF();
        }
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }
}

