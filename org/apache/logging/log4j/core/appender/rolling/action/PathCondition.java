/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public interface PathCondition {
    public static final PathCondition[] EMPTY_ARRAY = new PathCondition[0];

    public static PathCondition[] copy(PathCondition ... source) {
        return source == null || source.length == 0 ? EMPTY_ARRAY : Arrays.copyOf(source, source.length);
    }

    public void beforeFileTreeWalk();

    public boolean accept(Path var1, Path var2, BasicFileAttributes var3);
}

