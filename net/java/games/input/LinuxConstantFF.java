/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.LinuxEventDevice;
import net.java.games.input.LinuxForceFeedbackEffect;

final class LinuxConstantFF
extends LinuxForceFeedbackEffect {
    public LinuxConstantFF(LinuxEventDevice device) throws IOException {
        super(device);
    }

    protected final int upload(int id, float intensity) throws IOException {
        int scaled_intensity = Math.round(intensity * 32767.0f);
        return this.getDevice().uploadConstantEffect(id, 0, 0, 0, 0, 0, scaled_intensity, 0, 0, 0, 0);
    }
}

