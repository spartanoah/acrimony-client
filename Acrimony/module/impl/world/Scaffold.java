/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.world;

import Acrimony.Acrimony;
import Acrimony.event.Event;
import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.JumpEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.MoveEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.event.impl.StrafeEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.CustomDoubleSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.misc.KeyboardUtil;
import Acrimony.util.misc.LogUtil;
import Acrimony.util.network.PacketUtil;
import Acrimony.util.player.FixedRotations;
import Acrimony.util.player.InventoryUtil;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.RotationsUtil;
import Acrimony.util.world.BlockInfo;
import Acrimony.util.world.WorldUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class Scaffold
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Basic", "Basic", "Verus", "Hypixel", "Hypixel jump", "Basic custom", "Godbridge", "Sneak", "Andromeda");
    private final ModeSetting basicRotations = new ModeSetting("basic-rotations", "Rotations", () -> this.mode.is("Basic"), "Block center", "Movement based", "Block center");
    private final ModeSetting jumpSprintMode = new ModeSetting("Bypass Mode", () -> this.mode.is("Hypixel jump"), "Rise/Opal", "Rise/Opal", "Novoline");
    private final ModeSetting jumpMode = new ModeSetting("Jump mode", () -> this.mode.is("Basic"), "None", "None", "Normal", "Place when falling");
    private final ModeSetting noSprint = new ModeSetting("No sprint mode", () -> this.mode.is("Basic"), "None", "None", "Enabled", "Spoof");
    private final BooleanSetting showRotationsSettings = new BooleanSetting("Show rotations settings", () -> this.mode.is("Basic custom"), false);
    private final BooleanSetting showMovementSettings = new BooleanSetting("Show movement settings", () -> this.mode.is("Basic custom"), false);
    private final BooleanSetting showPlacementSettings = new BooleanSetting("Show placement settings", () -> this.mode.is("Basic custom"), false);
    private final ModeSetting bCustomRotationsTiming = new ModeSetting("bCustomRotationsTiming", "Rotations timing", () -> this.mode.is("Basic custom") && this.showRotationsSettings.isEnabled(), "Always", "Never", "Always", "Over air", "When placing", "When not jumping");
    private final ModeSetting bCustomRotationsMode = new ModeSetting("bCustomRotations", "Rotations", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && this.showRotationsSettings.isEnabled(), "Facing", "Facing", "Block center", "Movement based", "Godbridge", "Raytrace pitch", "Static");
    private final IntegerSetting bCustomYawOffset = new IntegerSetting("bCustomYawOffset", "Yaw offset", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && (this.bCustomRotationsMode.is("Movement based") || this.bCustomRotationsMode.is("Raytrace pitch")) && this.showRotationsSettings.isEnabled(), 180, 0, 180, 5);
    private final CustomDoubleSetting bCustomPitchValue = new CustomDoubleSetting("bCustomPitch", "Pitch", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && (this.bCustomRotationsMode.is("Movement based") || this.bCustomRotationsMode.is("Facing")) && this.showRotationsSettings.isEnabled(), 82.0);
    private final BooleanSetting instantRotations = new BooleanSetting("Instant rotations", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && this.showRotationsSettings.isEnabled(), true);
    private final ModeSetting yawSpeedMode = new ModeSetting("Yaw speed mode", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), "Randomised", "Randomised", "Acceleration");
    private final DoubleSetting minYawSpeed = new DoubleSetting("Min yaw speed", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 30.0, 5.0, 180.0, 2.5);
    private final DoubleSetting maxYawSpeed = new DoubleSetting("Max yaw speed", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 35.0, 5.0, 180.0, 2.5);
    private final DoubleSetting minYawAccel = new DoubleSetting("Min yaw accel", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.yawSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), 4.0, 0.0, 25.0, 0.25);
    private final DoubleSetting maxYawAccel = new DoubleSetting("Max yaw accel", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.yawSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), 4.0, 0.0, 25.0, 0.25);
    private final BooleanSetting reduceYawSpeedWhenAlmostDone = new BooleanSetting("Reduce yaw speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.yawSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), false);
    private final DoubleSetting minYawSpeedWhenAlmostDone = new DoubleSetting("Min yaw speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.yawSpeedMode.is("Acceleration") && this.reduceYawSpeedWhenAlmostDone.isEnabled() && this.showRotationsSettings.isEnabled(), 5.0, 0.0, 50.0, 0.5);
    private final DoubleSetting maxYawSpeedWhenAlmostDone = new DoubleSetting("Max yaw speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.yawSpeedMode.is("Acceleration") && this.reduceYawSpeedWhenAlmostDone.isEnabled() && this.showRotationsSettings.isEnabled(), 5.0, 0.0, 50.0, 0.5);
    private final ModeSetting pitchSpeedMode = new ModeSetting("Pitch speed mode", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), "Randomised", "Randomised", "Acceleration");
    private final DoubleSetting minPitchSpeed = new DoubleSetting("Min pitch speed", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 10.0, 2.0, 180.0, 2.0);
    private final DoubleSetting maxPitchSpeed = new DoubleSetting("Max pitch speed", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 10.0, 2.0, 180.0, 2.0);
    private final DoubleSetting minPitchAccel = new DoubleSetting("Min pitch accel", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.pitchSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), 4.0, 0.0, 25.0, 0.25);
    private final DoubleSetting maxPitchAccel = new DoubleSetting("Max pitch accel", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.pitchSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), 4.0, 0.0, 25.0, 0.25);
    private final BooleanSetting reducePitchSpeedWhenAlmostDone = new BooleanSetting("Reduce pitch speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.pitchSpeedMode.is("Acceleration") && this.showRotationsSettings.isEnabled(), false);
    private final DoubleSetting minPitchSpeedWhenAlmostDone = new DoubleSetting("Min pitch speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.pitchSpeedMode.is("Acceleration") && this.reducePitchSpeedWhenAlmostDone.isEnabled() && this.showRotationsSettings.isEnabled(), 5.0, 0.0, 50.0, 0.5);
    private final DoubleSetting maxPitchSpeedWhenAlmostDone = new DoubleSetting("Max pitch speed when almost done", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.pitchSpeedMode.is("Acceleration") && this.reducePitchSpeedWhenAlmostDone.isEnabled() && this.showRotationsSettings.isEnabled(), 5.0, 0.0, 50.0, 0.5);
    private final DoubleSetting minYawChange = new DoubleSetting("Min yaw change", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 2.0, 0.0, 5.0, 0.1);
    private final DoubleSetting minPitchChange = new DoubleSetting("Min pitch change", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 0.8, 0.0, 5.0, 0.1);
    private final DoubleSetting rotsRandomisation = new DoubleSetting("Rots randomisation", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), 1.6, 0.0, 25.0, 0.25);
    private final BooleanSetting bCustomResetRotsIfNotRotating = new BooleanSetting("bCustomResetRotsIfNotRotating", "Reset rots if not rotating", () -> this.mode.is("Basic custom") && !this.bCustomRotationsTiming.is("Never") && !this.bCustomRotationsTiming.is("Always") && !this.instantRotations.isEnabled() && this.showRotationsSettings.isEnabled(), false);
    private final ModeSetting customNoSprintTiming = new ModeSetting("customNoSprintTiming", "No sprint timing", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), "Never", "Never", "Always", "Over air", "When placing", "Onground", "Offground");
    private final ModeSetting customNoSprintMode = new ModeSetting("customNoSprintMode", "No sprint mode", () -> this.mode.is("Basic custom") && !this.customNoSprintTiming.is("Never") && this.showMovementSettings.isEnabled(), "Enabled", "Enabled", "Spoof");
    private final BooleanSetting customAllowJumpBoost = new BooleanSetting("customAllowJumpBoost", "Allow jump boost", () -> this.mode.is("Basic custom") && (!this.customNoSprintTiming.is("Always") || !this.noSprint.is("Enabled")) && this.showMovementSettings.isEnabled(), true);
    private final ModeSetting customSneakTiming = new ModeSetting("customSneakTiming", "Sneak timing", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), "Never", "Never", "Always", "Over air", "Over air and place fail", "When placing", "Every x blocks", "Alternate");
    private final IntegerSetting customSneakFrequency = new IntegerSetting("customSneakFrequency", "Sneak frequency", () -> this.mode.is("Basic custom") && this.customSneakTiming.is("Every x blocks") && this.showMovementSettings.isEnabled(), 2, 1, 10, 1);
    private final BooleanSetting customSneakOffground = new BooleanSetting("customSneakOffground", "Sneak offground", () -> this.mode.is("Basic custom") && !this.customSneakTiming.is("Never") && this.showMovementSettings.isEnabled(), false);
    private final ModeSetting customSneakMode = new ModeSetting("customSneakMode", "Sneak mode", () -> this.mode.is("Basic custom") && !this.customSneakTiming.is("Never") && this.showMovementSettings.isEnabled(), "Enabled", "Enabled", "Spoof");
    private final ModeSetting customGroundspoof = new ModeSetting("customGroundspoof", "Groundspoof", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), "Disabled", "Disabled", "Offground", "Alternate");
    private final ModeSetting customJumpMode = new ModeSetting("customJumpMode", "Jump mode", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), "None", "None", "Normal", "Place when falling", "Godbridge");
    private final ModeSetting movementType = new ModeSetting("Movement type", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), "Normal", "Normal", "Strafe", "Fixed");
    private final DoubleSetting customMotionMultOnGround = new DoubleSetting("customMotionMultOnGround", "Motion mult onground", () -> this.mode.is("Basic custom") && this.movementType.is("Normal") && this.showMovementSettings.isEnabled(), 1.0, 0.5, 1.4, 0.01);
    private final DoubleSetting customMotionMultOffGround = new DoubleSetting("customMotionMultOffGround", "Motion mult offground", () -> this.mode.is("Basic custom") && this.movementType.is("Normal") && this.showMovementSettings.isEnabled(), 1.0, 0.5, 1.4, 0.01);
    private final BooleanSetting customMultAffectsNextMotion = new BooleanSetting("customMultAffectsNextMotion", "Mult affects next motion", () -> this.mode.is("Basic custom") && this.movementType.is("Normal") && this.showMovementSettings.isEnabled(), false);
    private final DoubleSetting customJumpBoostAmount = new DoubleSetting("customJumpBoostAmount", "Jump boost amount", () -> this.mode.is("Basic custom") && this.movementType.is("Normal") && (!this.customNoSprintTiming.is("Always") || !this.noSprint.is("Enabled")) && this.customAllowJumpBoost.isEnabled() && this.showMovementSettings.isEnabled(), 0.2, 0.0, 0.4, 0.005);
    private final DoubleSetting customOnGroundSpeed = new DoubleSetting("customOnGroundSpeed", "Onground speed", () -> this.mode.is("Basic custom") && this.movementType.is("Strafe") && this.showMovementSettings.isEnabled(), 0.28, 0.1, 0.5, 0.005);
    private final BooleanSetting customOffGroundStrafe = new BooleanSetting("customOffGroundStrafe", "Offground strafe", () -> this.mode.is("Basic custom") && this.movementType.is("Strafe") && this.showMovementSettings.isEnabled(), true);
    private final DoubleSetting customOffGroundSpeed = new DoubleSetting("customOffGroundSpeed", "Offground speed", () -> this.mode.is("Basic custom") && this.movementType.is("Strafe") && this.customOffGroundStrafe.isEnabled() && this.showMovementSettings.isEnabled(), 0.28, 0.1, 0.5, 0.005);
    private final DoubleSetting customOnGroundPotionExtra = new DoubleSetting("customOnGroundPotionExtra", "Onground potion extra", () -> this.mode.is("Basic custom") && this.movementType.is("Strafe") && this.showMovementSettings.isEnabled(), 0.02, 0.0, 0.2, 0.005);
    private final DoubleSetting customOffGroundPotionExtra = new DoubleSetting("customOffGroundPotionExtra", "Offground potion extra", () -> this.mode.is("Basic custom") && this.movementType.is("Strafe") && this.customOffGroundStrafe.isEnabled() && this.showMovementSettings.isEnabled(), 0.02, 0.0, 0.2, 0.005);
    private final BooleanSetting customIgnoreSpeedPot = new BooleanSetting("customIgnoreSpeedPot", "Ignore speed pot", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), false);
    private final IntegerSetting customNoMoveTicksOnStart = new IntegerSetting("customNoMoveTicksOnStart", "No move ticks on start", () -> this.mode.is("Basic custom") && this.showMovementSettings.isEnabled(), 0, 0, 5, 1);
    private final BooleanSetting bCustomOnlyPlaceIfRaytraceSuccess = new BooleanSetting("bCustomOnlyPlaceIfRaytraceSuccess", "Only place if raytrace success", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), true);
    private final IntegerSetting range = new IntegerSetting("Range", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), 2, 1, 4, 1);
    private final DoubleSetting distFromBlock = new DoubleSetting("Dist from block", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), 0.0, 0.0, 0.24, 0.01);
    private final DoubleSetting offGroundDistFromBlock = new DoubleSetting("Offground dist from block", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), 0.0, 0.0, 0.24, 0.01);
    private final IntegerSetting minPlaceDelay = new IntegerSetting("Min place delay", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), 1, 1, 10, 1);
    private final IntegerSetting maxPlaceDelay = new IntegerSetting("Max place delay", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), 1, 1, 10, 1);
    private final BooleanSetting applyPlaceDelayOffground = new BooleanSetting("Apply place delay offground", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled(), false);
    private final ModeSetting extraRightClicks = new ModeSetting("Extra right clicks", () -> this.mode.is("Basic custom") && this.showPlacementSettings.isEnabled() || this.mode.is("Sneak") || this.mode.is("Godbridge"), "Normal", "Disabled", "Normal", "Dragclick", "Always");
    private final BooleanSetting modulo45Rots = new BooleanSetting("Modulo 45 rots", () -> this.mode.is("Raytrace custom") && this.showRotationsSettings.isEnabled(), true);
    private final IntegerSetting expand = new IntegerSetting("Expand", () -> this.mode.is("Basic"), 0, 0, 4, 1);
    private final BooleanSetting jump = new BooleanSetting("Jump", () -> this.mode.is("Verus"), false);
    private final ModeSetting hypixelSprint = new ModeSetting("Hypixel sprint", () -> this.mode.is("Hypixel"), "None", "None", "Semi");
    private final BooleanSetting jumpToAvoidSetback = new BooleanSetting("Jump to avoid setback", () -> this.mode.is("Hypixel") && this.hypixelSprint.is("Semi"), true);
    private final ModeSetting hypixelTower = new ModeSetting("Hypixel tower", () -> this.mode.is("Hypixel"), "Faster vertically", "None", "Faster vertically", "Faster horizontally", "Legit");
    private final DoubleSetting towerSpeed = new DoubleSetting("Speed", () -> this.mode.is("Hypixel") && this.hypixelTower.is("Faster vertically"), 0.28, 0.2, 0.28, 0.005);
    private final DoubleSetting towerSpeedWhenDiagonal = new DoubleSetting("Diagonal Speed", () -> this.mode.is("Hypixel") && this.hypixelTower.is("Faster vertically"), 0.22, 0.2, 0.28, 0.005);
    private final BooleanSetting stillPlaceOnRaytraceFail = new BooleanSetting("Still place on raytrace fail", () -> this.mode.is("Godbridge"), true);
    private final BooleanSetting debugOnRaytraceFail = new BooleanSetting("Debug on raytrace fail", () -> this.mode.is("Godbridge"), true);
    private final BooleanSetting rotationsEnabled = new BooleanSetting("rotations-enabled", "Rotations", () -> this.mode.is("Andromeda"), true);
    private final BooleanSetting moveFix = new BooleanSetting("Move fix", () -> this.mode.is("Andromeda") && this.rotationsEnabled.isEnabled(), true);
    private final BooleanSetting noPlaceOnJumpTick = new BooleanSetting("No place on jump tick", () -> this.mode.is("Andromeda") && this.rotationsEnabled.isEnabled() && this.moveFix.isEnabled(), false);
    public final BooleanSetting blockPlaceESP = new BooleanSetting("Block place ESP", false);
    private final ModeSetting blockPicker = new ModeSetting("Block picker", "Switch", "None", "Switch", "Spoof");
    private final BooleanSetting swingAnimation = new BooleanSetting("Swing animation", false);
    private final ModeSetting tower = new ModeSetting("Tower", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump"), "Legit", "None", "Vanilla", "NCP", "NCP2", "Hypixel", "Hypixel2", "Legit", "Custom");
    private final BooleanSetting showTeleportSettings = new BooleanSetting("Show teleport settings", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom"), false);
    private final BooleanSetting showMotionYSettings = new BooleanSetting("Show motionY settings", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom"), false);
    private final CustomDoubleSetting jumpMotionY = new CustomDoubleSetting("Jump motion Y", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), 0.42);
    private final ModeSetting teleportTick1 = new ModeSetting("Teleport tick 1", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick1 = new ModeSetting("Y motion tick 1", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue1 = new CustomDoubleSetting("Y motion value tick 1", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick1.is("Set motionY") || this.yMotionTick1.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick2 = new ModeSetting("Teleport tick 2", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick2 = new ModeSetting("Y motion tick 2", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue2 = new CustomDoubleSetting("Y motion value tick 2", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick2.is("Set motionY") || this.yMotionTick2.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick3 = new ModeSetting("Teleport tick 3", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick3 = new ModeSetting("Y motion tick 3", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue3 = new CustomDoubleSetting("Y motion value tick 3", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick3.is("Set motionY") || this.yMotionTick3.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick4 = new ModeSetting("Teleport tick 4", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick4 = new ModeSetting("Y motion tick 4", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue4 = new CustomDoubleSetting("Y motion value tick 4", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick4.is("Set motionY") || this.yMotionTick4.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick5 = new ModeSetting("Teleport tick 5", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick5 = new ModeSetting("Y motion tick 5", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue5 = new CustomDoubleSetting("Y motion value tick 5", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick5.is("Set motionY") || this.yMotionTick5.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick6 = new ModeSetting("Teleport tick 6", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick6 = new ModeSetting("Y motion tick 6", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue6 = new CustomDoubleSetting("Y motion value tick 6", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel2") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick6.is("Set motionY") || this.yMotionTick6.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick7 = new ModeSetting("Teleport tick 7", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick7 = new ModeSetting("Y motion tick 7", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && !this.mode.is("Hypixel2") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue7 = new CustomDoubleSetting("Y motion value tick 7", () -> !this.mode.is("Hypixel2") && !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick7.is("Set motionY") || this.yMotionTick7.is("Add motionY")), 0.0);
    private final ModeSetting teleportTick8 = new ModeSetting("Teleport tick 8", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showTeleportSettings.isEnabled(), "None", "Set pos to rounded Y", "Teleport to block over", "None");
    private final ModeSetting yMotionTick8 = new ModeSetting("Y motion tick 8", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), "None", "Set motionY", "Add motionY", "Jump again", "None");
    private final CustomDoubleSetting yMotionValue8 = new CustomDoubleSetting("Y motion value tick 8", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled() && (this.yMotionTick8.is("Set motionY") || this.yMotionTick8.is("Add motionY")), 0.0);
    private final BooleanSetting yChangeAffectsNextMotion = new BooleanSetting("Y change affects next motion", () -> !this.mode.is("Hypixel") && !this.mode.is("Hypixel jump") && this.tower.is("Custom") && this.showMotionYSettings.isEnabled(), true);
    private final ModeSetting towerTiming = new ModeSetting("Tower timing", () -> this.tower.is("Custom"), "Always", "Always", "Only when moving", "Only when not moving");
    private final DoubleSetting minMotionForMovement = new DoubleSetting("Min motion to define moving", () -> this.tower.is("Custom") && (this.towerTiming.is("Only when moving") || this.towerTiming.is("Only when not moving")), 0.1, 0.0, 0.2, 0.01);
    private double placeY;
    private BlockInfo info;
    private Vec3 rotationVec3;
    private FixedRotations rotations;
    private float lastTickYaw;
    private float lastTickPitch;
    private MovingObjectPosition requestedCursor;
    private boolean raytraceSuccess;
    private boolean startedRotating;
    private float facingYaw;
    private float facingPitch;
    private boolean overAir;
    private boolean placing;
    private boolean hasPlacedYet;
    private int sneakCounter;
    private int sneakPlacementCounter;
    private int offGroundTicks;
    private double yawSpeed;
    private boolean shouldResetYawAccel;
    private double pitchSpeed;
    private boolean shouldResetPitchAccel;
    private boolean yawDone;
    private boolean pitchDone;
    private int placeDelay;
    private int nextPlaceDelay;
    private int rightClickCounter;
    private float requestedRotationYaw;
    private float requestedRotationPitch;
    private int noMoveOnStartCounter;
    private int groundSpoofCounter;
    private float startingYaw;
    private boolean towering;
    private int towerTicks;
    private boolean startedSprint;
    private int sprintTicks;
    private boolean wasHovering;
    private int ticksHovering;
    private boolean wasTowering;
    private int toweringTicks;
    private boolean pendingMovementStop;
    private boolean jumpTick;
    private boolean changedKeybinds;
    private int oldSlot;
    private boolean changedSlot;
    private boolean started;
    private int counter;
    private int ticks;
    private boolean diagonally;
    private boolean lastDiagonal;
    private int blocksPlaced;
    private final Random random = new Random();
    private int randomNumber;
    private int randomNumber2;
    private double spoofedX;
    private double spoofedY;
    private double spoofedZ;
    private float renderedYaw;
    private float renderedPitch;

    public Scaffold() {
        super("Scaffold", Category.WORLD);
        this.addSettings(this.mode, this.jumpSprintMode, this.basicRotations, this.noSprint, this.jumpMode, this.showRotationsSettings, this.bCustomRotationsTiming, this.bCustomRotationsMode, this.bCustomYawOffset, this.bCustomPitchValue, this.instantRotations, this.yawSpeedMode, this.minYawSpeed, this.maxYawSpeed, this.minYawAccel, this.maxYawAccel, this.reduceYawSpeedWhenAlmostDone, this.minYawSpeedWhenAlmostDone, this.maxYawSpeedWhenAlmostDone, this.pitchSpeedMode, this.minPitchSpeed, this.maxPitchSpeed, this.minPitchAccel, this.maxPitchAccel, this.reducePitchSpeedWhenAlmostDone, this.minPitchSpeedWhenAlmostDone, this.maxPitchSpeedWhenAlmostDone, this.minYawChange, this.minPitchChange, this.rotsRandomisation, this.bCustomResetRotsIfNotRotating, this.modulo45Rots, this.showMovementSettings, this.customNoSprintTiming, this.customNoSprintMode, this.customAllowJumpBoost, this.customSneakTiming, this.customSneakFrequency, this.customSneakOffground, this.customSneakMode, this.customGroundspoof, this.customJumpMode, this.movementType, this.customMotionMultOnGround, this.customMotionMultOffGround, this.customMultAffectsNextMotion, this.customJumpBoostAmount, this.customOnGroundSpeed, this.customOffGroundStrafe, this.customOffGroundSpeed, this.customOnGroundPotionExtra, this.customOffGroundPotionExtra, this.customIgnoreSpeedPot, this.customNoMoveTicksOnStart, this.showPlacementSettings, this.bCustomOnlyPlaceIfRaytraceSuccess, this.range, this.distFromBlock, this.offGroundDistFromBlock, this.minPlaceDelay, this.maxPlaceDelay, this.applyPlaceDelayOffground, this.extraRightClicks, this.expand, this.jump, this.hypixelSprint, this.jumpToAvoidSetback, this.hypixelTower, this.towerSpeed, this.towerSpeedWhenDiagonal, this.stillPlaceOnRaytraceFail, this.debugOnRaytraceFail, this.rotationsEnabled, this.moveFix, this.noPlaceOnJumpTick, this.blockPlaceESP, this.blockPicker, this.swingAnimation, this.tower, this.showTeleportSettings, this.showMotionYSettings, this.jumpMotionY, this.teleportTick1, this.yMotionTick1, this.yMotionValue1, this.teleportTick2, this.yMotionTick2, this.yMotionValue2, this.teleportTick3, this.yMotionTick3, this.yMotionValue3, this.teleportTick4, this.yMotionTick4, this.yMotionValue4, this.teleportTick5, this.yMotionTick5, this.yMotionValue5, this.teleportTick6, this.yMotionTick6, this.yMotionValue6, this.teleportTick7, this.yMotionTick7, this.yMotionValue7, this.teleportTick8, this.yMotionTick8, this.yMotionValue8, this.yChangeAffectsNextMotion, this.towerTiming, this.minMotionForMovement);
        this.showRotationsSettings.setShownInColor(true);
        this.showMovementSettings.setShownInColor(true);
        this.showPlacementSettings.setShownInColor(true);
    }

    @Override
    public void onEnable() {
        this.placeY = Scaffold.mc.thePlayer.posY;
        this.info = null;
        this.rotationVec3 = null;
        this.startedRotating = false;
        this.facingYaw = Scaffold.mc.thePlayer.rotationYaw - 180.0f;
        this.facingPitch = (float)this.bCustomPitchValue.getValue();
        this.startingYaw = Scaffold.mc.thePlayer.rotationYaw;
        this.rotations = new FixedRotations(Scaffold.mc.thePlayer.rotationYaw, Scaffold.mc.thePlayer.rotationPitch);
        this.overAir = false;
        this.placing = false;
        this.hasPlacedYet = false;
        this.sneakCounter = 0;
        this.sneakPlacementCounter = 0;
        this.yawSpeed = this.minYawSpeed.getValue();
        this.pitchSpeed = this.minPitchSpeed.getValue();
        this.yawDone = false;
        this.pitchDone = false;
        this.shouldResetYawAccel = true;
        this.shouldResetPitchAccel = true;
        this.placeDelay = 0;
        this.nextPlaceDelay = this.getNextPlaceDelay();
        this.requestedCursor = null;
        this.rightClickCounter = 0;
        this.noMoveOnStartCounter = 0;
        this.groundSpoofCounter = 0;
        this.towering = false;
        this.towerTicks = 0;
        this.startedSprint = false;
        this.sprintTicks = 0;
        this.wasHovering = false;
        this.ticksHovering = 0;
        this.wasTowering = false;
        this.toweringTicks = 0;
        this.pendingMovementStop = false;
        this.jumpTick = false;
        this.changedKeybinds = false;
        this.started = false;
        this.counter = 0;
        this.ticks = 0;
        this.diagonally = false;
        this.blocksPlaced = 0;
        this.oldSlot = Scaffold.mc.thePlayer.inventory.currentItem;
        this.changedSlot = false;
        if (this.blockPicker.is("Spoof")) {
            Acrimony.instance.getSlotSpoofHandler().startSpoofing(Scaffold.mc.thePlayer.inventory.currentItem);
        }
        float yaw1 = MathHelper.wrapAngleTo180_float(Scaffold.mc.thePlayer.rotationYaw) + 720.0f;
        switch (this.mode.getMode()) {
            case "Hypixel": {
                if (!this.hypixelSprint.is("Semi") || this.jumpToAvoidSetback.isEnabled() && Scaffold.mc.thePlayer.onGround && !Scaffold.mc.gameSettings.keyBindJump.pressed && Acrimony.instance.getAcrimonyClientUtil().getGroundTicks() >= 10) break;
                this.startedSprint = true;
                break;
            }
            case "Sneak": {
                if (yaw1 % 90.0f < 22.5f || yaw1 % 90.0f > 67.5f) {
                    this.diagonally = false;
                    this.requestedRotationYaw = yaw1 + 45.0f - (yaw1 + 45.0f) % 90.0f - 135.0f;
                    this.requestedRotationPitch = 79.3f;
                    this.blocksPlaced = 1;
                } else {
                    this.diagonally = true;
                    this.requestedRotationYaw = yaw1 - yaw1 % 90.0f + 45.0f - 180.0f;
                    this.requestedRotationPitch = 79.7f;
                }
                this.randomNumber = this.random.nextInt(2);
                this.randomNumber2 = this.random.nextInt(3);
                this.sneakCounter = 1000;
                break;
            }
            case "Godbridge": {
                if (yaw1 % 90.0f < 22.5f || yaw1 % 90.0f > 67.5f) {
                    this.diagonally = false;
                    this.requestedRotationYaw = yaw1 + 45.0f - (yaw1 + 45.0f) % 90.0f - 135.0f;
                    this.requestedRotationPitch = 76.0f;
                    this.blocksPlaced = 1;
                } else {
                    this.diagonally = true;
                    this.requestedRotationYaw = yaw1 - yaw1 % 90.0f + 45.0f - 180.0f;
                    this.requestedRotationPitch = 77.0f;
                }
                this.randomNumber = this.random.nextInt(2);
                this.randomNumber2 = this.random.nextInt(3);
                break;
            }
            case "Basic custom": {
                if (!(yaw1 % 90.0f < 22.5f) && !(yaw1 % 90.0f > 67.5f)) break;
                this.blocksPlaced = 1;
            }
        }
        this.renderedYaw = MovementUtil.getPlayerDirection() - 180.0f;
        this.renderedPitch = 82.0f;
    }

    @Override
    public void onDisable() {
        Scaffold.mc.gameSettings.keyBindSneak.pressed = KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindSneak);
        Scaffold.mc.gameSettings.keyBindJump.pressed = KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump);
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        if (this.changedKeybinds) {
            KeyboardUtil.resetKeybindings(Scaffold.mc.gameSettings.keyBindForward, Scaffold.mc.gameSettings.keyBindBack, Scaffold.mc.gameSettings.keyBindLeft, Scaffold.mc.gameSettings.keyBindRight);
            this.changedKeybinds = false;
        }
        this.switchToOriginalSlot();
    }

    @Listener
    public void onEvent(Event event) {
        if (this.jumpSprintMode.is("Rise/Opal")) {
            this.riseOpal(event);
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        if (Scaffold.mc.thePlayer.ticksExisted < 10) {
            this.setEnabled(false);
            return;
        }
        ++this.offGroundTicks;
        if (Scaffold.mc.thePlayer.onGround) {
            this.offGroundTicks = 0;
        }
        switch (this.mode.getMode()) {
            case "Basic": {
                this.basicScaffold();
                break;
            }
            case "Verus": {
                this.verusScaffold();
                break;
            }
            case "Basic custom": {
                this.basicCustomScaffold();
                break;
            }
            case "Hypixel": {
                this.hypixelScaffold();
                break;
            }
            case "Hypixel jump": {
                this.hypixelJumpScaffold();
                break;
            }
            case "Hypixel2": {
                this.hypixel2();
                break;
            }
            case "Andromeda": {
                this.andromedaScaffold();
                break;
            }
            case "Godbridge": {
                this.godbridgeScaffold();
                break;
            }
            case "Sneak": {
                this.sneakScaffold();
            }
        }
        this.pickBlock();
    }

    private void pickBlock() {
        switch (this.blockPicker.getMode()) {
            case "Switch": 
            case "Spoof": {
                Scaffold.mc.thePlayer.inventory.currentItem = this.getBlockSlot();
            }
        }
    }

    private void switchToOriginalSlot() {
        switch (this.blockPicker.getMode()) {
            case "Spoof": {
                Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
            }
            case "Switch": {
                Scaffold.mc.thePlayer.inventory.currentItem = this.oldSlot;
                Scaffold.mc.playerController.syncCurrentPlayItem();
                this.changedSlot = false;
            }
        }
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        switch (this.mode.getMode()) {
            case "Andromeda": {
                if (this.jumpTick) break;
                Scaffold.mc.thePlayer.setSprinting(false);
            }
        }
    }

    private void basicScaffold() {
        if (this.noSprint.is("Enabled")) {
            Scaffold.mc.thePlayer.setSprinting(false);
            Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        }
        switch (this.jumpMode.getMode()) {
            case "None": {
                this.placeY = Scaffold.mc.thePlayer.posY;
                break;
            }
            case "Normal": 
            case "Place when falling": {
                if (Scaffold.mc.thePlayer.onGround || Scaffold.mc.gameSettings.keyBindJump.pressed) {
                    this.placeY = Scaffold.mc.thePlayer.posY;
                }
                if (!Scaffold.mc.thePlayer.onGround || Scaffold.mc.gameSettings.keyBindJump.pressed || !MovementUtil.isMoving()) break;
                Scaffold.mc.thePlayer.jump();
            }
        }
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        if (this.expand.getValue() > 0 && MovementUtil.isMoving() && !Scaffold.mc.gameSettings.keyBindJump.pressed) {
            double x = Scaffold.mc.thePlayer.posX;
            double z = Scaffold.mc.thePlayer.posZ;
            for (int i = 0; i <= this.expand.getValue(); ++i) {
                if (i > 0) {
                    float direction = (float)Math.toRadians(MovementUtil.getPlayerDirection());
                    x -= Math.sin(direction);
                    z += Math.cos(direction);
                }
                if (WorldUtil.isAirOrLiquid(pos = new BlockPos(x, this.placeY - 1.0, z))) break;
            }
        }
        this.info = WorldUtil.getBlockInfo(pos, 4);
        this.overAir = WorldUtil.isAirOrLiquid(pos);
        this.rotationVec3 = null;
        boolean allowPlacing = this.jumpMode.is("Place when falling") ? Scaffold.mc.thePlayer.onGround || Scaffold.mc.thePlayer.motionY < 0.0 : true;
        Vec3 placeVec3 = null;
        if (this.overAir && this.info != null && allowPlacing) {
            Scaffold.mc.rightClickDelayTimer = 0;
            Scaffold.mc.gameSettings.keyBindUseItem.pressed = true;
            Vec3 vec3 = placeVec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
            this.placeBlock(vec3);
            this.placing = true;
        } else {
            this.placing = false;
        }
        switch (this.basicRotations.getMode()) {
            case "Movement based": {
                if (!this.startedRotating) {
                    this.rotations.updateRotations(MovementUtil.getPlayerDirection() - 180.0f, 82.0f);
                    this.startedRotating = true;
                } else if (MovementUtil.isMoving()) {
                    this.rotations.updateRotations(MovementUtil.getPlayerDirection() - 180.0f, this.rotations.getPitch());
                }
                if (!this.placing || placeVec3 == null) break;
                Vec3 hitVec = placeVec3;
                float[] rots = RotationsUtil.getRotationsToPosition(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
                this.rotations.updateRotations(this.rotations.getYaw(), rots[1]);
                break;
            }
            case "Block center": {
                if (!this.placing || placeVec3 == null) break;
                Vec3 hitVec = placeVec3;
                float[] rots = RotationsUtil.getRotationsToPosition(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
                this.rotations.updateRotations(rots[0], rots[1]);
            }
        }
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        Scaffold.mc.objectMouseOver = null;
    }

    private void verusScaffold() {
        if (!this.jump.isEnabled() || Scaffold.mc.thePlayer.onGround || Scaffold.mc.gameSettings.keyBindJump.pressed) {
            this.placeY = Scaffold.mc.thePlayer.posY;
        }
        if (this.jump.isEnabled() && Scaffold.mc.thePlayer.onGround && !Scaffold.mc.gameSettings.keyBindJump.pressed) {
            Scaffold.mc.thePlayer.jump();
        }
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        this.info = WorldUtil.getBlockInfo(pos, 4);
        if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
            this.placeBlock(WorldUtil.getVec3(this.info.pos, this.info.facing, true));
            this.placing = true;
        } else {
            this.placing = false;
        }
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        Scaffold.mc.objectMouseOver = null;
    }

    private void hypixelScaffold() {
        boolean allowSprint = false;
        switch (this.hypixelSprint.getMode()) {
            case "None": {
                allowSprint = Scaffold.mc.gameSettings.keyBindJump.pressed && this.hypixelTowerAllowSprint();
                break;
            }
            case "Semi": 
            case "Full": {
                boolean bl = allowSprint = !Scaffold.mc.gameSettings.keyBindJump.pressed || this.hypixelTowerAllowSprint();
            }
        }
        if (!allowSprint) {
            Scaffold.mc.thePlayer.setSprinting(false);
            Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        }
        if (!this.hypixelSprint.is("Semi") || this.startedSprint) {
            this.placeY = Scaffold.mc.thePlayer.posY;
        }
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        this.info = WorldUtil.getBlockInfo(pos, 3);
        this.overAir = WorldUtil.isAirOrLiquid(pos);
        this.rotationVec3 = null;
        ++this.ticks;
        float yaw = MovementUtil.getPlayerDirection() - 180.0f;
        float pitch = (float)(this.info != null && this.overAir ? (double)this.getPitch(yaw) : (!this.hypixelSprint.is("None") && Scaffold.mc.thePlayer.onGround ? (this.ticks < 2 ? 58.0 - Math.random() : 80.0 + Math.random()) : (double)this.rotations.getPitch()));
        this.renderedYaw = yaw;
        if (this.info != null && this.overAir) {
            this.renderedPitch = pitch;
        }
        this.rotations.updateRotations(yaw, pitch);
        if (this.overAir && this.info != null) {
            Vec3 vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
            if (this.rotationVec3 != null) {
                vec3 = this.rotationVec3;
            } else {
                MovingObjectPosition raytrace = WorldUtil.raytrace(this.rotations.getYaw(), this.rotations.getPitch());
                if (raytrace != null && raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.getBlockPos().equals(this.info.pos)) {
                    boolean horizontalFace;
                    boolean sameFace = raytrace.sideHit == this.info.facing;
                    boolean bl = horizontalFace = raytrace.sideHit != EnumFacing.UP && raytrace.sideHit != EnumFacing.DOWN;
                    if (sameFace || horizontalFace && !this.towering) {
                        if (!sameFace) {
                            BlockPos oldPos = this.info.pos;
                            this.info = new BlockInfo(oldPos, raytrace.sideHit);
                        }
                        vec3 = raytrace.hitVec;
                    }
                }
            }
            if (this.placeBlock(vec3)) {
                this.ticks = 0;
            }
            this.placing = true;
        } else {
            if (Scaffold.mc.thePlayer.ticksExisted % 2 == 0) {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
            }
            this.placing = false;
        }
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        Scaffold.mc.objectMouseOver = null;
    }

    private Vec3 getNextTickPos() {
        float f4 = 0.91f;
        if (Scaffold.mc.thePlayer.onGround) {
            f4 = Scaffold.mc.theWorld.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posX), (int)(MathHelper.floor_double((double)Scaffold.mc.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
        }
        float f = 0.16277136f / (f4 * f4 * f4);
        float f5 = Scaffold.mc.thePlayer.onGround ? Scaffold.mc.thePlayer.getAIMoveSpeed() * f : Scaffold.mc.thePlayer.jumpMovementFactor;
        double[] xzMotion = this.moveFlying(Scaffold.mc.thePlayer.moveStrafing, Scaffold.mc.thePlayer.moveForward, f5, Scaffold.mc.thePlayer.rotationYaw);
        double posX = Scaffold.mc.thePlayer.posX + xzMotion[0];
        double posY = Scaffold.mc.thePlayer.posY;
        double posZ = Scaffold.mc.thePlayer.posZ + xzMotion[1];
        return new Vec3(posX, posY - 1.0, posZ);
    }

    private double[] moveFlying(float strafe, float forward, float friction, float yaw) {
        float f = strafe * strafe + forward * forward;
        double motionX = Scaffold.mc.thePlayer.motionX;
        double motionZ = Scaffold.mc.thePlayer.motionZ;
        if (f >= 1.0E-4f) {
            if ((f = MathHelper.sqrt_float(f)) < 1.0f) {
                f = 1.0f;
            }
            f = friction / f;
            float f1 = MathHelper.sin(yaw * (float)Math.PI / 180.0f);
            float f2 = MathHelper.cos(yaw * (float)Math.PI / 180.0f);
            motionX += (double)((strafe *= f) * f2 - (forward *= f) * f1);
            motionZ += (double)(forward * f2 + strafe * f1);
        }
        return new double[]{motionX, motionZ};
    }

    private void hypixel2() {
        if (Scaffold.mc.thePlayer.onGround) {
            Scaffold.mc.thePlayer.setSprinting(false);
        } else {
            Scaffold.mc.thePlayer.setSprinting(true);
        }
        if (!Scaffold.mc.gameSettings.keyBindJump.pressed && Scaffold.mc.thePlayer.onGround && this.ticks == 0) {
            Scaffold.mc.thePlayer.jump();
            this.ticks = 11;
        } else if (this.ticks > 0) {
            --this.ticks;
        }
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        if (Scaffold.mc.thePlayer.posY - this.placeY > 1.25 && Scaffold.mc.thePlayer.onGround) {
            this.placeY = Scaffold.mc.thePlayer.posY;
        }
        if (!Scaffold.mc.gameSettings.keyBindJump.isKeyDown()) {
            pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        }
        if (Scaffold.mc.gameSettings.keyBindJump.isKeyDown()) {
            pos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.posY - 1.1, Scaffold.mc.thePlayer.posZ);
        }
        if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
            Vec3 vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
            float[] rots = RotationsUtil.getRotationsToPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
            this.rotations.updateRotations(rots[0], rots[1]);
            this.placeBlock(vec3);
            this.placing = true;
        } else {
            this.placing = false;
        }
    }

    private void hypixelJumpScaffold2() {
    }

    private void hypixelJumpScaffold() {
        if (this.jumpSprintMode.is("Novoline")) {
            if (Scaffold.mc.thePlayer.onGround || KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump)) {
                this.placeY = Scaffold.mc.thePlayer.posY;
                Scaffold.mc.gameSettings.keyBindJump.pressed = true;
            } else if (!KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump)) {
                Scaffold.mc.gameSettings.keyBindJump.pressed = false;
            }
            BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
            this.info = WorldUtil.getBlockInfo(pos, 3);
            this.overAir = WorldUtil.isAirOrLiquid(pos);
            if (!this.overAir) {
                pos = new BlockPos(Scaffold.mc.thePlayer.posX - Scaffold.mc.thePlayer.motionX * 4.0, this.placeY, Scaffold.mc.thePlayer.posZ - Scaffold.mc.thePlayer.motionZ * 4.0);
                this.info = WorldUtil.getBlockInfo(pos, 3);
                this.overAir = WorldUtil.isAirOrLiquid(pos);
                this.placing = true;
            }
            if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
                Vec3 vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
                float[] rots = RotationsUtil.getRotationsToPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                this.rotations.updateRotations(rots[0], rots[1]);
                this.placeBlock(vec3);
                this.placing = true;
            } else {
                this.placing = false;
            }
            Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
            Scaffold.mc.objectMouseOver = null;
        } else if (this.jumpSprintMode.is("Rise/Opal")) {
            if (Scaffold.mc.thePlayer.onGround || KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump)) {
                this.placeY = Scaffold.mc.thePlayer.posY;
                LogUtil.addChatMessage("This Mode is still on working progress");
                Scaffold.mc.gameSettings.keyBindJump.pressed = true;
            } else if (!KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump)) {
                Scaffold.mc.gameSettings.keyBindJump.pressed = false;
            }
            BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
            this.info = WorldUtil.getBlockInfo(pos, 3);
            this.overAir = WorldUtil.isAirOrLiquid(pos);
            if (!this.overAir && this.sprintTicks % 3 != 0) {
                pos = new BlockPos(Scaffold.mc.thePlayer.posX - Scaffold.mc.thePlayer.motionX * 2.0, this.placeY, Scaffold.mc.thePlayer.posZ - Scaffold.mc.thePlayer.motionZ * 2.0);
                this.info = WorldUtil.getBlockInfo(pos, 3);
                this.overAir = WorldUtil.isAirOrLiquid(pos);
                this.placing = true;
            }
            if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
                Vec3 vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
                float[] rots = RotationsUtil.getRotationsToPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                this.rotations.updateRotations(rots[0], rots[1]);
                this.placeBlock(vec3);
                this.placing = true;
            } else {
                this.placing = false;
            }
            Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
            Scaffold.mc.objectMouseOver = null;
        }
    }

    private void riseOpal(Event event) {
        if (event instanceof UpdateEvent) {
            if (Scaffold.mc.thePlayer.onGround) {
                Scaffold.mc.thePlayer.setSprinting(false);
            } else {
                Scaffold.mc.thePlayer.setSprinting(true);
            }
        }
        if (event instanceof MotionEvent) {
            MotionEvent e = (MotionEvent)event;
            if (!Scaffold.mc.gameSettings.keyBindJump.pressed && Scaffold.mc.thePlayer.onGround && this.ticks == 0) {
                Scaffold.mc.thePlayer.jump();
                this.ticks = 11;
            } else if (this.ticks > 0) {
                --this.ticks;
            }
            BlockPos pos = null;
            if (Scaffold.mc.thePlayer.posY - this.placeY > 1.25 && Scaffold.mc.thePlayer.onGround) {
                this.placeY = Scaffold.mc.thePlayer.posY;
            }
            if (!Scaffold.mc.gameSettings.keyBindJump.isKeyDown()) {
                pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
            }
            if (Scaffold.mc.gameSettings.keyBindJump.isKeyDown()) {
                pos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.posY - 1.1, Scaffold.mc.thePlayer.posZ);
            }
            if (Scaffold.mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir) {
                this.info = WorldUtil.getBlockInfo(pos, 3);
            }
            if (Scaffold.mc.thePlayer.motionY > -0.16 && Scaffold.mc.thePlayer.motionY < 0.17 && this.sneakCounter >= 4) {
                this.info = WorldUtil.getBlockInfo(pos, 3);
            }
            if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
                Vec3 vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
                float[] rots = RotationsUtil.getRotationsToPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                this.rotations.updateRotations(rots[0], rots[1]);
                this.placeBlock(vec3);
                this.placing = true;
            } else {
                this.placing = false;
            }
            Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
            Scaffold.mc.objectMouseOver = null;
            LogUtil.addChatMessage(((MotionEvent)event).getPitch() + " " + ((MotionEvent)event).getYaw());
        }
    }

    private boolean hypixelTowerAllowSprint() {
        switch (this.hypixelTower.getMode()) {
            case "None": {
                return false;
            }
            case "Faster vertically": {
                return false;
            }
            case "Faster horizontally": {
                return !MovementUtil.isGoingDiagonally(0.12);
            }
            case "Legit": {
                return false;
            }
        }
        return false;
    }

    private void godbridgeScaffold() {
        Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        Scaffold.mc.thePlayer.setSprinting(false);
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.posY - 1.0, Scaffold.mc.thePlayer.posZ);
        this.info = WorldUtil.getBlockInfo(pos, 3);
        this.overAir = WorldUtil.isAirOrLiquid(pos);
        this.placing = false;
        float yaw1 = MathHelper.wrapAngleTo180_float(Scaffold.mc.thePlayer.rotationYaw) + 720.0f;
        if (yaw1 % 90.0f < 22.5f || yaw1 % 90.0f > 67.5f) {
            this.diagonally = false;
            this.requestedRotationYaw = yaw1 + 45.0f - (yaw1 + 45.0f) % 90.0f - 135.0f;
            this.requestedRotationPitch = 76.0f;
        } else {
            this.diagonally = true;
            this.requestedRotationYaw = yaw1 - yaw1 % 90.0f + 45.0f - 180.0f;
            this.requestedRotationPitch = 77.0f;
        }
        this.updateRotations(new float[]{this.requestedRotationYaw, this.requestedRotationPitch}, false, "Acceleration", 8.0, 30.0, 1.75, 3.25, false, 8.0, 30.0, "Randomised", 8.0, 14.0, 0.0, 0.0, false, 8.0, 14.0, 1.0, 1.0, 0.0);
        float yawDiff = Math.abs(MathHelper.wrapAngleTo180_float(this.rotations.getYaw()) - MathHelper.wrapAngleTo180_float(this.requestedRotationYaw));
        if (yawDiff > 180.0f) {
            yawDiff = 360.0f - yawDiff;
        }
        float pitchDiff = Math.abs(this.rotations.getPitch() - this.requestedRotationPitch);
        boolean finishedRotating = yawDiff < 1.0f && pitchDiff < 1.0f;
        ++this.sneakCounter;
        if (this.info != null && this.overAir) {
            MovingObjectPosition raytrace = WorldUtil.raytrace(this.rotations.getYaw(), this.rotations.getPitch());
            Vec3 vec3 = null;
            Vec3 nextTickPos = this.getNextTickPos();
            if (raytrace != null && raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.getBlockPos().equals(this.info.pos) && raytrace.sideHit == this.info.facing) {
                vec3 = raytrace.hitVec;
            } else if (WorldUtil.negativeExpand(0.299)) {
                if (this.debugOnRaytraceFail.isEnabled()) {
                    LogUtil.addChatMessage("Raytrace fail : " + Scaffold.mc.thePlayer.ticksExisted + " | Diffs : " + (double)Math.round(yawDiff * 1000.0f) / 1000.0 + " " + (double)Math.round(pitchDiff * 1000.0f) / 1000.0);
                }
                if (finishedRotating && this.stillPlaceOnRaytraceFail.isEnabled()) {
                    vec3 = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
                }
            }
            if (vec3 != null && this.placeBlock(vec3)) {
                this.placing = true;
                this.rightClickCounter = 0;
                this.started = true;
            }
        }
        if (!this.placing && this.started) {
            boolean rightClick = false;
            switch (this.extraRightClicks.getMode()) {
                case "Disabled": {
                    rightClick = false;
                    break;
                }
                case "Normal": {
                    rightClick = this.rightClickCounter % 2 == 0 || Math.random() < 0.2;
                    break;
                }
                case "Dragclick": {
                    rightClick = this.rightClickCounter == 0 || this.rightClickCounter >= 3;
                    break;
                }
                case "Always": {
                    rightClick = true;
                }
            }
            if (rightClick) {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
            }
            ++this.rightClickCounter;
        }
        boolean pressSneak = false;
        if (Scaffold.mc.thePlayer.onGround) {
            pressSneak = Acrimony.instance.getAcrimonyClientUtil().getGroundTicks() <= 2 || this.overAir || this.sneakCounter <= 1;
        } else {
            boolean bl = pressSneak = Scaffold.mc.thePlayer.motionY < 0.0;
        }
        if (this.diagonally) {
            this.blocksPlaced = 1;
        } else if (this.blocksPlaced >= 9) {
            Scaffold.mc.gameSettings.keyBindJump.pressed = true;
            this.blocksPlaced = 0;
        } else {
            KeyboardUtil.resetKeybinding(Scaffold.mc.gameSettings.keyBindJump);
        }
        this.lastDiagonal = this.diagonally;
    }

    private void sneakScaffold() {
        MovingObjectPosition raytrace;
        Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        Scaffold.mc.thePlayer.setSprinting(false);
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.posY - 1.0, Scaffold.mc.thePlayer.posZ);
        this.info = WorldUtil.getBlockInfo(pos, 3);
        this.overAir = WorldUtil.isAirOrLiquid(pos);
        this.placing = false;
        float yaw1 = MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection()) + 720.0f;
        if (yaw1 % 90.0f < 22.5f || yaw1 % 90.0f > 67.5f) {
            this.diagonally = false;
            this.requestedRotationYaw = yaw1 + 45.0f - (yaw1 + 45.0f) % 90.0f - 135.0f;
        } else {
            this.diagonally = true;
            this.requestedRotationYaw = yaw1 - yaw1 % 90.0f + 45.0f - 180.0f;
        }
        this.rotationVec3 = null;
        if (Scaffold.mc.thePlayer.onGround && !Scaffold.mc.gameSettings.keyBindJump.pressed) {
            this.requestedRotationPitch = 79.8f;
            raytrace = WorldUtil.raytrace(this.rotations.getYaw(), this.requestedRotationPitch);
            if (raytrace != null && raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.getBlockPos().equals(this.info.pos)) {
                boolean horizontalFace;
                boolean sameFace = raytrace.sideHit == this.info.facing;
                boolean bl = horizontalFace = raytrace.sideHit != EnumFacing.UP && raytrace.sideHit != EnumFacing.DOWN;
                if (sameFace || horizontalFace) {
                    if (!sameFace) {
                        BlockPos oldPos = this.info.pos;
                        this.info = new BlockInfo(oldPos, raytrace.sideHit);
                    }
                    this.rotationVec3 = raytrace.hitVec;
                }
            }
        } else if (this.info != null && this.overAir) {
            this.requestedRotationPitch = this.getPitch(this.requestedRotationYaw);
        }
        this.updateRotations(new float[]{this.requestedRotationYaw, this.requestedRotationPitch}, false, "Acceleration", 8.0, 30.0, 1.75, 3.25, false, 8.0, 30.0, "Randomised", 12.0, 16.0, 0.0, 0.0, false, 12.0, 16.0, 1.0, 1.0, 0.0);
        ++this.sneakCounter;
        if (this.info != null) {
            if (this.rotationVec3 != null) {
                if (this.placeBlock(this.rotationVec3)) {
                    this.placing = true;
                    this.sneakCounter = 0;
                    this.started = true;
                }
            } else {
                raytrace = WorldUtil.raytrace(this.rotations.getYaw(), this.rotations.getPitch());
                if (raytrace != null && raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.getBlockPos().equals(this.info.pos) && raytrace.sideHit == this.info.facing && this.placeBlock(raytrace.hitVec)) {
                    this.placing = true;
                    this.sneakCounter = 0;
                    this.started = true;
                }
            }
        }
        if (!this.placing && this.started) {
            boolean rightClick = false;
            switch (this.extraRightClicks.getMode()) {
                case "Disabled": {
                    rightClick = false;
                    break;
                }
                case "Normal": {
                    rightClick = this.rightClickCounter % 2 == 0 || Math.random() < 0.2;
                    break;
                }
                case "Dragclick": {
                    boolean bl = rightClick = this.rightClickCounter == 0 || this.rightClickCounter >= 3;
                    if (this.rightClickCounter == 4 && Math.random() > 0.2) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
                        break;
                    }
                    if (this.rightClickCounter != 0 || !(Math.random() > 0.3)) break;
                    PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
                    break;
                }
                case "Always": {
                    rightClick = true;
                }
            }
            if (rightClick) {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
            }
            ++this.rightClickCounter;
        }
        boolean pressSneak = false;
        pressSneak = Scaffold.mc.thePlayer.onGround ? Acrimony.instance.getAcrimonyClientUtil().getGroundTicks() <= 2 || this.overAir || this.sneakCounter <= 1 : Scaffold.mc.thePlayer.motionY < 0.0;
        Scaffold.mc.gameSettings.keyBindSneak.pressed = pressSneak;
        this.lastDiagonal = this.diagonally;
    }

    private float getPitch(float yaw) {
        if (this.info == null) {
            return 80.0f;
        }
        FixedRotations testRotations = new FixedRotations(this.rotations.getYaw(), this.rotations.getPitch());
        ArrayList<Float> pitchValues = new ArrayList<Float>();
        pitchValues.add(Float.valueOf(this.rotations.getPitch()));
        for (float testPitch = 90.0f; testPitch >= 45.0f; testPitch -= testPitch > 70.0f ? 0.15f : 1.0f) {
            pitchValues.add(Float.valueOf(testPitch));
        }
        Iterator iterator = pitchValues.iterator();
        while (iterator.hasNext()) {
            boolean horizontalFace;
            float testPitch = ((Float)iterator.next()).floatValue();
            testRotations.updateRotations(yaw, testPitch);
            MovingObjectPosition raytrace = WorldUtil.raytrace(testRotations.getYaw(), testRotations.getPitch());
            if (raytrace == null || raytrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !raytrace.getBlockPos().equals(this.info.pos)) continue;
            boolean sameFace = raytrace.sideHit == this.info.facing;
            boolean bl = horizontalFace = raytrace.sideHit != EnumFacing.UP && raytrace.sideHit != EnumFacing.DOWN;
            if (!sameFace && (!horizontalFace || this.towering)) continue;
            if (!sameFace) {
                BlockPos oldPos = this.info.pos;
                this.info = new BlockInfo(oldPos, raytrace.sideHit);
            }
            this.rotationVec3 = raytrace.hitVec;
            return testPitch;
        }
        return 80.0f;
    }

    private void andromedaScaffold() {
        this.jumpTick = false;
        if (Scaffold.mc.thePlayer.onGround) {
            this.placeY = Scaffold.mc.thePlayer.posY;
            if (MovementUtil.isMoving()) {
                Scaffold.mc.thePlayer.jumpNoEvent();
                MovementUtil.strafe(0.21);
                this.jumpTick = true;
            }
        }
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        if (!this.jumpTick || !this.noPlaceOnJumpTick.isEnabled()) {
            this.info = WorldUtil.getBlockInfo(pos, 3);
            this.overAir = WorldUtil.isAirOrLiquid(pos);
            if (!this.overAir) {
                pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY + 2.0, Scaffold.mc.thePlayer.posZ);
                this.info = WorldUtil.getBlockInfo(pos, 3);
                this.overAir = WorldUtil.isAirOrLiquid(pos);
            }
        }
        Vec3 vec3 = null;
        if (WorldUtil.isAirOrLiquid(pos) && this.info != null) {
            vec3 = WorldUtil.getVec3(this.info.pos, this.info.facing, false);
            this.placeBlock(vec3);
            this.placing = true;
        } else {
            this.placing = false;
        }
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        Scaffold.mc.objectMouseOver = null;
        if (this.placing) {
            float[] rots = RotationsUtil.getRotationsToPosition(vec3.xCoord, vec3.yCoord, vec3.zCoord);
            this.requestedRotationYaw = rots[0];
            this.requestedRotationPitch = rots[1];
        } else {
            this.requestedRotationYaw = Scaffold.mc.thePlayer.rotationYaw;
            this.requestedRotationPitch = Scaffold.mc.thePlayer.rotationPitch;
        }
        if (this.placing && this.rotationsEnabled.isEnabled() && this.moveFix.isEnabled()) {
            Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
            Scaffold.mc.thePlayer.setSprinting(false);
        }
        this.rotations.updateRotations(this.requestedRotationYaw, this.requestedRotationPitch);
    }

    private void basicCustomScaffold() {
        MovingObjectPosition raytrace;
        BlockPos pos = new BlockPos(Scaffold.mc.thePlayer.posX, this.placeY - 1.0, Scaffold.mc.thePlayer.posZ);
        this.info = WorldUtil.getBlockInfo(pos, this.range.getValue());
        this.overAir = WorldUtil.isAirOrLiquid(pos);
        boolean jumped = false;
        float yaw1 = MathHelper.wrapAngleTo180_float(Scaffold.mc.thePlayer.rotationYaw) + 720.0f;
        switch (this.customJumpMode.getMode()) {
            case "Godbridge": {
                if (this.blocksPlaced >= 9) {
                    Scaffold.mc.gameSettings.keyBindJump.pressed = true;
                    Scaffold.mc.thePlayer.jumpTicks = 0;
                    jumped = true;
                    this.blocksPlaced = 0;
                }
                if (yaw1 % 90.0f > 22.5f && yaw1 % 90.0f < 67.5f) {
                    this.blocksPlaced = 1;
                }
            }
            case "None": {
                this.placeY = Scaffold.mc.thePlayer.posY;
                break;
            }
            case "Normal": 
            case "Place when falling": {
                if (Scaffold.mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    Scaffold.mc.gameSettings.keyBindJump.pressed = true;
                    Scaffold.mc.thePlayer.jumpTicks = 0;
                    jumped = true;
                }
                if (!Scaffold.mc.thePlayer.onGround && !Scaffold.mc.gameSettings.keyBindJump.pressed) break;
                this.placeY = Scaffold.mc.thePlayer.posY;
            }
        }
        if (!jumped) {
            KeyboardUtil.resetKeybinding(Scaffold.mc.gameSettings.keyBindJump);
        }
        if (this.shouldNotSprint() && this.customNoSprintMode.is("Enabled") || this.noMoveOnStartCounter < this.customNoMoveTicksOnStart.getValue()) {
            Scaffold.mc.thePlayer.setSprinting(false);
            Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        }
        ++this.sneakCounter;
        boolean hadToSneak = false;
        if (this.shouldSneak(true) && this.customSneakMode.is("Enabled")) {
            Scaffold.mc.gameSettings.keyBindSneak.pressed = true;
            hadToSneak = true;
        } else {
            Scaffold.mc.gameSettings.keyBindSneak.pressed = KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindSneak);
        }
        boolean allowPlacing = true;
        if (this.customJumpMode.is("Place when falling") && !Scaffold.mc.thePlayer.onGround && Scaffold.mc.thePlayer.motionY >= 0.0) {
            allowPlacing = false;
        }
        ++this.placeDelay;
        if (this.placeDelay < this.nextPlaceDelay && (this.applyPlaceDelayOffground.isEnabled() || Scaffold.mc.thePlayer.onGround)) {
            allowPlacing = false;
        }
        if (!WorldUtil.negativeExpand(this.getMinDistFromBlock())) {
            allowPlacing = false;
        }
        if (this.shouldRotate()) {
            this.updateRotations(this.getBasicCustomRotations(this.overAir && this.info != null && allowPlacing), this.instantRotations.isEnabled(), this.yawSpeedMode.getMode(), this.minYawSpeed.getValue(), this.maxYawSpeed.getValue(), this.minYawAccel.getValue(), this.maxYawAccel.getValue(), this.reduceYawSpeedWhenAlmostDone.isEnabled(), this.minYawSpeedWhenAlmostDone.getValue(), this.maxYawSpeedWhenAlmostDone.getValue(), this.pitchSpeedMode.getMode(), this.minPitchSpeed.getValue(), this.maxPitchSpeed.getValue(), this.minPitchAccel.getValue(), this.maxPitchAccel.getValue(), this.reducePitchSpeedWhenAlmostDone.isEnabled(), this.minPitchSpeedWhenAlmostDone.getValue(), this.maxPitchSpeedWhenAlmostDone.getValue(), this.minYawChange.getValue(), this.minPitchChange.getValue(), this.rotsRandomisation.getValue());
        } else if (!this.bCustomRotationsTiming.is("Never") && this.bCustomResetRotsIfNotRotating.isEnabled()) {
            this.rotations.updateRotations(Scaffold.mc.thePlayer.rotationYaw, Scaffold.mc.thePlayer.rotationPitch);
        }
        Scaffold.mc.rightClickDelayTimer = 0;
        Vec3 raytraceVec3 = null;
        if (this.overAir && this.info != null && allowPlacing && (raytrace = WorldUtil.raytrace(this.rotations.getYaw(), this.rotations.getPitch())) != null && raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.getBlockPos().equals(this.info.pos) && raytrace.sideHit == this.info.facing) {
            raytraceVec3 = raytrace.hitVec;
        }
        if (raytraceVec3 == null && this.bCustomOnlyPlaceIfRaytraceSuccess.isEnabled()) {
            allowPlacing = false;
        }
        if (this.overAir && this.info != null && allowPlacing) {
            Vec3 vec3 = this.rotationVec3 != null && this.yawDone && this.pitchDone ? this.rotationVec3 : (raytraceVec3 != null ? raytraceVec3 : WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch()));
            this.placeBlock(vec3);
            this.nextPlaceDelay = this.getNextPlaceDelay();
            this.placeDelay = 0;
            this.rightClickCounter = 0;
            switch (this.info.facing) {
                case NORTH: {
                    this.facingYaw = 0.0f;
                    this.facingPitch = (float)(Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0 : this.bCustomPitchValue.getValue());
                    break;
                }
                case SOUTH: {
                    this.facingYaw = 180.0f;
                    this.facingPitch = (float)(Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0 : this.bCustomPitchValue.getValue());
                    break;
                }
                case EAST: {
                    this.facingYaw = 90.0f;
                    this.facingPitch = (float)(Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0 : this.bCustomPitchValue.getValue());
                    break;
                }
                case WEST: {
                    this.facingYaw = -90.0f;
                    this.facingPitch = (float)(Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0 : this.bCustomPitchValue.getValue());
                    break;
                }
                case UP: {
                    this.facingPitch = 90.0f;
                    break;
                }
                case DOWN: {
                    this.facingPitch = -90.0f;
                }
            }
            this.placing = true;
            ++this.sneakPlacementCounter;
        } else {
            boolean rightClick = false;
            switch (this.extraRightClicks.getMode()) {
                case "Disabled": {
                    rightClick = false;
                    break;
                }
                case "Normal": {
                    rightClick = this.rightClickCounter % 2 == 0 || Math.random() < 0.2;
                    break;
                }
                case "Dragclick": {
                    rightClick = this.rightClickCounter == 0 || this.rightClickCounter >= 3;
                    break;
                }
                case "Always": {
                    rightClick = true;
                }
            }
            if (rightClick) {
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(Scaffold.mc.thePlayer.getHeldItem()));
            }
            ++this.rightClickCounter;
            this.placing = false;
        }
        Scaffold.mc.gameSettings.keyBindUseItem.pressed = false;
        Scaffold.mc.objectMouseOver = null;
        if (!hadToSneak && this.shouldSneak(false) && this.customSneakMode.is("Enabled")) {
            Scaffold.mc.gameSettings.keyBindSneak.pressed = true;
        }
    }

    private boolean placeBlock(Vec3 vec3) {
        boolean placed = false;
        if (this.info != null && Scaffold.mc.thePlayer.getHeldItem() != null && Scaffold.mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock && Scaffold.mc.playerController.onPlayerRightClick(Scaffold.mc.thePlayer, Scaffold.mc.theWorld, Scaffold.mc.thePlayer.getHeldItem(), this.info.pos, this.info.facing, vec3)) {
            placed = true;
            if (this.swingAnimation.isEnabled()) {
                Scaffold.mc.thePlayer.swingItem();
            } else {
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
            ++this.blocksPlaced;
        }
        return placed;
    }

    private void updateRotations(float[] rots, boolean instantRotations, String yawSpeedMode, double minYawSpeed, double maxYawSpeed, double minYawAccel, double maxYawAccel, boolean reduceYawSpeedWhenAlmostDone, double minYawSpeedWhenAlmostDone, double maxYawSpeedWhenAlmostDone, String pitchSpeedMode, double minPitchSpeed, double maxPitchSpeed, double minPitchAccel, double maxPitchAccel, boolean reducePitchSpeedWhenAlmostDone, double minPitchSpeedWhenAlmostDone, double maxPitchSpeedWhenAlmostDone, double minYawChange, double minPitchChange, double rotsRandomisation) {
        boolean pitchChange;
        boolean reducePitchSpeed;
        float requestedYaw = rots[0];
        float requestedPitch = rots[1];
        if (instantRotations) {
            this.rotations.updateRotations(requestedYaw, requestedPitch);
            return;
        }
        float yaw = this.rotations.getYaw();
        float pitch = this.rotations.getPitch();
        float aaaa = MathHelper.wrapAngleTo180_float(yaw);
        float bbbb = MathHelper.wrapAngleTo180_float(requestedYaw);
        float yawDiff = Math.abs(bbbb - aaaa);
        float pitchDiff = Math.abs(requestedPitch - pitch);
        double randomAmount = Math.random() * rotsRandomisation;
        float to180YawDiff = yawDiff > 180.0f ? 360.0f - yawDiff : yawDiff;
        boolean reduceYawSpeed = this.yawSpeed > (double)to180YawDiff && reduceYawSpeedWhenAlmostDone;
        boolean bl = reducePitchSpeed = this.pitchSpeed > (double)pitchDiff && reducePitchSpeedWhenAlmostDone;
        if (yawSpeedMode.equals("Randomised")) {
            this.yawSpeed = maxYawSpeed > minYawSpeed ? ThreadLocalRandom.current().nextDouble(minYawSpeed, maxYawSpeed) : minYawSpeed;
        } else if (yawSpeedMode.equals("Acceleration")) {
            if (reduceYawSpeed) {
                this.yawSpeed = maxYawSpeedWhenAlmostDone > minYawSpeedWhenAlmostDone ? ThreadLocalRandom.current().nextDouble(minYawSpeedWhenAlmostDone, maxYawSpeedWhenAlmostDone) : minYawSpeedWhenAlmostDone;
                this.shouldResetYawAccel = true;
            } else if (this.shouldResetYawAccel) {
                this.yawSpeed = minYawSpeed;
            } else {
                this.yawSpeed += maxYawAccel > minYawAccel ? ThreadLocalRandom.current().nextDouble(minYawAccel, maxYawAccel) : minYawAccel;
                this.yawSpeed = Math.min(this.yawSpeed, maxYawSpeed);
            }
            this.shouldResetYawAccel = false;
        }
        if (pitchSpeedMode.equals("Randomised")) {
            this.pitchSpeed = maxPitchSpeed > minPitchSpeed ? ThreadLocalRandom.current().nextDouble(minPitchSpeed, maxPitchSpeed) : minPitchSpeed;
        } else if (pitchSpeedMode.equals("Acceleration")) {
            if (reduceYawSpeed) {
                this.pitchSpeed = maxPitchSpeedWhenAlmostDone > minPitchSpeedWhenAlmostDone ? ThreadLocalRandom.current().nextDouble(minPitchSpeedWhenAlmostDone, maxPitchSpeedWhenAlmostDone) : minPitchSpeedWhenAlmostDone;
                this.shouldResetPitchAccel = true;
            } else if (this.shouldResetPitchAccel) {
                this.pitchSpeed = minPitchSpeed;
            } else {
                this.pitchSpeed += maxPitchAccel > minPitchAccel ? ThreadLocalRandom.current().nextDouble(minPitchAccel, maxPitchAccel) : minPitchAccel;
                this.pitchSpeed = Math.min(this.pitchSpeed, maxPitchSpeed);
            }
            this.shouldResetPitchAccel = false;
        }
        boolean yawChange = (double)yawDiff > minYawChange && (double)yawDiff < 360.0 - minYawChange;
        boolean bl2 = pitchChange = (double)pitchDiff > minPitchChange;
        if (yawChange) {
            this.yawDone = false;
            yaw = yawDiff > 180.0f ? (bbbb > aaaa ? (float)((double)yaw - Math.min(this.yawSpeed, (double)yawDiff)) : (float)((double)yaw + Math.min(this.yawSpeed, (double)yawDiff))) : (bbbb > aaaa ? (float)((double)yaw + Math.min(this.yawSpeed, (double)yawDiff)) : (float)((double)yaw - Math.min(this.yawSpeed, (double)yawDiff)));
            yaw = (float)((double)yaw + (Math.random() * randomAmount - randomAmount * 0.5));
        } else {
            this.yawDone = true;
            this.shouldResetYawAccel = true;
        }
        this.yawSpeed = Math.min(this.yawSpeed, (double)Math.abs(this.rotations.getYaw() - yaw));
        if (pitchChange) {
            this.pitchDone = false;
            pitch = requestedPitch > pitch ? (float)((double)pitch + Math.min(this.pitchSpeed, (double)pitchDiff)) : (float)((double)pitch - Math.min(this.pitchSpeed, (double)pitchDiff));
            if ((pitch = (float)((double)pitch + (Math.random() * randomAmount - randomAmount * 0.5))) > 88.0f) {
                pitch = 90.0f;
            } else if (pitch < -88.0f) {
                pitch = -90.0f;
            }
        } else {
            this.pitchDone = true;
            this.shouldResetPitchAccel = true;
        }
        this.rotations.updateRotations(yaw, pitch);
    }

    private float[] getBasicCustomRotations(boolean canPlace) {
        float yaw = this.rotations.getYaw();
        float pitch = this.rotations.getPitch();
        this.rotationVec3 = null;
        block8 : switch (this.bCustomRotationsMode.getMode()) {
            case "Facing": {
                yaw = this.facingYaw;
                pitch = this.facingPitch;
                break;
            }
            case "Block center": {
                if (canPlace) {
                    Vec3 hitVec;
                    this.rotationVec3 = hitVec = WorldUtil.getVec3ClosestFromRots(this.info.pos, this.info.facing, true, this.rotations.getYaw(), this.rotations.getPitch());
                    float[] rots = RotationsUtil.getRotationsToPosition(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
                    this.requestedRotationYaw = yaw = rots[0];
                    this.requestedRotationPitch = pitch = rots[1];
                    this.startedRotating = true;
                    break;
                }
                if (!this.startedRotating) {
                    this.requestedRotationYaw = yaw = MovementUtil.getPlayerDirection() - 180.0f;
                    pitch = Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0f : 82.0f;
                    this.requestedRotationPitch = pitch;
                    this.startedRotating = true;
                    break;
                }
                yaw = this.requestedRotationYaw;
                pitch = this.requestedRotationPitch;
                break;
            }
            case "Movement based": {
                if (!this.startedRotating || MovementUtil.isMoving()) {
                    this.requestedRotationYaw = yaw = MovementUtil.getPlayerDirection() - (float)this.bCustomYawOffset.getValue();
                    this.requestedRotationPitch = pitch = (float)(Scaffold.mc.gameSettings.keyBindJump.pressed ? 90.0 : this.bCustomPitchValue.getValue());
                    this.startedRotating = true;
                    break;
                }
                yaw = this.requestedRotationYaw;
                pitch = this.requestedRotationPitch;
                break;
            }
            case "Godbridge": {
                float yaw1 = MathHelper.wrapAngleTo180_float(Scaffold.mc.thePlayer.rotationYaw) + 720.0f;
                if (yaw1 % 90.0f < 22.5f || yaw1 % 90.0f > 67.5f) {
                    this.requestedRotationYaw = yaw = yaw1 + 45.0f - (yaw1 + 45.0f) % 90.0f - 135.0f;
                    pitch = 75.9f;
                    this.requestedRotationPitch = 75.9f;
                    break;
                }
                this.requestedRotationYaw = yaw = yaw1 - yaw1 % 90.0f + 45.0f - 180.0f;
                pitch = 77.0f;
                this.requestedRotationPitch = 77.0f;
                break;
            }
            case "Raytrace pitch": {
                yaw = MovementUtil.getPlayerDirection() - (float)this.bCustomYawOffset.getValue();
                if (canPlace) {
                    FixedRotations testRotations = new FixedRotations(this.rotations.getYaw(), this.rotations.getPitch());
                    for (float testPitch = 90.0f; testPitch >= 45.0f; testPitch -= pitch > 70.0f ? 0.15f : 1.0f) {
                        boolean horizontalFace;
                        testRotations.updateRotations(MovementUtil.getPlayerDirection() - (float)this.bCustomYawOffset.getValue(), testPitch);
                        MovingObjectPosition raytrace = WorldUtil.raytrace(testRotations.getYaw(), testRotations.getPitch());
                        if (raytrace == null || raytrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !raytrace.getBlockPos().equals(this.info.pos)) continue;
                        boolean sameFace = raytrace.sideHit == this.info.facing;
                        boolean bl = horizontalFace = raytrace.sideHit != EnumFacing.UP && raytrace.sideHit != EnumFacing.DOWN;
                        if (!sameFace && !horizontalFace) continue;
                        if (!sameFace) {
                            BlockPos oldPos = this.info.pos;
                            this.info = new BlockInfo(oldPos, raytrace.sideHit);
                            LogUtil.addChatMessage("Changed block face");
                        }
                        yaw = MovementUtil.getPlayerDirection() - (float)this.bCustomYawOffset.getValue();
                        pitch = testPitch;
                        this.rotationVec3 = raytrace.hitVec;
                        break block8;
                    }
                    break;
                }
                if (this.startedRotating) break;
                pitch = 80.0f;
                this.startedRotating = true;
                break;
            }
            case "Static": {
                yaw = this.startingYaw - 180.0f;
                pitch = 82.0f;
            }
        }
        return new float[]{yaw, pitch};
    }

    private boolean shouldRotate() {
        return this.bCustomRotationsTiming.is("Always") ? true : (this.bCustomRotationsTiming.is("Never") ? false : (this.bCustomRotationsTiming.is("Over air") ? this.overAir : (this.bCustomRotationsTiming.is("When placing") ? this.placing : (this.bCustomRotationsTiming.is("When not jumping") ? (this.customJumpMode.is("None") || this.customJumpMode.is("Godbridge") ? !Scaffold.mc.thePlayer.onGround || !Scaffold.mc.gameSettings.keyBindJump.pressed : !Scaffold.mc.thePlayer.onGround) : true))));
    }

    private boolean shouldNotSprint() {
        return this.customNoSprintTiming.is("Always") ? true : (this.customNoSprintTiming.is("Never") ? false : (this.customNoSprintTiming.is("Over air") ? this.overAir : (this.customNoSprintTiming.is("When placing") ? this.placing : (this.customNoSprintTiming.is("Onground") ? Scaffold.mc.thePlayer.onGround : (this.customNoSprintTiming.is("Offground") ? !Scaffold.mc.thePlayer.onGround : true)))));
    }

    private boolean shouldSneak(boolean prePlacement) {
        if (!(this.customSneakOffground.isEnabled() || Scaffold.mc.thePlayer.onGround && !Scaffold.mc.gameSettings.keyBindJump.pressed)) {
            return false;
        }
        return this.customSneakTiming.is("Always") ? true : (this.customSneakTiming.is("Never") ? false : (this.customSneakTiming.is("Over air") ? this.overAir : (this.customSneakTiming.is("Over air and place fail") ? this.overAir && !prePlacement && !this.placing : (this.customSneakTiming.is("When placing") ? this.placing : (this.customSneakTiming.is("Every x blocks") ? this.sneakPlacementCounter % this.customSneakFrequency.getValue() == 0 && this.overAir && prePlacement : (this.customSneakTiming.is("Alternate") ? this.sneakCounter % 2 == 0 : false))))));
    }

    private int getNextPlaceDelay() {
        if (this.minPlaceDelay.getValue() >= this.maxPlaceDelay.getValue()) {
            return this.minPlaceDelay.getValue();
        }
        return ThreadLocalRandom.current().nextInt(this.minPlaceDelay.getValue(), this.maxPlaceDelay.getValue());
    }

    private double getMinDistFromBlock() {
        return Scaffold.mc.thePlayer.onGround ? this.distFromBlock.getValue() : this.offGroundDistFromBlock.getValue();
    }

    private double getMotionMult() {
        return Scaffold.mc.thePlayer.onGround ? this.customMotionMultOnGround.getValue() : this.customMotionMultOffGround.getValue();
    }

    @Listener
    public void onEntityAction(EntityActionEvent event) {
        switch (this.mode.getMode()) {
            case "Basic": {
                if (!this.noSprint.is("Spoof")) break;
                event.setSprinting(false);
                break;
            }
            case "Basic custom": {
                if (this.shouldNotSprint() && this.customNoSprintMode.is("Spoof")) {
                    event.setSprinting(false);
                }
                if (!this.shouldSneak(false) || !this.customSneakMode.is("Spoof")) break;
                event.setSneaking(true);
                break;
            }
            case "Hypixel": {
                if (!Scaffold.mc.thePlayer.onGround || MovementUtil.getSpeedAmplifier() > 0) {
                    // empty if block
                }
                if (this.hypixelSprint.is("None") || !MovementUtil.isMoving() || !(MovementUtil.getHorizontalMotion() > 0.1)) break;
                break;
            }
        }
    }

    @Listener
    public void onStrafe(StrafeEvent event) {
        switch (this.mode.getMode()) {
            case "Basic custom": {
                if (this.customIgnoreSpeedPot.isEnabled() && Scaffold.mc.thePlayer.onGround && Scaffold.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float speed = (float)(Scaffold.mc.thePlayer.isSprinting() ? 0.13 : 0.1);
                    float f4 = Scaffold.mc.theWorld.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posX), (int)(MathHelper.floor_double((double)Scaffold.mc.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
                    float f = 0.16277136f / (f4 * f4 * f4);
                    float f5 = speed * f;
                    event.setAttributeSpeed(f5);
                }
                if (this.movementType.is("Fixed") && this.shouldRotate()) {
                    this.fixMovement(event);
                }
                if (this.noMoveOnStartCounter >= this.customNoMoveTicksOnStart.getValue()) break;
                event.setForward(0.0f);
                event.setStrafe(0.0f);
                break;
            }
            case "Hypixel": {
                if (!Scaffold.mc.thePlayer.onGround || !Scaffold.mc.thePlayer.isPotionActive(Potion.moveSpeed)) break;
                float speed = (float)(Scaffold.mc.thePlayer.isSprinting() ? 0.13 : 0.1);
                float f4 = Scaffold.mc.theWorld.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posX), (int)(MathHelper.floor_double((double)Scaffold.mc.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
                float f = 0.16277136f / (f4 * f4 * f4);
                float f5 = speed * f;
                event.setAttributeSpeed(f5);
                break;
            }
            case "Hypixel jump": {
                if (Scaffold.mc.thePlayer.onGround || Scaffold.mc.thePlayer.isSprinting()) {
                    // empty if block
                }
                Scaffold.mc.gameSettings.keyBindSprint.pressed = MovementUtil.isMoving();
                Scaffold.mc.thePlayer.setSprinting(MovementUtil.isMoving());
                if (!Scaffold.mc.thePlayer.onGround || !Scaffold.mc.thePlayer.isPotionActive(Potion.moveSpeed)) break;
                float speed = (float)(Scaffold.mc.thePlayer.isSprinting() ? 0.13 : 0.1);
                float f4 = Scaffold.mc.theWorld.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posX), (int)(MathHelper.floor_double((double)Scaffold.mc.thePlayer.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)Scaffold.mc.thePlayer.posZ))).getBlock().slipperiness * 0.91f;
                float f = 0.16277136f / (f4 * f4 * f4);
                float f5 = speed * f;
                event.setAttributeSpeed(f5);
                break;
            }
            case "Andromeda": {
                if (!this.rotationsEnabled.isEnabled() || !this.moveFix.isEnabled()) break;
                this.fixMovement(event);
                break;
            }
            case "Sneak": 
            case "Godbridge": {
                this.fixMovement(event);
            }
        }
    }

    private void fixMovement(StrafeEvent event) {
        float value;
        float diff = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(this.rotations.getYaw()) - MathHelper.wrapAngleTo180_float(this.getYawDirection())) + 22.5f;
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

    @Listener
    public void onJump(JumpEvent event) {
        switch (this.mode.getMode()) {
            case "Basic custom": {
                if (this.movementType.is("Fixed") && this.shouldRotate() && !this.mode.is("Raytrace custom")) {
                    event.setBoosting(false);
                } else if (!this.customAllowJumpBoost.isEnabled()) {
                    event.setBoosting(false);
                }
                if (!this.movementType.is("Normal")) break;
                event.setBoostAmount((float)this.customJumpBoostAmount.getValue());
                break;
            }
            case "Hypixel": {
                if (this.hypixelSprint.is("Semi") && this.startedSprint && this.wasHovering) {
                    event.setCancelled(true);
                }
                event.setBoostAmount(0.1f);
                break;
            }
            case "Hypixel jump": {
                if (this.towering && MovementUtil.isMoving()) {
                    event.setCancelled(true);
                }
                ++this.sprintTicks;
                if (this.sprintTicks % 2 != 0) break;
                event.setBoosting(true);
                event.setBoostAmount(0.25f);
                break;
            }
            case "Andromeda": {
                if (!this.rotationsEnabled.isEnabled() || !this.moveFix.isEnabled()) break;
                event.setYaw(this.rotations.getYaw());
            }
        }
    }

    @Listener
    public void onMove(MoveEvent event) {
        switch (this.mode.getMode()) {
            case "Basic custom": {
                if (this.movementType.is("Strafe")) {
                    if (this.noMoveOnStartCounter < this.customNoMoveTicksOnStart.getValue()) {
                        MovementUtil.strafe(event, 0.0);
                    } else if (Scaffold.mc.thePlayer.onGround) {
                        MovementUtil.strafe(event, this.customOnGroundSpeed.getValue() + (double)MovementUtil.getSpeedAmplifier() * this.customOnGroundPotionExtra.getValue() - this.randomAmount());
                    } else if (this.customOffGroundStrafe.isEnabled()) {
                        MovementUtil.strafe(event, this.customOffGroundSpeed.getValue() + (double)MovementUtil.getSpeedAmplifier() * this.customOffGroundPotionExtra.getValue() - this.randomAmount());
                    }
                } else if (this.movementType.is("Normal")) {
                    double mult;
                    double d = mult = this.customMultAffectsNextMotion.isEnabled() && !Scaffold.mc.thePlayer.onGround ? Math.min(this.getMotionMult(), 1.1) : this.getMotionMult();
                    if (this.customMultAffectsNextMotion.isEnabled()) {
                        Scaffold.mc.thePlayer.motionX = event.getX() * mult;
                        event.setX(Scaffold.mc.thePlayer.motionX);
                        Scaffold.mc.thePlayer.motionZ = event.getZ() * mult;
                        event.setZ(Scaffold.mc.thePlayer.motionZ);
                    } else {
                        event.setX(event.getX() * mult);
                        event.setZ(event.getZ() * mult);
                    }
                }
                ++this.noMoveOnStartCounter;
                break;
            }
            case "Hypixel": {
                boolean allowTower = true;
                switch (this.hypixelSprint.getMode()) {
                    case "Semi": {
                        if (!this.startedSprint) {
                            MovementUtil.strafe(event, 0.185 - this.randomAmount());
                            if (Scaffold.mc.thePlayer.onGround) {
                                if (this.sprintTicks == 0) {
                                    MovementUtil.jump(event);
                                    ++this.sprintTicks;
                                } else {
                                    this.startedSprint = true;
                                    this.sprintTicks = 0;
                                }
                            }
                            allowTower = false;
                            break;
                        }
                        boolean diagonal = MovementUtil.isGoingDiagonally(0.12);
                        if (this.towering != this.wasTowering) {
                            if (!this.towering && this.toweringTicks > 4) {
                                this.pendingMovementStop = true;
                            }
                            this.toweringTicks = 0;
                        }
                        if (Scaffold.mc.thePlayer.onGround && this.pendingMovementStop) {
                            MovementUtil.strafe(event, 0.0);
                            this.pendingMovementStop = false;
                        } else if (this.towering) {
                            if (!this.hypixelTowerAllowSprint() || diagonal || !Scaffold.mc.thePlayer.onGround) {
                                // empty if block
                            }
                            this.sprintTicks = 0;
                        } else {
                            ++this.sprintTicks;
                            double speed = 0.26999 - Math.random() * 1.0E-5;
                            if (Scaffold.mc.thePlayer.onGround) {
                                MovementUtil.strafe(event, speed - 0.15);
                                this.spoofedX = Scaffold.mc.thePlayer.posX + event.getX();
                                this.spoofedZ = Scaffold.mc.thePlayer.posZ + event.getZ();
                                MovementUtil.strafe(event, speed);
                            } else {
                                this.sprintTicks = 0;
                            }
                        }
                        this.wasTowering = this.towering;
                        ++this.toweringTicks;
                        break;
                    }
                    case "Full": {
                        ++this.sprintTicks;
                        double speed = 0.24989 - Math.random() * 1.0E-5;
                        if (Scaffold.mc.thePlayer.onGround && !this.towering) {
                            MovementUtil.strafe(speed - 0.18);
                            this.spoofedX = Scaffold.mc.thePlayer.posX + event.getX();
                            this.spoofedZ = Scaffold.mc.thePlayer.posZ + event.getZ();
                            MovementUtil.strafe(event, speed);
                            break;
                        }
                        this.sprintTicks = 0;
                        break;
                    }
                    case "None": {
                        if (this.towering != this.wasTowering) {
                            if (!this.towering && this.toweringTicks > 4) {
                                this.pendingMovementStop = true;
                            }
                            if (!this.towering) {
                                // empty if block
                            }
                            this.toweringTicks = 0;
                        }
                        if (Scaffold.mc.thePlayer.onGround && this.pendingMovementStop) {
                            MovementUtil.strafe(event, 0.04);
                            this.pendingMovementStop = false;
                        }
                        if (!this.towering) {
                            event.setX(event.getX() * (double)0.95f);
                            event.setZ(event.getZ() * (double)0.95f);
                        }
                        this.wasTowering = this.towering;
                        ++this.toweringTicks;
                    }
                }
                boolean airUnder = WorldUtil.negativeExpand(0.299);
                if (!allowTower) break;
                switch (this.hypixelTower.getMode()) {
                    case "Faster vertically": {
                        double towerSpeed;
                        if (!MovementUtil.isMoving() || !(MovementUtil.getHorizontalMotion() > 0.1) || Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) break;
                        double d = towerSpeed = MovementUtil.isGoingDiagonally(0.1) ? this.towerSpeedWhenDiagonal.getValue() : this.towerSpeed.getValue();
                        if (Scaffold.mc.thePlayer.onGround) {
                            boolean bl = this.towering = Scaffold.mc.gameSettings.keyBindJump.pressed && !airUnder;
                            if (this.towering) {
                                this.towerTicks = 0;
                                Scaffold.mc.thePlayer.jumpTicks = 0;
                                if (event.getY() > 0.0) {
                                    Scaffold.mc.thePlayer.motionY = 0.41985f;
                                    event.setY(0.41985f);
                                    MovementUtil.strafe(event, towerSpeed - this.randomAmount());
                                }
                            }
                        } else if (this.towering) {
                            if (this.towerTicks == 2) {
                                event.setY(Math.floor(Scaffold.mc.thePlayer.posY + 1.0) - Scaffold.mc.thePlayer.posY);
                            } else if (this.towerTicks == 3) {
                                if (Scaffold.mc.gameSettings.keyBindJump.pressed && !airUnder) {
                                    Scaffold.mc.thePlayer.motionY = 0.41985f;
                                    event.setY(0.41985f);
                                    MovementUtil.strafe(event, towerSpeed - this.randomAmount());
                                    this.towerTicks = 0;
                                } else {
                                    this.towering = false;
                                }
                            }
                        }
                        if (this.towering) {
                            // empty if block
                        }
                        ++this.towerTicks;
                        break;
                    }
                    case "Faster horizontally": {
                        if (Scaffold.mc.thePlayer.onGround) {
                            this.towerTicks = 0;
                        }
                        Scaffold.mc.thePlayer.jumpTicks = 0;
                        if (!MovementUtil.isMoving() || !(MovementUtil.getHorizontalMotion() > 0.1) || Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) break;
                        this.towering = Scaffold.mc.gameSettings.keyBindJump.pressed;
                        if (!this.towering) break;
                        if (Scaffold.mc.thePlayer.onGround) {
                            this.towerTicks = 0;
                        } else if (this.towerTicks == 7) {
                            // empty if block
                        }
                        ++this.towerTicks;
                        break;
                    }
                    case "Legit": {
                        Scaffold.mc.thePlayer.jumpTicks = 0;
                    }
                }
                break;
            }
        }
        if (!this.mode.is("Hypixel") && !this.mode.is("Hypixel jump")) {
            boolean canTower = false;
            switch (this.towerTiming.getMode()) {
                case "Always": {
                    canTower = true;
                    break;
                }
                case "Only when moving": {
                    canTower = MovementUtil.isMoving() && MovementUtil.getHorizontalMotion() >= this.minMotionForMovement.getValue();
                    break;
                }
                case "Only when not moving": {
                    canTower = !MovementUtil.isMoving() && MovementUtil.getHorizontalMotion() <= this.minMotionForMovement.getValue();
                }
            }
            boolean pressingSpace = KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump);
            block48 : switch (this.tower.getMode()) {
                case "Vanilla": {
                    if (!pressingSpace) break;
                    MovementUtil.jump(event);
                    break;
                }
                case "NCP": {
                    if (!pressingSpace || !(MovementUtil.getHorizontalMotion() < 0.1) || Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) break;
                    if (Scaffold.mc.thePlayer.onGround) {
                        MovementUtil.jump(event);
                        this.towerTicks = 0;
                    } else if (this.towerTicks == 2) {
                        Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                        Scaffold.mc.thePlayer.motionY = 0.0;
                        event.setY(0.0);
                    } else if (this.towerTicks == 3) {
                        MovementUtil.jump(event);
                        this.towerTicks = 0;
                    }
                    ++this.towerTicks;
                    break;
                }
                case "NCP2": {
                    if (!pressingSpace || Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) break;
                    if (Scaffold.mc.thePlayer.onGround) {
                        MovementUtil.jump(event);
                        this.towerTicks = 0;
                    } else if (this.towerTicks == 3) {
                        Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                        Scaffold.mc.thePlayer.motionY = 0.0;
                        event.setY(0.0);
                    } else if (this.towerTicks == 4) {
                        MovementUtil.jump(event);
                        this.towerTicks = 0;
                    }
                    ++this.towerTicks;
                    break;
                }
                case "Hypixel": {
                    if (MovementUtil.isMoving() && MovementUtil.getHorizontalMotion() > 0.1 && !Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) {
                        if (Scaffold.mc.thePlayer.onGround) {
                            this.towering = Scaffold.mc.gameSettings.keyBindJump.pressed;
                            if (this.towering) {
                                this.towerTicks = 0;
                                Scaffold.mc.thePlayer.jumpTicks = 0;
                                if (event.getY() > 0.0) {
                                    Scaffold.mc.thePlayer.motionY = 0.41985f;
                                    event.setY(0.41985f);
                                    MovementUtil.strafe(event, 0.26);
                                }
                            }
                        } else if (this.towerTicks == 2) {
                            event.setY(Math.floor(Scaffold.mc.thePlayer.posY + 1.0) - Scaffold.mc.thePlayer.posY);
                        } else if (this.towerTicks == 3) {
                            this.towering = Scaffold.mc.gameSettings.keyBindJump.pressed;
                            if (this.towering) {
                                Scaffold.mc.thePlayer.motionY = 0.41985f;
                                event.setY(0.41985f);
                                this.towerTicks = 0;
                            }
                        }
                        ++this.towerTicks;
                        break;
                    }
                    this.towering = false;
                    break;
                }
                case "Hypixel2": {
                    if (Scaffold.mc.thePlayer.onGround) {
                        this.towerTicks = 0;
                    }
                    Scaffold.mc.thePlayer.jumpTicks = 0;
                    if (MovementUtil.isMoving() && MovementUtil.getHorizontalMotion() > 0.1 && !Scaffold.mc.thePlayer.isPotionActive(Potion.jump)) {
                        this.towering = Scaffold.mc.gameSettings.keyBindJump.pressed;
                        if (!this.towering) break;
                        if (Scaffold.mc.thePlayer.onGround) {
                            this.towerTicks = 0;
                        } else if (this.towerTicks == 7) {
                            Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                        }
                        ++this.towerTicks;
                        break;
                    }
                    this.towering = false;
                    break;
                }
                case "Legit": {
                    if (!pressingSpace) break;
                    Scaffold.mc.thePlayer.jumpTicks = 0;
                    break;
                }
                case "Custom": {
                    if (!pressingSpace || !canTower) break;
                    Scaffold.mc.thePlayer.jumpTicks = 0;
                    if (Scaffold.mc.thePlayer.onGround) {
                        if (event.getY() > 0.0) {
                            Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                            event.setY(Scaffold.mc.thePlayer.motionY);
                            MovementUtil.strafe(event, 0.26);
                        }
                        this.towerTicks = 0;
                        break;
                    }
                    ++this.towerTicks;
                    switch (this.towerTicks) {
                        case 1: {
                            switch (this.teleportTick1.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick1.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue1.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue1.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue1.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue1.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 2: {
                            switch (this.teleportTick2.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick2.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue2.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue2.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue2.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue2.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 3: {
                            switch (this.teleportTick3.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick3.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue3.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue3.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue3.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue3.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 4: {
                            switch (this.teleportTick4.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick4.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue4.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue4.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue4.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue4.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 5: {
                            switch (this.teleportTick5.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick5.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue5.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue5.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue5.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue5.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 6: {
                            switch (this.teleportTick6.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick6.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue6.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue6.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue6.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue6.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 7: {
                            switch (this.teleportTick7.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick7.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue7.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue7.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue7.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue7.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                            break block48;
                        }
                        case 8: {
                            switch (this.teleportTick8.getMode()) {
                                case "Set pos to rounded Y": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.round(Scaffold.mc.thePlayer.posY), Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                    break;
                                }
                                case "Teleport to block over": {
                                    Scaffold.mc.thePlayer.setPosition(Scaffold.mc.thePlayer.posX, Math.floor(Scaffold.mc.thePlayer.posY) + 1.0, Scaffold.mc.thePlayer.posZ);
                                    Scaffold.mc.thePlayer.motionY = 0.0;
                                    event.setY(0.0);
                                }
                            }
                            switch (this.yMotionTick8.getMode()) {
                                case "Set motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = this.yMotionValue8.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(this.yMotionValue8.getValue());
                                    break block48;
                                }
                                case "Add motionY": {
                                    if (this.yChangeAffectsNextMotion.isEnabled()) {
                                        Scaffold.mc.thePlayer.motionY = event.getY() + this.yMotionValue8.getValue();
                                        event.setY(Scaffold.mc.thePlayer.motionY);
                                        break block48;
                                    }
                                    event.setY(event.getY() + this.yMotionValue8.getValue());
                                    break block48;
                                }
                                case "Jump again": {
                                    Scaffold.mc.thePlayer.motionY = (float)this.jumpMotionY.getValue();
                                    event.setY(Scaffold.mc.thePlayer.motionY);
                                    this.towerTicks = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private double randomAmount() {
        return 1.0E-4 + Math.random() * 0.001;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Listener
    public void onMotion(MotionEvent event) {
        block4 : switch (this.mode.getMode()) {
            case "Basic custom": {
                switch (this.customGroundspoof.getMode()) {
                    case "Offground": {
                        event.setOnGround(false);
                        break;
                    }
                    case "Alternate": {
                        if (!Scaffold.mc.thePlayer.onGround) break;
                        if (this.groundSpoofCounter % 2 == 0) {
                            event.setOnGround(false);
                        }
                        ++this.groundSpoofCounter;
                        break;
                    }
                }
                break;
            }
            case "Hypixel": {
                block20 : switch (this.hypixelSprint.getMode()) {
                    case "Full": {
                        if (!Scaffold.mc.thePlayer.onGround) break;
                        if (this.sprintTicks % 2 != 0) {
                            event.setX(this.spoofedX);
                            event.setZ(this.spoofedZ);
                            break;
                        }
                        event.setOnGround(false);
                        break;
                    }
                    case "Semi": {
                        if (!this.startedSprint) break;
                        if (Scaffold.mc.thePlayer.onGround) {
                            if (this.ticksHovering == 0 && KeyboardUtil.isPressed(Scaffold.mc.gameSettings.keyBindJump)) break;
                            switch (++this.ticksHovering) {
                                case 1: {
                                    if (MovementUtil.isGoingDiagonally(0.1) && this.overAir) {
                                        this.ticksHovering = 0;
                                        this.wasHovering = false;
                                        break block20;
                                    }
                                    event.setY(event.getY() + 5.0E-4);
                                    event.setOnGround(false);
                                    this.wasHovering = true;
                                    break block20;
                                }
                                case 2: {
                                    this.ticksHovering = 0;
                                    this.wasHovering = false;
                                    break block4;
                                }
                            }
                            break;
                        }
                        this.ticksHovering = 0;
                    }
                }
                break;
            }
        }
        if (this.shouldRotateSilently()) {
            event.setYaw(this.rotations.getYaw());
            event.setPitch(this.rotations.getPitch());
        }
    }

    public boolean shouldOverrideRenderedRots() {
        return this.mode.is("Hypixel") && !this.hypixelSprint.is("None");
    }

    @Listener
    public void onPostMotion(PostMotionEvent event) {
    }

    @Listener
    public void onPacketSend(PacketSendEvent event) {
        C08PacketPlayerBlockPlacement packet;
        if (!(event.getPacket() instanceof C08PacketPlayerBlockPlacement) || (packet = (C08PacketPlayerBlockPlacement)event.getPacket()).getPlacedBlockDirection() != 255) {
            // empty if block
        }
    }

    private boolean shouldRotateSilently() {
        return this.mode.is("Basic") || this.mode.is("Hypixel") || this.mode.is("Hypixel jump") || this.mode.is("Sneak") || this.mode.is("Godbridge") || this.mode.is("Andromeda") && this.rotationsEnabled.isEnabled() || this.mode.is("Basic custom") && this.shouldRotate();
    }

    private float getYawDirection() {
        switch (this.mode.getMode()) {
            case "Basic custom": {
                return MovementUtil.getPlayerDirection();
            }
        }
        return MovementUtil.getPlayerDirection();
    }

    public int getBlockSlot() {
        int bestSlot = 0;
        for (int i = 8; i >= 0; --i) {
            ItemStack stack = Scaffold.mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock) || InventoryUtil.isBlockBlacklisted(stack.getItem()) || stack.stackSize < 5) continue;
            bestSlot = i;
            this.changedSlot = true;
            break;
        }
        return bestSlot;
    }

    @Override
    public String getSuffix() {
        return this.mode.getMode();
    }

    public float getRenderedYaw() {
        return this.renderedYaw;
    }

    public float getRenderedPitch() {
        return this.renderedPitch;
    }
}

