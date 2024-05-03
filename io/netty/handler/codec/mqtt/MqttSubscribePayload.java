/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.List;

public final class MqttSubscribePayload {
    private final List<MqttTopicSubscription> topicSubscriptions;

    public MqttSubscribePayload(List<MqttTopicSubscription> topicSubscriptions) {
        this.topicSubscriptions = Collections.unmodifiableList(topicSubscriptions);
    }

    public List<MqttTopicSubscription> topicSubscriptions() {
        return this.topicSubscriptions;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(StringUtil.simpleClassName(this)).append('[');
        for (int i = 0; i < this.topicSubscriptions.size(); ++i) {
            builder.append(this.topicSubscriptions.get(i)).append(", ");
        }
        if (!this.topicSubscriptions.isEmpty()) {
            builder.setLength(builder.length() - 2);
        }
        return builder.append(']').toString();
    }
}

