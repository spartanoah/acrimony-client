/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.event.Listener;
import Acrimony.event.impl.JumpEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.StrafeEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.module.impl.player.Scaffold;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.ModuleUtil;
import Acrimony.util.player.FixedRotations;
import Acrimony.util.player.MovementUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.MathHelper;

public class Turnback
extends Module {
    private final BooleanSetting applyForKillaura = new BooleanSetting("Apply for Killaura", true);
    private final BooleanSetting applyForScaffold = new BooleanSetting("Apply for Scaffold", true);
    private final BooleanSetting movementFix = new BooleanSetting("Movement fix", false);
    private final ModeSetting yawSpeedMode = new ModeSetting("Yaw speed mode", "Randomised", "Randomised", "Acceleration");
    private final DoubleSetting minYawSpeed = new DoubleSetting("Min yaw speed", 30.0, 5.0, 180.0, 2.5);
    private final DoubleSetting maxYawSpeed = new DoubleSetting("Max yaw speed", 35.0, 5.0, 180.0, 2.5);
    private final DoubleSetting minYawAccel = new DoubleSetting("Min yaw accel", () -> this.yawSpeedMode.is("Acceleration"), 4.0, 0.0, 25.0, 0.25);
    private final DoubleSetting maxYawAccel = new DoubleSetting("Max yaw accel", () -> this.yawSpeedMode.is("Acceleration"), 4.0, 0.0, 25.0, 0.25);
    private final DoubleSetting minPitchSpeed = new DoubleSetting("Min pitch speed", 10.0, 2.0, 180.0, 2.0);
    private final DoubleSetting maxPitchSpeed = new DoubleSetting("Max pitch speed", 10.0, 2.0, 180.0, 2.0);
    private final DoubleSetting minYawChange = new DoubleSetting("Min yaw change", 2.0, 0.0, 5.0, 0.1);
    private final DoubleSetting minPitchChange = new DoubleSetting("Min pitch change", 0.8, 0.0, 5.0, 0.1);
    private final DoubleSetting rotsRandomisation = new DoubleSetting("Rots randomisation", 1.6, 0.0, 6.0, 0.1);
    private FixedRotations rotations;
    private boolean rotating;
    private Killaura killauraModule;
    private Scaffold scaffoldModule;
    private boolean killauraHadTarget;
    private boolean wasScaffoldEnabled;
    private boolean shouldResetAccel;
    private double yawSpeed;
    private boolean yawDone;
    private boolean pitchDone;

    public Turnback() {
        super("Turnback", Category.MOVEMENT);
        this.addSettings(this.applyForKillaura, this.applyForScaffold, this.movementFix, this.yawSpeedMode, this.minYawSpeed, this.maxYawSpeed, this.minYawAccel, this.maxYawAccel, this.minPitchSpeed, this.maxPitchSpeed, this.minYawChange, this.minPitchChange, this.rotsRandomisation);
    }

    @Override
    public void onClientStarted() {
        this.killauraModule = ModuleUtil.getKillaura();
        this.scaffoldModule = ModuleUtil.getScaffold();
    }

    @Override
    public void onEnable() {
        this.rotations = new FixedRotations(Turnback.mc.thePlayer.rotationYaw, Turnback.mc.thePlayer.rotationPitch);
        this.killauraHadTarget = false;
        this.wasScaffoldEnabled = false;
        this.yawDone = false;
        this.pitchDone = false;
        this.shouldResetAccel = true;
    }

    @Listener(value=3)
    public void onTick(TickEvent event) {
        if (Turnback.mc.thePlayer.ticksExisted < 10) {
            this.rotating = false;
        }
        if (this.rotations == null) {
            this.rotations = new FixedRotations(Turnback.mc.thePlayer.rotationYaw, Turnback.mc.thePlayer.rotationPitch);
        }
        boolean killauraHasTarget = this.killauraModule.isEnabled() && this.killauraModule.getTarget() != null;
        boolean scaffoldEnabled = this.scaffoldModule.isEnabled();
        if (!killauraHasTarget && this.killauraHadTarget && this.applyForKillaura.isEnabled()) {
            this.rotating = true;
            this.yawDone = false;
            this.pitchDone = false;
            this.shouldResetAccel = true;
        } else if (!scaffoldEnabled && this.wasScaffoldEnabled && this.applyForScaffold.isEnabled()) {
            this.rotating = true;
            this.yawDone = false;
            this.pitchDone = false;
            this.shouldResetAccel = true;
        }
        if (this.rotating) {
            this.updateRotations(new float[]{Turnback.mc.thePlayer.rotationYaw, Turnback.mc.thePlayer.rotationPitch}, this.yawSpeedMode.getMode(), this.minYawSpeed.getValue(), this.maxYawSpeed.getValue(), this.minYawAccel.getValue(), this.maxYawAccel.getValue(), this.minPitchSpeed.getValue(), this.maxPitchSpeed.getValue(), this.minYawChange.getValue(), this.minPitchChange.getValue(), this.rotsRandomisation.getValue());
        }
        if (this.yawDone && this.pitchDone) {
            this.rotating = false;
        }
        if (this.rotating && this.movementFix.isEnabled()) {
            float value;
            float diff = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(this.rotations.getYaw()) - MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection())) + 22.5f;
            if (diff < 0.0f) {
                diff = 360.0f + diff;
            }
            int a = (int)((double)diff / 45.0);
            float forward = value = Turnback.mc.thePlayer.moveForward != 0.0f ? Math.abs(Turnback.mc.thePlayer.moveForward) : Math.abs(Turnback.mc.thePlayer.moveStrafing);
            float strafe = 0.0f;
            for (int i = 0; i < 8 - a; ++i) {
                float[] dirs = MovementUtil.incrementMoveDirection(forward, strafe);
                forward = dirs[0];
                strafe = dirs[1];
            }
            if (forward < 0.8f) {
                Turnback.mc.gameSettings.keyBindSprint.pressed = false;
                Turnback.mc.thePlayer.setSprinting(false);
            }
        }
        this.killauraHadTarget = killauraHasTarget;
        this.wasScaffoldEnabled = scaffoldEnabled;
    }

    @Listener
    public void onJump(JumpEvent event) {
        if (this.rotating && this.movementFix.isEnabled()) {
            event.setYaw(this.rotations.getYaw());
        }
    }

    @Listener
    public void onStrafe(StrafeEvent event) {
        if (this.rotating && this.movementFix.isEnabled()) {
            float value;
            float diff = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(this.rotations.getYaw()) - MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection())) + 22.5f;
            if (diff < 0.0f) {
                diff = 360.0f + diff;
            }
            int a = (int)((double)diff / 45.0);
            float forward = value = event.getForward() != 0.0f ? Math.abs(event.getForward()) : Math.abs(event.getStrafe());
            float strafe = 0.0f;
            for (int i = 0; i < 8 - a; ++i) {
                float[] dirs = MovementUtil.incrementMoveDirection(forward, strafe);
                forward = dirs[0];
                strafe = dirs[1];
            }
            event.setForward(forward);
            event.setStrafe(strafe);
            event.setYaw(this.rotations.getYaw());
        }
    }

    @Listener(value=3)
    public void onMotion(MotionEvent event) {
        if (this.rotations != null) {
            if (!this.rotating) {
                this.rotations.updateRotations(event.getYaw(), event.getPitch());
            }
        } else {
            this.rotations = new FixedRotations(event.getYaw(), event.getPitch());
        }
        if (this.rotating) {
            event.setYaw(this.rotations.getYaw());
            event.setPitch(this.rotations.getPitch());
        }
    }

    private void updateRotations(float[] rots, String yawSpeedMode, double minYawSpeed, double maxYawSpeed, double minYawAccel, double maxYawAccel, double minPitchSpeed, double maxPitchSpeed, double minYawChange, double minPitchChange, double rotsRandomisation) {
        boolean pitchChange;
        float requestedYaw = rots[0];
        float requestedPitch = rots[1];
        float yaw = this.rotations.getYaw();
        float pitch = this.rotations.getPitch();
        float aaaa = MathHelper.wrapAngleTo180_float(yaw);
        float bbbb = MathHelper.wrapAngleTo180_float(requestedYaw);
        float yawDiff = Math.abs(bbbb - aaaa);
        float pitchDiff = Math.abs(requestedPitch - pitch);
        if (yawDiff < 10.0f && pitchDiff < 10.0f) {
            this.yawDone = true;
            this.pitchDone = true;
            return;
        }
        double randomAmount = Math.random() * rotsRandomisation;
        if (yawSpeedMode.equals("Randomised")) {
            this.yawSpeed = maxYawSpeed > minYawSpeed ? ThreadLocalRandom.current().nextDouble(minYawSpeed, maxYawSpeed) : minYawSpeed;
        } else if (yawSpeedMode.equals("Acceleration")) {
            if (this.shouldResetAccel) {
                this.yawSpeed = minYawSpeed;
            } else {
                this.yawSpeed += maxYawAccel > minYawAccel ? ThreadLocalRandom.current().nextDouble(minYawAccel, maxYawAccel) : minYawAccel;
                this.yawSpeed = Math.min(this.yawSpeed, maxYawSpeed);
            }
            this.shouldResetAccel = false;
        }
        double pitchSpeed = maxPitchSpeed > minPitchSpeed ? ThreadLocalRandom.current().nextDouble(minPitchSpeed, maxPitchSpeed) : minPitchSpeed;
        boolean yawChange = (double)yawDiff > minYawChange && (double)yawDiff < 360.0 - minYawChange;
        boolean bl = pitchChange = (double)pitchDiff > minPitchChange;
        if (yawChange) {
            this.yawDone = false;
            yaw = yawDiff > 180.0f ? (bbbb > aaaa ? (float)((double)yaw - Math.min(this.yawSpeed, (double)yawDiff)) : (float)((double)yaw + Math.min(this.yawSpeed, (double)yawDiff))) : (bbbb > aaaa ? (float)((double)yaw + Math.min(this.yawSpeed, (double)yawDiff)) : (float)((double)yaw - Math.min(this.yawSpeed, (double)yawDiff)));
            yaw = (float)((double)yaw + (Math.random() * randomAmount - randomAmount * 0.5));
        } else {
            this.yawDone = true;
            this.shouldResetAccel = true;
        }
        this.yawSpeed = Math.min(this.yawSpeed, (double)Math.abs(this.rotations.getYaw() - yaw));
        if (pitchChange) {
            this.pitchDone = false;
            pitch = requestedPitch > pitch ? (float)((double)pitch + Math.min(pitchSpeed, (double)pitchDiff)) : (float)((double)pitch - Math.min(pitchSpeed, (double)pitchDiff));
            if ((pitch = (float)((double)pitch + (Math.random() * randomAmount - randomAmount * 0.5))) > 88.0f) {
                pitch = 90.0f;
            } else if (pitch < -88.0f) {
                pitch = -90.0f;
            }
        } else {
            this.pitchDone = true;
        }
        this.rotations.updateRotations(yaw, pitch);
    }
}

