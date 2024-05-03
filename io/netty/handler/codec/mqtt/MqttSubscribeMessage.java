/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;

public final class MqttSubscribeMessage
extends MqttMessage {
    public MqttSubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdAndPropertiesVariableHeader variableHeader, MqttSubscribePayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    public MqttSubscribeMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttSubscribePayload payload) {
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
    public MqttSubscribePayload payload() {
        return (MqttSubscribePayload)super.payload();
    }
}

