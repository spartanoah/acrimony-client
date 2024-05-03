/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public final class MqttPublishVariableHeader {
    private final String topicName;
    private final int packetId;
    private final MqttProperties properties;

    public MqttPublishVariableHeader(String topicName, int packetId) {
        this(topicName, packetId, MqttProperties.NO_PROPERTIES);
    }

    public MqttPublishVariableHeader(String topicName, int packetId, MqttProperties properties) {
        this.topicName = topicName;
        this.packetId = packetId;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public String topicName() {
        return this.topicName;
    }

    @Deprecated
    public int messageId() {
        return this.packetId;
    }

    public int packetId() {
        return this.packetId;
    }

    public MqttProperties properties() {
        return this.properties;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "topicName=" + this.topicName + ", packetId=" + this.packetId + ']';
    }
}

