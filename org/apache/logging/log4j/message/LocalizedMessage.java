/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.LoggerNameAwareMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

public class LocalizedMessage
implements Message,
LoggerNameAwareMessage {
    private static final long serialVersionUID = 3893703791567290742L;
    private String baseName;
    private transient ResourceBundle resourceBundle;
    private final Locale locale;
    private transient StatusLogger logger = StatusLogger.getLogger();
    private String loggerName;
    private String key;
    private String[] stringArgs;
    private transient Object[] argArray;
    private String formattedMessage;
    private transient Throwable throwable;

    public LocalizedMessage(String messagePattern, Object[] arguments) {
        this((ResourceBundle)null, (Locale)null, messagePattern, arguments);
    }

    public LocalizedMessage(String baseName, String key, Object[] arguments) {
        this(baseName, (Locale)null, key, arguments);
    }

    public LocalizedMessage(ResourceBundle bundle, String key, Object[] arguments) {
        this(bundle, (Locale)null, key, arguments);
    }

    public LocalizedMessage(String baseName, Locale locale, String key, Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = baseName;
        this.resourceBundle = null;
        this.locale = locale;
    }

    public LocalizedMessage(ResourceBundle bundle, Locale locale, String key, Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = null;
        this.resourceBundle = bundle;
        this.locale = locale;
    }

    public LocalizedMessage(Locale locale, String key, Object[] arguments) {
        this((ResourceBundle)null, locale, key, arguments);
    }

    public LocalizedMessage(String messagePattern, Object arg) {
        this((ResourceBundle)null, (Locale)null, messagePattern, new Object[]{arg});
    }

    public LocalizedMessage(String baseName, String key, Object arg) {
        this(baseName, (Locale)null, key, new Object[]{arg});
    }

    public LocalizedMessage(ResourceBundle bundle, String key) {
        this(bundle, (Locale)null, key, new Object[0]);
    }

    public LocalizedMessage(ResourceBundle bundle, String key, Object arg) {
        this(bundle, (Locale)null, key, new Object[]{arg});
    }

    public LocalizedMessage(String baseName, Locale locale, String key, Object arg) {
        this(baseName, locale, key, new Object[]{arg});
    }

    public LocalizedMessage(ResourceBundle bundle, Locale locale, String key, Object arg) {
        this(bundle, locale, key, new Object[]{arg});
    }

    public LocalizedMessage(Locale locale, String key, Object arg) {
        this((ResourceBundle)null, locale, key, new Object[]{arg});
    }

    public LocalizedMessage(String messagePattern, Object arg1, Object arg2) {
        this((ResourceBundle)null, (Locale)null, messagePattern, new Object[]{arg1, arg2});
    }

    public LocalizedMessage(String baseName, String key, Object arg1, Object arg2) {
        this(baseName, (Locale)null, key, new Object[]{arg1, arg2});
    }

    public LocalizedMessage(ResourceBundle bundle, String key, Object arg1, Object arg2) {
        this(bundle, (Locale)null, key, new Object[]{arg1, arg2});
    }

    public LocalizedMessage(String baseName, Locale locale, String key, Object arg1, Object arg2) {
        this(baseName, locale, key, new Object[]{arg1, arg2});
    }

    public LocalizedMessage(ResourceBundle bundle, Locale locale, String key, Object arg1, Object arg2) {
        this(bundle, locale, key, new Object[]{arg1, arg2});
    }

    public LocalizedMessage(Locale locale, String key, Object arg1, Object arg2) {
        this((ResourceBundle)null, locale, key, new Object[]{arg1, arg2});
    }

    @Override
    public void setLoggerName(String name) {
        this.loggerName = name;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage != null) {
            return this.formattedMessage;
        }
        ResourceBundle bundle = this.resourceBundle;
        if (bundle == null) {
            bundle = this.baseName != null ? this.getResourceBundle(this.baseName, this.locale, false) : this.getResourceBundle(this.loggerName, this.locale, true);
        }
        String myKey = this.getFormat();
        String msgPattern = bundle == null || !bundle.containsKey(myKey) ? myKey : bundle.getString(myKey);
        Object[] array = this.argArray == null ? this.stringArgs : this.argArray;
        FormattedMessage msg = new FormattedMessage(msgPattern, array);
        this.formattedMessage = msg.getFormattedMessage();
        this.throwable = msg.getThrowable();
        return this.formattedMessage;
    }

    @Override
    public String getFormat() {
        return this.key;
    }

    @Override
    public Object[] getParameters() {
        if (this.argArray != null) {
            return this.argArray;
        }
        return this.stringArgs;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    protected ResourceBundle getResourceBundle(String rbBaseName, Locale resourceBundleLocale, boolean loop) {
        int i;
        ResourceBundle rb;
        block7: {
            rb = null;
            if (rbBaseName == null) {
                return null;
            }
            try {
                rb = resourceBundleLocale != null ? ResourceBundle.getBundle(rbBaseName, resourceBundleLocale) : ResourceBundle.getBundle(rbBaseName);
            } catch (MissingResourceException ex) {
                if (loop) break block7;
                this.logger.debug("Unable to locate ResourceBundle " + rbBaseName);
                return null;
            }
        }
        String substr = rbBaseName;
        while (rb == null && (i = substr.lastIndexOf(46)) > 0) {
            substr = substr.substring(0, i);
            try {
                if (resourceBundleLocale != null) {
                    rb = ResourceBundle.getBundle(substr, resourceBundleLocale);
                    continue;
                }
                rb = ResourceBundle.getBundle(substr);
            } catch (MissingResourceException ex) {
                this.logger.debug("Unable to locate ResourceBundle " + substr);
            }
        }
        return rb;
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        this.getFormattedMessage();
        out.writeUTF(this.formattedMessage);
        out.writeUTF(this.key);
        out.writeUTF(this.baseName);
        out.writeInt(this.argArray.length);
        this.stringArgs = new String[this.argArray.length];
        int i = 0;
        for (Object obj : this.argArray) {
            this.stringArgs[i] = obj.toString();
            ++i;
        }
        out.writeObject(this.stringArgs);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.formattedMessage = in.readUTF();
        this.key = in.readUTF();
        this.baseName = in.readUTF();
        in.readInt();
        this.stringArgs = (String[])in.readObject();
        this.logger = StatusLogger.getLogger();
        this.resourceBundle = null;
        this.argArray = null;
    }
}

