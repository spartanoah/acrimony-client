/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.ai;

public abstract class EntityAIBase {
    private int mutexBits;

    public abstract boolean shouldExecute();

    public boolean continueExecuting() {
        return this.shouldExecute();
    }

    public boolean isInterruptible() {
        return true;
    }

    public void startExecuting() {
    }

    public void resetTask() {
    }

    public void updateTask() {
    }

    public void setMutexBits(int mutexBitsIn) {
        this.mutexBits = mutexBitsIn;
    }

    public int getMutexBits() {
        return this.mutexBits;
    }
}

