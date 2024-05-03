/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.automation;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.handler.automation.PendingPlacement;
import Acrimony.util.IMinecraft;
import Acrimony.util.player.FixedRotations;
import Acrimony.util.world.BlockInfo;
import Acrimony.util.world.WorldUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class AutomationHandler
implements IMinecraft {
    private PendingPlacement pendingPlacement;
    private FixedRotations rotations;
    private float oldYaw;
    private float oldPitch;
    private boolean lastPlacementSuccess;
    private boolean keepRotations;
    private boolean moveFix;
    private float lastPartialTicks;
    private int range = 3;

    public AutomationHandler() {
        Acrimony.instance.getEventManager().register(this);
    }

    @Listener
    public void onRender(RenderEvent event) {
        float partialTicks;
        this.lastPartialTicks = partialTicks = event.getPartialTicks();
    }

    private boolean attemptPlacement() {
        BlockInfo info = WorldUtil.getBlockInfo(this.pendingPlacement.x, this.pendingPlacement.y, this.pendingPlacement.z, this.range);
        boolean found = false;
        if (WorldUtil.isAirOrLiquid(new BlockPos(this.pendingPlacement.x, this.pendingPlacement.y, this.pendingPlacement.z)) && info != null) {
            float pitch;
            ArrayList<Float> yawValues = new ArrayList<Float>();
            if (this.moveFix) {
                for (float yaw = -180.0f; yaw <= 180.0f; yaw += 45.0f) {
                    yawValues.add(Float.valueOf(yaw));
                }
            } else {
                int i;
                float aaa = AutomationHandler.mc.thePlayer.rotationYaw;
                yawValues.add(Float.valueOf(AutomationHandler.mc.thePlayer.rotationYaw));
                for (i = 0; i < 30; ++i) {
                    yawValues.add(Float.valueOf(i % 2 == 0 ? aaa - (float)i : aaa + (float)i));
                }
                for (i = 30; i < 180; i += 5) {
                    yawValues.add(Float.valueOf(i % 2 == 0 ? aaa - (float)i : aaa + (float)i));
                }
            }
            ArrayList<Float> pitchValues = new ArrayList<Float>();
            if (!this.moveFix) {
                // empty if block
            }
            for (pitch = 70.0f; pitch <= 90.0f; pitch += pitch < 75.0f ? 1.0f : 0.1f) {
                pitchValues.add(Float.valueOf(pitch));
            }
            for (pitch = (float)(!this.moveFix ? 65 : 40); pitch <= 70.0f; pitch += 1.0f) {
                pitchValues.add(Float.valueOf(pitch));
            }
            Iterator iterator = yawValues.iterator();
            block5: while (iterator.hasNext()) {
                float yaw = ((Float)iterator.next()).floatValue();
                if (found) break;
                Iterator iterator2 = pitchValues.iterator();
                while (iterator2.hasNext()) {
                    float pitch2 = ((Float)iterator2.next()).floatValue();
                    this.rotations.updateRotations(yaw, pitch2);
                    MovingObjectPosition result = WorldUtil.raytrace(this.rotations.getYaw(), this.rotations.getPitch());
                    if (result == null || !result.getBlockPos().equals(info.pos) || result.sideHit != info.facing) continue;
                    AutomationHandler.mc.thePlayer.rotationYaw = this.rotations.getYaw();
                    AutomationHandler.mc.thePlayer.rotationPitch = this.rotations.getPitch();
                    AutomationHandler.mc.gameSettings.keyBindUseItem.pressed = true;
                    AutomationHandler.mc.rightClickDelayTimer = 0;
                    found = true;
                    continue block5;
                }
            }
        }
        return found;
    }

    @Listener(value=-127)
    public void onTick(TickEvent event) {
        if (this.pendingPlacement != null) {
            this.pendingPlacement = null;
        }
    }

    public PendingPlacement getPendingPlacement() {
        return this.pendingPlacement;
    }

    public void setPendingPlacement(PendingPlacement pendingPlacement, boolean keepRots, boolean moveFix, boolean onlyIfAirUnder) {
        if (!onlyIfAirUnder || WorldUtil.isAirOrLiquid(new BlockPos(pendingPlacement.x, pendingPlacement.y, pendingPlacement.z))) {
            this.keepRotations = keepRots;
            this.pendingPlacement = pendingPlacement;
            this.moveFix = moveFix;
            this.range = 3;
        }
    }

    public void setPendingPlacement(PendingPlacement pendingPlacement, boolean keepRots, boolean moveFix, boolean onlyIfAirUnder, int range) {
        if (!onlyIfAirUnder || WorldUtil.isAirOrLiquid(new BlockPos(pendingPlacement.x, pendingPlacement.y, pendingPlacement.z))) {
            this.keepRotations = keepRots;
            this.pendingPlacement = pendingPlacement;
            this.moveFix = moveFix;
            this.range = range;
        }
    }

    public boolean isRotating() {
        return this.rotations != null;
    }

    public float getOldYaw() {
        return this.oldYaw;
    }

    public float getOldPitch() {
        return this.oldPitch;
    }

    public boolean lastPlacementSuccess() {
        return this.lastPlacementSuccess;
    }
}

