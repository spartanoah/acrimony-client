/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.lang.ProcessHandle
 */
package org.apache.logging.log4j.util;

public class ProcessIdUtil {
    public static final String DEFAULT_PROCESSID = "-";

    public static String getProcessId() {
        try {
            return Long.toString(ProcessHandle.current().pid());
        } catch (Exception ex) {
            return DEFAULT_PROCESSID;
        }
    }
}

