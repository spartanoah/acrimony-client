/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

final class KQueueStaticallyReferencedJniMethods {
    private KQueueStaticallyReferencedJniMethods() {
    }

    static native short evAdd();

    static native short evEnable();

    static native short evDisable();

    static native short evDelete();

    static native short evClear();

    static native short evEOF();

    static native short evError();

    static native short noteReadClosed();

    static native short noteConnReset();

    static native short noteDisconnected();

    static native short evfiltRead();

    static native short evfiltWrite();

    static native short evfiltUser();

    static native short evfiltSock();

    static native int connectResumeOnReadWrite();

    static native int connectDataIdempotent();
}

