/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ObjectCleaner {
    private static final int REFERENCE_QUEUE_POLL_TIMEOUT_MS = Math.max(500, SystemPropertyUtil.getInt("io.netty.util.internal.ObjectCleaner.refQueuePollTimeout", 10000));
    static final String CLEANER_THREAD_NAME = ObjectCleaner.class.getSimpleName() + "Thread";
    private static final Set<AutomaticCleanerReference> LIVE_SET = new ConcurrentSet<AutomaticCleanerReference>();
    private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue();
    private static final AtomicBoolean CLEANER_RUNNING = new AtomicBoolean(false);
    private static final Runnable CLEANER_TASK = new Runnable(){

        @Override
        public void run() {
            boolean interrupted = false;
            while (true) {
                if (!LIVE_SET.isEmpty()) {
                    AutomaticCleanerReference reference;
                    try {
                        reference = (AutomaticCleanerReference)REFERENCE_QUEUE.remove(REFERENCE_QUEUE_POLL_TIMEOUT_MS);
                    } catch (InterruptedException ex) {
                        interrupted = true;
                        continue;
                    }
                    if (reference == null) continue;
                    try {
                        reference.cleanup();
                    } catch (Throwable throwable) {
                        // empty catch block
                    }
                    LIVE_SET.remove(reference);
                    continue;
                }
                CLEANER_RUNNING.set(false);
                if (LIVE_SET.isEmpty() || !CLEANER_RUNNING.compareAndSet(false, true)) break;
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    };

    public static void register(Object object, Runnable cleanupTask) {
        AutomaticCleanerReference reference = new AutomaticCleanerReference(object, ObjectUtil.checkNotNull(cleanupTask, "cleanupTask"));
        LIVE_SET.add(reference);
        if (CLEANER_RUNNING.compareAndSet(false, true)) {
            final FastThreadLocalThread cleanupThread = new FastThreadLocalThread(CLEANER_TASK);
            cleanupThread.setPriority(1);
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    cleanupThread.setContextClassLoader(null);
                    return null;
                }
            });
            cleanupThread.setName(CLEANER_THREAD_NAME);
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
    }

    public static int getLiveSetCount() {
        return LIVE_SET.size();
    }

    private ObjectCleaner() {
    }

    private static final class AutomaticCleanerReference
    extends WeakReference<Object> {
        private final Runnable cleanupTask;

        AutomaticCleanerReference(Object referent, Runnable cleanupTask) {
            super(referent, REFERENCE_QUEUE);
            this.cleanupTask = cleanupTask;
        }

        void cleanup() {
            this.cleanupTask.run();
        }

        @Override
        public Thread get() {
            return null;
        }

        @Override
        public void clear() {
            LIVE_SET.remove(this);
            super.clear();
        }
    }
}

