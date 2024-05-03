/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;

public final class MqttUnsubscribeMessage
extends MqttMessage {
    public MqttUnsubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdAndPropertiesVariableHeader variableHeader, MqttUnsubscribePayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    public MqttUnsubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttUnsubscribePayload payload) {
        this(mqttFixedHeader, variableHeader.withDefaultEmptyProperties(), payload);
    }

    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }

    public MqttMessageIdAndPropertiesVariableHeader idAndPropertiesVariableHeader() {
        return (MqttMessageIdAndPropertiesVariableHeader)super.variableHeader();
    }

    @Override
    public MqttUnsubscribePayload payload() {
        return (MqttUnsubscribePayload)super.payload();
    }
}

