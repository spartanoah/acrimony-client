/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive
public class AppenderControlArraySet {
    private static final AtomicReferenceFieldUpdater<AppenderControlArraySet, AppenderControl[]> appenderArrayUpdater = AtomicReferenceFieldUpdater.newUpdater(AppenderControlArraySet.class, AppenderControl[].class, "appenderArray");
    private volatile AppenderControl[] appenderArray = AppenderControl.EMPTY_ARRAY;

    public boolean add(AppenderControl control) {
        AppenderControl[] copy;
        AppenderControl[] original;
        boolean success;
        do {
            for (AppenderControl existing : original = this.appenderArray) {
                if (!existing.equals(control)) continue;
                return false;
            }
            copy = Arrays.copyOf(original, original.length + 1);
            copy[copy.length - 1] = control;
        } while (!(success = appenderArrayUpdater.compareAndSet(this, original, copy)));
        return true;
    }

    public AppenderControl remove(String name) {
        boolean success;
        block0: do {
            success = true;
            AppenderControl[] original = this.appenderArray;
            for (int i = 0; i < original.length; ++i) {
                AppenderControl appenderControl = original[i];
                if (!Objects.equals(name, appenderControl.getAppenderName())) continue;
                AppenderControl[] copy = this.removeElementAt(i, original);
                if (appenderArrayUpdater.compareAndSet(this, original, copy)) {
                    return appenderControl;
                }
                success = false;
                continue block0;
            }
        } while (!success);
        return null;
    }

    private AppenderControl[] removeElementAt(int i, AppenderControl[] array) {
        AppenderControl[] result = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, i + 1, result, i, result.length - i);
        return result;
    }

    public Map<String, Appender> asMap() {
        HashMap<String, Appender> result = new HashMap<String, Appender>();
        for (AppenderControl appenderControl : this.appenderArray) {
            result.put(appenderControl.getAppenderName(), appenderControl.getAppender());
        }
        return result;
    }

    public AppenderControl[] clear() {
        return appenderArrayUpdater.getAndSet(this, AppenderControl.EMPTY_ARRAY);
    }

    public boolean isEmpty() {
        return this.appenderArray.length == 0;
    }

    public AppenderControl[] get() {
        return this.appenderArray;
    }

    public String toString() {
        return "AppenderControlArraySet [appenderArray=" + Arrays.toString(this.appenderArray) + "]";
    }
}

