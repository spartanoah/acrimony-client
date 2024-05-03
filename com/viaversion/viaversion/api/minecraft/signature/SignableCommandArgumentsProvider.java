/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.signature;

import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.util.Pair;
import java.util.List;

public abstract class SignableCommandArgumentsProvider
implements Provider {
    public abstract List<Pair<String, String>> getSignableArguments(String var1);
}

