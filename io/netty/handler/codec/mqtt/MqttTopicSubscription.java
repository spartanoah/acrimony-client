/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;
import io.netty.util.internal.StringUtil;

public final class MqttTopicSubscription {
    private final String topicFilter;
    private final MqttSubscriptionOption option;

    public MqttTopicSubscription(String topicFilter, MqttQoS qualityOfService) {
        this.topicFilter = topicFilter;
        this.option = MqttSubscriptionOption.onlyFromQos(qualityOfService);
    }

    public MqttTopicSubscription(String topicFilter, MqttSubscriptionOption option) {
        this.topicFilter = topicFilter;
        this.option = option;
    }

    public String topicName() {
        return this.topicFilter;
    }

    public MqttQoS qualityOfService() {
        return this.option.qos();
    }

    public MqttSubscriptionOption option() {
        return this.option;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "topicFilter=" + this.topicFilter + ", option=" + this.option + ']';
    }
}

