/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.annotation.JsonValue;

public enum JsonValueFormat {
    COLOR("color"),
    DATE("date"),
    DATE_TIME("date-time"),
    EMAIL("email"),
    HOST_NAME("host-name"),
    IP_ADDRESS("ip-address"),
    IPV6("ipv6"),
    PHONE("phone"),
    REGEX("regex"),
    STYLE("style"),
    TIME("time"),
    URI("uri"),
    UTC_MILLISEC("utc-millisec"),
    UUID("uuid");

    private final String _desc;

    private JsonValueFormat(String desc) {
        this._desc = desc;
    }

    @JsonValue
    public String toString() {
        return this._desc;
    }
}

