/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.Message;

public interface MessageCollectionMessage<T>
extends Message,
Iterable<T> {
}

