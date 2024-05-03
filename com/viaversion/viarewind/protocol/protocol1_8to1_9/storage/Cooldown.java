/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.CooldownVisualization;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import com.viaversion.viarewind.utils.Tickable;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.Pair;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class Cooldown
extends StoredObject
implements Tickable {
    private double attackSpeed = 4.0;
    private long lastHit = 0L;
    private CooldownVisualization.Factory visualizationFactory = CooldownVisualization.Factory.fromConfiguration();
    private CooldownVisualization current;

    public Cooldown(UserConnection user) {
        super(user);
    }

    @Override
    public void tick() {
        if (!this.hasCooldown()) {
            this.endCurrentVisualization();
            return;
        }
        BlockPlaceDestroyTracker tracker = this.getUser().get(BlockPlaceDestroyTracker.class);
        if (tracker.isMining()) {
            this.lastHit = 0L;
            this.endCurrentVisualization();
            return;
        }
        if (this.current == null) {
            this.current = this.visualizationFactory.create(this.getUser());
        }
        try {
            this.current.show(this.getCooldown());
        } catch (Exception exception) {
            ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Unable to show cooldown visualization", exception);
        }
    }

    private void endCurrentVisualization() {
        if (this.current != null) {
            try {
                this.current.hide();
            } catch (Exception exception) {
                ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Unable to hide cooldown visualization", exception);
            }
            this.current = null;
        }
    }

    public boolean hasCooldown() {
        long time = System.currentTimeMillis() - this.lastHit;
        double cooldown = this.restrain((double)time * this.attackSpeed / 1000.0, 0.0, 1.5);
        return cooldown > 0.1 && cooldown < 1.1;
    }

    public double getCooldown() {
        long time = System.currentTimeMillis() - this.lastHit;
        return this.restrain((double)time * this.attackSpeed / 1000.0, 0.0, 1.0);
    }

    private double restrain(double x, double a, double b) {
        if (x < a) {
            return a;
        }
        return Math.min(x, b);
    }

    public double getAttackSpeed() {
        return this.attackSpeed;
    }

    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public void setAttackSpeed(double base, ArrayList<Pair<Byte, Double>> modifiers) {
        int j;
        this.attackSpeed = base;
        for (j = 0; j < modifiers.size(); ++j) {
            if (modifiers.get(j).key() != 0) continue;
            this.attackSpeed += modifiers.get(j).value().doubleValue();
            modifiers.remove(j--);
        }
        for (j = 0; j < modifiers.size(); ++j) {
            if (modifiers.get(j).key() != 1) continue;
            this.attackSpeed += base * modifiers.get(j).value();
            modifiers.remove(j--);
        }
        for (j = 0; j < modifiers.size(); ++j) {
            if (modifiers.get(j).key() != 2) continue;
            this.attackSpeed *= 1.0 + modifiers.get(j).value();
            modifiers.remove(j--);
        }
    }

    public void hit() {
        this.lastHit = System.currentTimeMillis();
    }

    public void setLastHit(long lastHit) {
        this.lastHit = lastHit;
    }

    public void setVisualizationFactory(CooldownVisualization.Factory visualizationFactory) {
        this.visualizationFactory = Objects.requireNonNull(visualizationFactory, "visualizationFactory");
    }
}

