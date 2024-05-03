/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.ViaRewindConfig;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.ActionBarVisualization;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.BossBarVisualization;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.DisabledCooldownVisualization;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.TitleCooldownVisualization;
import com.viaversion.viaversion.api.connection.UserConnection;

public interface CooldownVisualization {
    public static final int MAX_PROGRESS_TEXT_LENGTH = 10;

    public void show(double var1) throws Exception;

    public void hide() throws Exception;

    public static String buildProgressText(String symbol, double cooldown) {
        int green = (int)Math.floor(10.0 * cooldown);
        int grey = 10 - green;
        StringBuilder builder = new StringBuilder("\u00a78");
        while (green-- > 0) {
            builder.append(symbol);
        }
        builder.append("\u00a77");
        while (grey-- > 0) {
            builder.append(symbol);
        }
        return builder.toString();
    }

    public static interface Factory {
        public static final Factory DISABLED = user -> new DisabledCooldownVisualization();

        public CooldownVisualization create(UserConnection var1);

        public static Factory fromConfiguration() {
            try {
                return Factory.fromIndicator(ViaRewind.getConfig().getCooldownIndicator());
            } catch (IllegalArgumentException e) {
                ViaRewind.getPlatform().getLogger().warning("Invalid cooldown-indicator setting");
                return DISABLED;
            }
        }

        public static Factory fromIndicator(ViaRewindConfig.CooldownIndicator indicator) {
            switch (indicator) {
                case TITLE: {
                    return TitleCooldownVisualization::new;
                }
                case BOSS_BAR: {
                    return BossBarVisualization::new;
                }
                case ACTION_BAR: {
                    return ActionBarVisualization::new;
                }
                case DISABLED: {
                    return DISABLED;
                }
            }
            throw new IllegalArgumentException("Unexpected: " + (Object)((Object)indicator));
        }
    }
}

