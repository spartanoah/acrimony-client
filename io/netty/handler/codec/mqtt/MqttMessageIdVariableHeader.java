/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public class MqttMessageIdVariableHeader {
    private final int messageId;

    public static MqttMessageIdVariableHeader from(int messageId) {
        if (messageId < 1 || messageId > 65535) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
        }
        return new MqttMessageIdVariableHeader(messageId);
    }

    protected MqttMessageIdVariableHeader(int messageId) {
        this.messageId = messageId;
    }

    public int messageId() {
        return this.messageId;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "messageId=" + this.messageId + ']';
    }

    public MqttMessageIdAndPropertiesVariableHeader withEmptyProperties() {
        return new MqttMessageIdAndPropertiesVariableHeader(this.messageId, MqttProperties.NO_PROPERTIES);
    }

    MqttMessageIdAndPropertiesVariableHeader withDefaultEmptyProperties() {
        return this.withEmptyProperties();
    }
}

