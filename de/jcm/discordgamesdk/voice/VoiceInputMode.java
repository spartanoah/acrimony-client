/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.voice;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class VoiceInputMode {
    private InputModeType type;
    private String shortcut;

    VoiceInputMode(int type, String shortcut) {
        this(InputModeType.javaValue(type), shortcut);
    }

    public VoiceInputMode(InputModeType type, String shortcut) {
        this.type = type;
        this.shortcut = shortcut;
    }

    public InputModeType getType() {
        return this.type;
    }

    public void setType(InputModeType type) {
        this.type = type;
    }

    public String getShortcut() {
        return this.shortcut;
    }

    public void setShortcut(String shortcut) {
        if (shortcut.getBytes(StandardCharsets.UTF_8).length >= 256) {
            throw new IllegalArgumentException("max shortcut length is 255");
        }
        this.shortcut = shortcut;
    }

    public String toString() {
        return "VoiceInputMode{type=" + (Object)((Object)this.type) + ", shortcut='" + this.shortcut + '\'' + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        VoiceInputMode that = (VoiceInputMode)o;
        return this.type == that.type && Objects.equals(this.shortcut, that.shortcut);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.type, this.shortcut});
    }

    int getNativeType() {
        return this.getType().nativeValue();
    }

    public static enum InputModeType {
        VOICE_ACTIVITY,
        PUSH_TO_TALK;

        private static final int OFFSET = 0;

        private int nativeValue() {
            return this.ordinal() + 0;
        }

        private static InputModeType javaValue(int nativeValue) {
            return InputModeType.values()[nativeValue - 0];
        }
    }
}

