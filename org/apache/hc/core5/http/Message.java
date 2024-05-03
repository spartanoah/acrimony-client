/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.util.Args;

public final class Message<H extends MessageHeaders, B> {
    private final H head;
    private final B body;

    public Message(H head, B body) {
        this.head = (MessageHeaders)Args.notNull(head, "Message head");
        this.body = body;
    }

    public H getHead() {
        return this.head;
    }

    public B getBody() {
        return this.body;
    }

    public String toString() {
        return "[head=" + this.head + ", body=" + this.body + ']';
    }
}

