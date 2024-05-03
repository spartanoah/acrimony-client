/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer.search;

import io.netty.buffer.search.SearchProcessor;

public interface SearchProcessorFactory {
    public SearchProcessor newSearchProcessor();
}

