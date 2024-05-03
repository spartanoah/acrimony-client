/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer.search;

import io.netty.buffer.search.MultiSearchProcessor;
import io.netty.buffer.search.SearchProcessorFactory;

public interface MultiSearchProcessorFactory
extends SearchProcessorFactory {
    @Override
    public MultiSearchProcessor newSearchProcessor();
}

