/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttUnsubAckPayload;

public final class MqttUnsubAckMessage
extends MqttMessage {
    public MqttUnsubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdAndPropertiesVariableHeader variableHeader, MqttUnsubAckPayload payload) {
        super(mqttFixedHeader, variableHeader, MqttUnsubAckPayload.withEmptyDefaults(payload));
    }

    public MqttUnsubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttUnsubAckPayload payload) {
        this(mqttFixedHeader, MqttUnsubAckMessage.fallbackVariableHeader(variableHeader), payload);
    }

    public MqttUnsubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader) {
        this(mqttFixedHeader, variableHeader, null);
    }

    private static MqttMessageIdAndPropertiesVariableHeader fallbackVariableHeader(MqttMessageIdVariableHeader variableHeader) {
        if (variableHeader instanceof MqttMessageIdAndPropertiesVariableHeader) {
            return (MqttMessageIdAndPropertiesVariableHeader)variableHeader;
        }
        return new MqttMessageIdAndPropertiesVariableHeader(variableHeader.messageId(), MqttProperties.NO_PROPERTIES);
    }

    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }

    public MqttMessageIdAndPropertiesVariableHeader idAndPropertiesVariableHeader() {
        return (MqttMessageIdAndPropertiesVariableHeader)super.variableHeader();
    }

    @Override
    public MqttUnsubAckPayload payload() {
        return (MqttUnsubAckPayload)super.payload();
    }
}

