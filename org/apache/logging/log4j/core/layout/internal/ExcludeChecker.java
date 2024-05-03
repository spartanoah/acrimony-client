/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout.internal;

import java.util.List;
import org.apache.logging.log4j.core.layout.internal.ListChecker;

public class ExcludeChecker
implements ListChecker {
    private final List<String> list;

    public ExcludeChecker(List<String> list) {
        this.list = list;
    }

    @Override
    public boolean check(String key) {
        return !this.list.contains(key);
    }

    public String toString() {
        return "ThreadContextExcludes=" + this.list.toString();
    }
}

