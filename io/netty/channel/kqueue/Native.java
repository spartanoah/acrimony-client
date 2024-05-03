/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.channel.DefaultFileRegion;
import io.netty.channel.kqueue.KQueueEventArray;
import io.netty.channel.kqueue.KQueueStaticallyReferencedJniMethods;
import io.netty.channel.unix.Errors;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.unix.PeerCredentials;
import io.netty.channel.unix.Unix;
import io.netty.util.internal.ClassInitializerUtil;
import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.nio.channels.FileChannel;

final class Native {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Native.class);
    static final short EV_ADD;
    static final short EV_ENABLE;
    static final short EV_DISABLE;
    static final short EV_DELETE;
    static final short EV_CLEAR;
    static final short EV_ERROR;
    static final short EV_EOF;
    static final int NOTE_READCLOSED;
    static final int NOTE_CONNRESET;
    static final int NOTE_DISCONNECTED;
    static final int NOTE_RDHUP;
    static final short EV_ADD_CLEAR_ENABLE;
    static final short EV_DELETE_DISABLE;
    static final short EVFILT_READ;
    static final short EVFILT_WRITE;
    static final short EVFILT_USER;
    static final short EVFILT_SOCK;
    private static final int CONNECT_RESUME_ON_READ_WRITE;
    private static final int CONNECT_DATA_IDEMPOTENT;
    static final int CONNECT_TCP_FASTOPEN;

    private static native int registerUnix();

    static FileDescriptor newKQueue() {
        return new FileDescriptor(Native.kqueueCreate());
    }

    static int keventWait(int kqueueFd, KQueueEventArray changeList, KQueueEventArray eventList, int tvSec, int tvNsec) throws IOException {
        int ready = Native.keventWait(kqueueFd, changeList.memoryAddress(), changeList.size(), eventList.memoryAddress(), eventList.capacity(), tvSec, tvNsec);
        if (ready < 0) {
            throw Errors.newIOException("kevent", ready);
        }
        return ready;
    }

    private static native int kqueueCreate();

    private static native int keventWait(int var0, long var1, int var3, long var4, int var6, int var7, int var8);

    static native int keventTriggerUserEvent(int var0, int var1);

    static native int keventAddUserEvent(int var0, int var1);

    static native int sizeofKEvent();

    static native int offsetofKEventIdent();

    static native int offsetofKEventFlags();

    static native int offsetofKEventFFlags();

    static native int offsetofKEventFilter();

    static native int offsetofKeventData();

    private static void loadNativeLibrary() {
        String name = PlatformDependent.normalizedOs();
        if (!"osx".equals(name) && !name.contains("bsd")) {
            throw new IllegalStateException("Only supported on OSX/BSD");
        }
        String staticLibName = "netty_transport_native_kqueue";
        String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
        ClassLoader cl = PlatformDependent.getClassLoader(Native.class);
        try {
            NativeLibraryLoader.load(sharedLibName, cl);
        } catch (UnsatisfiedLinkError e1) {
            try {
                NativeLibraryLoader.load(staticLibName, cl);
                logger.debug("Failed to load {}", (Object)sharedLibName, (Object)e1);
            } catch (UnsatisfiedLinkError e2) {
                ThrowableUtil.addSuppressed((Throwable)e1, e2);
                throw e1;
            }
        }
    }

    private Native() {
    }

    static {
        ClassInitializerUtil.tryLoadClasses(Native.class, PeerCredentials.class, DefaultFileRegion.class, FileChannel.class, java.io.FileDescriptor.class);
        try {
            Native.sizeofKEvent();
        } catch (UnsatisfiedLinkError ignore) {
            Native.loadNativeLibrary();
        }
        Unix.registerInternal(new Runnable(){

            @Override
            public void run() {
                Native.registerUnix();
            }
        });
        EV_ADD = KQueueStaticallyReferencedJniMethods.evAdd();
        EV_ENABLE = KQueueStaticallyReferencedJniMethods.evEnable();
        EV_DISABLE = KQueueStaticallyReferencedJniMethods.evDisable();
        EV_DELETE = KQueueStaticallyReferencedJniMethods.evDelete();
        EV_CLEAR = KQueueStaticallyReferencedJniMethods.evClear();
        EV_ERROR = KQueueStaticallyReferencedJniMethods.evError();
        EV_EOF = KQueueStaticallyReferencedJniMethods.evEOF();
        NOTE_READCLOSED = KQueueStaticallyReferencedJniMethods.noteReadClosed();
        NOTE_CONNRESET = KQueueStaticallyReferencedJniMethods.noteConnReset();
        NOTE_DISCONNECTED = KQueueStaticallyReferencedJniMethods.noteDisconnected();
        NOTE_RDHUP = NOTE_READCLOSED | NOTE_CONNRESET | NOTE_DISCONNECTED;
        EV_ADD_CLEAR_ENABLE = (short)(EV_ADD | EV_CLEAR | EV_ENABLE);
        EV_DELETE_DISABLE = (short)(EV_DELETE | EV_DISABLE);
        EVFILT_READ = KQueueStaticallyReferencedJniMethods.evfiltRead();
        EVFILT_WRITE = KQueueStaticallyReferencedJniMethods.evfiltWrite();
        EVFILT_USER = KQueueStaticallyReferencedJniMethods.evfiltUser();
        EVFILT_SOCK = KQueueStaticallyReferencedJniMethods.evfiltSock();
        CONNECT_RESUME_ON_READ_WRITE = KQueueStaticallyReferencedJniMethods.connectResumeOnReadWrite();
        CONNECT_DATA_IDEMPOTENT = KQueueStaticallyReferencedJniMethods.connectDataIdempotent();
        CONNECT_TCP_FASTOPEN = CONNECT_RESUME_ON_READ_WRITE | CONNECT_DATA_IDEMPOTENT;
    }
}

