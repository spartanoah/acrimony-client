/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

public enum MqttQoS {
    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2),
    FAILURE(128);

    private final int value;

    private MqttQoS(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static MqttQoS valueOf(int value) {
        switch (value) {
            case 0: {
                return AT_MOST_ONCE;
            }
            case 1: {
                return AT_LEAST_ONCE;
            }
            case 2: {
                return EXACTLY_ONCE;
            }
            case 128: {
                return FAILURE;
            }
        }
        throw new IllegalArgumentException("invalid QoS: " + value);
    }
}

