/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.snbt;

import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;

public interface ISNbtSerializer {
    public String serialize(Tag var1) throws SNbtSerializeException;
}

