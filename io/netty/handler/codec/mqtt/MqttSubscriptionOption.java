/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;

public final class MqttSubscriptionOption {
    private final MqttQoS qos;
    private final boolean noLocal;
    private final boolean retainAsPublished;
    private final RetainedHandlingPolicy retainHandling;

    public static MqttSubscriptionOption onlyFromQos(MqttQoS qos) {
        return new MqttSubscriptionOption(qos, false, false, RetainedHandlingPolicy.SEND_AT_SUBSCRIBE);
    }

    public MqttSubscriptionOption(MqttQoS qos, boolean noLocal, boolean retainAsPublished, RetainedHandlingPolicy retainHandling) {
        this.qos = qos;
        this.noLocal = noLocal;
        this.retainAsPublished = retainAsPublished;
        this.retainHandling = retainHandling;
    }

    public MqttQoS qos() {
        return this.qos;
    }

    public boolean isNoLocal() {
        return this.noLocal;
    }

    public boolean isRetainAsPublished() {
        return this.retainAsPublished;
    }

    public RetainedHandlingPolicy retainHandling() {
        return this.retainHandling;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MqttSubscriptionOption that = (MqttSubscriptionOption)o;
        if (this.noLocal != that.noLocal) {
            return false;
        }
        if (this.retainAsPublished != that.retainAsPublished) {
            return false;
        }
        if (this.qos != that.qos) {
            return false;
        }
        return this.retainHandling == that.retainHandling;
    }

    public int hashCode() {
        int result = this.qos.hashCode();
        result = 31 * result + (this.noLocal ? 1 : 0);
        result = 31 * result + (this.retainAsPublished ? 1 : 0);
        result = 31 * result + this.retainHandling.hashCode();
        return result;
    }

    public String toString() {
        return "SubscriptionOption[qos=" + (Object)((Object)this.qos) + ", noLocal=" + this.noLocal + ", retainAsPublished=" + this.retainAsPublished + ", retainHandling=" + (Object)((Object)this.retainHandling) + ']';
    }

    public static enum RetainedHandlingPolicy {
        SEND_AT_SUBSCRIBE(0),
        SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS(1),
        DONT_SEND_AT_SUBSCRIBE(2);

        private final int value;

        private RetainedHandlingPolicy(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static RetainedHandlingPolicy valueOf(int value) {
            switch (value) {
                case 0: {
                    return SEND_AT_SUBSCRIBE;
                }
                case 1: {
                    return SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS;
                }
                case 2: {
                    return DONT_SEND_AT_SUBSCRIBE;
                }
            }
            throw new IllegalArgumentException("invalid RetainedHandlingPolicy: " + value);
        }
    }
}

