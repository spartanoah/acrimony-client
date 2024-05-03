/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;

public class MqttPublishMessage
extends MqttMessage
implements ByteBufHolder {
    public MqttPublishMessage(MqttFixedHeader mqttFixedHeader, MqttPublishVariableHeader variableHeader, ByteBuf payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    @Override
    public MqttPublishVariableHeader variableHeader() {
        return (MqttPublishVariableHeader)super.variableHeader();
    }

    @Override
    public ByteBuf payload() {
        return this.content();
    }

    @Override
    public ByteBuf content() {
        return ByteBufUtil.ensureAccessible((ByteBuf)((ByteBuf)super.payload()));
    }

    @Override
    public MqttPublishMessage copy() {
        return this.replace(this.content().copy());
    }

    @Override
    public MqttPublishMessage duplicate() {
        return this.replace(this.content().duplicate());
    }

    public MqttPublishMessage retainedDuplicate() {
        return this.replace(this.content().retainedDuplicate());
    }

    public MqttPublishMessage replace(ByteBuf content) {
        return new MqttPublishMessage(this.fixedHeader(), this.variableHeader(), content);
    }

    @Override
    public int refCnt() {
        return this.content().refCnt();
    }

    @Override
    public MqttPublishMessage retain() {
        this.content().retain();
        return this;
    }

    @Override
    public MqttPublishMessage retain(int increment) {
        this.content().retain(increment);
        return this;
    }

    public MqttPublishMessage touch() {
        this.content().touch();
        return this;
    }

    public MqttPublishMessage touch(Object hint) {
        this.content().touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return this.content().release();
    }

    @Override
    public boolean release(int decrement) {
        return this.content().release(decrement);
    }
}

