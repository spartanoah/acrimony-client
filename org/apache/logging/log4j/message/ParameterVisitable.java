/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public interface ParameterVisitable {
    public <S> void forEachParameter(ParameterConsumer<S> var1, S var2);
}

