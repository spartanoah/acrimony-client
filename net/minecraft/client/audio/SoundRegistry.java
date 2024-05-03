/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;

public class SoundRegistry
extends RegistrySimple<ResourceLocation, SoundEventAccessorComposite> {
    private Map<ResourceLocation, SoundEventAccessorComposite> soundRegistry;

    @Override
    protected Map<ResourceLocation, SoundEventAccessorComposite> createUnderlyingMap() {
        this.soundRegistry = Maps.newHashMap();
        return this.soundRegistry;
    }

    public void registerSound(SoundEventAccessorComposite p_148762_1_) {
        this.putObject(p_148762_1_.getSoundEventLocation(), p_148762_1_);
    }

    public void clearMap() {
        this.soundRegistry.clear();
    }
}

