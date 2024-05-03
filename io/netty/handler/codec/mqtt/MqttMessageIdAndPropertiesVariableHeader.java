/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public final class MqttMessageIdAndPropertiesVariableHeader
extends MqttMessageIdVariableHeader {
    private final MqttProperties properties;

    public MqttMessageIdAndPropertiesVariableHeader(int messageId, MqttProperties properties) {
        super(messageId);
        if (messageId < 1 || messageId > 65535) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
        }
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public MqttProperties properties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "[messageId=" + this.messageId() + ", properties=" + this.properties + ']';
    }

    @Override
    MqttMessageIdAndPropertiesVariableHeader withDefaultEmptyProperties() {
        return this;
    }
}

