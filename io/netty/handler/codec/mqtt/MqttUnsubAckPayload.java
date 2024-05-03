/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MqttUnsubAckPayload {
    private final List<Short> unsubscribeReasonCodes;
    private static final MqttUnsubAckPayload EMPTY = new MqttUnsubAckPayload(new short[0]);

    public static MqttUnsubAckPayload withEmptyDefaults(MqttUnsubAckPayload payload) {
        if (payload == null) {
            return EMPTY;
        }
        return payload;
    }

    public MqttUnsubAckPayload(short ... unsubscribeReasonCodes) {
        ObjectUtil.checkNotNull(unsubscribeReasonCodes, "unsubscribeReasonCodes");
        ArrayList<Short> list = new ArrayList<Short>(unsubscribeReasonCodes.length);
        short[] sArray = unsubscribeReasonCodes;
        int n = sArray.length;
        for (int i = 0; i < n; ++i) {
            Short v = sArray[i];
            list.add(v);
        }
        this.unsubscribeReasonCodes = Collections.unmodifiableList(list);
    }

    public MqttUnsubAckPayload(Iterable<Short> unsubscribeReasonCodes) {
        ObjectUtil.checkNotNull(unsubscribeReasonCodes, "unsubscribeReasonCodes");
        ArrayList<Short> list = new ArrayList<Short>();
        for (Short v : unsubscribeReasonCodes) {
            ObjectUtil.checkNotNull(v, "unsubscribeReasonCode");
            list.add(v);
        }
        this.unsubscribeReasonCodes = Collections.unmodifiableList(list);
    }

    public List<Short> unsubscribeReasonCodes() {
        return this.unsubscribeReasonCodes;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "unsubscribeReasonCodes=" + this.unsubscribeReasonCodes + ']';
    }
}

