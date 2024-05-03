/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.snbt;

import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtDeserializeException;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;

public interface ISNbtDeserializer<T extends Tag> {
    public T deserialize(String var1) throws SNbtDeserializeException;

    public Tag deserializeValue(String var1) throws SNbtDeserializeException;
}

