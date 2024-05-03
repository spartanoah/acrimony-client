/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.Message;

public class InternalAsyncUtil {
    public static Message makeMessageImmutable(Message msg) {
        if (msg != null && !InternalAsyncUtil.canFormatMessageInBackground(msg)) {
            msg.getFormattedMessage();
        }
        return msg;
    }

    private static boolean canFormatMessageInBackground(Message message) {
        return Constants.FORMAT_MESSAGES_IN_BACKGROUND || message.getClass().isAnnotationPresent(AsynchronouslyFormattable.class);
    }
}

