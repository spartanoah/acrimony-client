/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.conversantmedia.util.concurrent.DisruptorBlockingQueue
 *  com.conversantmedia.util.concurrent.SpinPolicy
 */
package org.apache.logging.log4j.core.async;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.conversantmedia.util.concurrent.SpinPolicy;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.core.async.BlockingQueueFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="DisruptorBlockingQueue", category="Core", elementType="BlockingQueueFactory")
public class DisruptorBlockingQueueFactory<E>
implements BlockingQueueFactory<E> {
    private final SpinPolicy spinPolicy;

    private DisruptorBlockingQueueFactory(SpinPolicy spinPolicy) {
        this.spinPolicy = spinPolicy;
    }

    @Override
    public BlockingQueue<E> create(int capacity) {
        return new DisruptorBlockingQueue(capacity, this.spinPolicy);
    }

    @PluginFactory
    public static <E> DisruptorBlockingQueueFactory<E> createFactory(@PluginAttribute(value="SpinPolicy", defaultString="WAITING") SpinPolicy spinPolicy) {
        return new DisruptorBlockingQueueFactory<E>(spinPolicy);
    }
}

