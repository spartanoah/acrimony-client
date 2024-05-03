/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

public enum MqttConnectReturnCode {
    CONNECTION_ACCEPTED(0),
    CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION(1),
    CONNECTION_REFUSED_IDENTIFIER_REJECTED(2),
    CONNECTION_REFUSED_SERVER_UNAVAILABLE(3),
    CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD(4),
    CONNECTION_REFUSED_NOT_AUTHORIZED(5),
    CONNECTION_REFUSED_UNSPECIFIED_ERROR(-128),
    CONNECTION_REFUSED_MALFORMED_PACKET(-127),
    CONNECTION_REFUSED_PROTOCOL_ERROR(-126),
    CONNECTION_REFUSED_IMPLEMENTATION_SPECIFIC(-125),
    CONNECTION_REFUSED_UNSUPPORTED_PROTOCOL_VERSION(-124),
    CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID(-123),
    CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD(-122),
    CONNECTION_REFUSED_NOT_AUTHORIZED_5(-121),
    CONNECTION_REFUSED_SERVER_UNAVAILABLE_5(-120),
    CONNECTION_REFUSED_SERVER_BUSY(-119),
    CONNECTION_REFUSED_BANNED(-118),
    CONNECTION_REFUSED_BAD_AUTHENTICATION_METHOD(-116),
    CONNECTION_REFUSED_TOPIC_NAME_INVALID(-112),
    CONNECTION_REFUSED_PACKET_TOO_LARGE(-107),
    CONNECTION_REFUSED_QUOTA_EXCEEDED(-105),
    CONNECTION_REFUSED_PAYLOAD_FORMAT_INVALID(-103),
    CONNECTION_REFUSED_RETAIN_NOT_SUPPORTED(-102),
    CONNECTION_REFUSED_QOS_NOT_SUPPORTED(-101),
    CONNECTION_REFUSED_USE_ANOTHER_SERVER(-100),
    CONNECTION_REFUSED_SERVER_MOVED(-99),
    CONNECTION_REFUSED_CONNECTION_RATE_EXCEEDED(-97);

    private static final MqttConnectReturnCode[] VALUES;
    private final byte byteValue;

    private MqttConnectReturnCode(byte byteValue) {
        this.byteValue = byteValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public static MqttConnectReturnCode valueOf(byte b) {
        int unsignedByte = b & 0xFF;
        MqttConnectReturnCode mqttConnectReturnCode = null;
        try {
            mqttConnectReturnCode = VALUES[unsignedByte];
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            // empty catch block
        }
        if (mqttConnectReturnCode == null) {
            throw new IllegalArgumentException("unknown connect return code: " + unsignedByte);
        }
        return mqttConnectReturnCode;
    }

    static {
        MqttConnectReturnCode[] values = MqttConnectReturnCode.values();
        VALUES = new MqttConnectReturnCode[160];
        for (MqttConnectReturnCode code : values) {
            int unsignedByte = code.byteValue & 0xFF;
            MqttConnectReturnCode.VALUES[unsignedByte] = code;
        }
    }
}

