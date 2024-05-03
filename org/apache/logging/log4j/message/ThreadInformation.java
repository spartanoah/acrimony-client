/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

public interface ThreadInformation {
    public void printThreadInfo(StringBuilder var1);

    public void printStack(StringBuilder var1, StackTraceElement[] var2);
}

