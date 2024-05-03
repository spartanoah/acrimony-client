/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.List;

public final class MqttUnsubscribePayload {
    private final List<String> topics;

    public MqttUnsubscribePayload(List<String> topics) {
        this.topics = Collections.unmodifiableList(topics);
    }

    public List<String> topics() {
        return this.topics;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(StringUtil.simpleClassName(this)).append('[');
        for (int i = 0; i < this.topics.size(); ++i) {
            builder.append("topicName = ").append(this.topics.get(i)).append(", ");
        }
        if (!this.topics.isEmpty()) {
            builder.setLength(builder.length() - 2);
        }
        return builder.append("]").toString();
    }
}

