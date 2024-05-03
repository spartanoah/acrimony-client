/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.JumpEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.MoveEvent;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.Render3DEvent;
import Acrimony.event.impl.StrafeEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.network.PacketUtil;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.PlayerUtil;
import Acrimony.util.player.RotationsUtil;
import Acrimony.util.render.RenderUtil;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.block.BlockSlime;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class Speed
extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "NCP", "Watchdog", "Blocksmc", "Strafe", "Fake strafe");
    private final DoubleSetting vanillaSpeed = new DoubleSetting("Vanilla speed", () -> this.mode.is("Vanilla"), 1.0, 0.2, 9.0, 0.1);
    private final BooleanSetting autoJump = new BooleanSetting("Autojump", () -> this.mode.is("Vanilla") || this.mode.is("Strafe"), true);
    private final ModeSetting ncpMode = new ModeSetting("NCP Mode", () -> this.mode.is("NCP"), "Hop", "Hop", "Updated Hop");
    private final BooleanSetting damageBoost = new BooleanSetting("Damage Boost", () -> this.mode.is("NCP") && this.ncpMode.is("Updated Hop"), true);
    public final ModeSetting watchdogMode = new ModeSetting("Watchdog Mode", () -> this.mode.is("Watchdog"), "Strafe", "Strafe", "Semi-Strafe", "Strafeless", "Ground");
    private final BooleanSetting fast = new BooleanSetting("Fast", () -> this.mode.is("Watchdog") && (this.watchdogMode.is("Strafe") || this.watchdogMode.is("Strafeless")), true);
    private final DoubleSetting attributeSpeedOffground = new DoubleSetting("Attribute speed offground", () -> this.mode.is("Watchdog") && this.watchdogMode.is("Strafe"), 0.023, 0.02, 0.026, 0.001);
    private final DoubleSetting mult = new DoubleSetting("Mult", () -> this.mode.is("Watchdog") && this.watchdogMode.is("Strafeless") && this.fast.isEnabled(), 1.24, 1.0, 1.3, 0.005);
    private final DoubleSetting speedPotMult = new DoubleSetting("Speed pot mult", () -> this.mode.is("Watchdog") && this.watchdogMode.is("Strafeless") && this.fast.isEnabled(), 1.24, 1.0, 1.3, 0.005);
    private final BooleanSetting allDirSprint = new BooleanSetting("All directions sprint", () -> this.mode.is("Strafe"), true);
    private final IntegerSetting minHurtTime = new IntegerSetting("Min hurttime", () -> this.mode.is("Strafe"), 10, 0, 10, 1);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", () -> this.mode.is("Fake strafe"), true);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", () -> this.mode.is("Fake strafe"), false);
    private final BooleanSetting groundStrafe = new BooleanSetting("Ground Strafe", () -> this.mode.is("Fake strafe"), false);
    private final ModeSetting velocityMode = new ModeSetting("Velocity handling", () -> this.mode.is("Fake strafe"), "Ignore", "Ignore", "Vertical", "Legit");
    private final ModeSetting clientSpeed = new ModeSetting("Client speed", () -> this.mode.is("Fake strafe"), "Normal", "Normal", "Custom");
    private final DoubleSetting customClientSpeed = new DoubleSetting("Custom client speed", () -> this.mode.is("Fake strafe") && this.clientSpeed.is("Custom"), 0.5, 0.15, 1.0, 0.025);
    private final BooleanSetting fakeFly = new BooleanSetting("Fake fly", () -> this.mode.is("Fake strafe"), false);
    private final BooleanSetting renderRealPosBox = new BooleanSetting("Render box at real pos", () -> this.mode.is("Fake strafe"), true);
    private final ModeSetting timerMode = new ModeSetting("Timer mode", () -> this.mode.is("NCP"), "None", "None", "Bypass", "Custom");
    private final DoubleSetting customTimer = new DoubleSetting("Custom timer", () -> this.mode.is("NCP") && this.timerMode.is("Custom") || this.mode.is("Watchdog"), 1.0, 0.1, 3.0, 0.05);
    private double speed;
    private boolean prevOnGround;
    private int counter;
    private int ticks;
    private int offGroundTicks;
    private int ticksSinceVelocity;
    private boolean takingVelocity;
    private boolean wasTakingVelocity;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double velocityDist;
    private float lastDirection;
    private float lastYaw;
    private double motionX;
    private double motionY;
    private double motionZ;
    private double actualX;
    private double actualY;
    private double actualZ;
    private double lastActualX;
    private double lastActualY;
    private double lastActualZ;
    private boolean actualGround;
    private boolean started;
    private boolean firstJumpDone;
    private boolean wasCollided;
    private int oldSlot;
    private final ArrayList<BlockPos> barriers = new ArrayList();
    private float lastForward;
    private float lastStrafe;

    public Speed() {
        super("Speed", Category.MOVEMENT);
        this.addSettings(this.mode, this.vanillaSpeed, this.autoJump, this.ncpMode, this.damageBoost, this.watchdogMode, this.fast, this.mult, this.speedPotMult, this.attributeSpeedOffground, this.allDirSprint, this.minHurtTime, this.sprint, this.rotate, this.groundStrafe, this.velocityMode, this.clientSpeed, this.customClientSpeed, this.fakeFly, this.renderRealPosBox, this.timerMode, this.customTimer);
    }

    @Override
    public void onEnable() {
        this.prevOnGround = false;
        this.speed = 0.28;
        this.counter = 0;
        this.offGroundTicks = 0;
        this.ticks = 0;
        this.ticksSinceVelocity = Integer.MAX_VALUE;
        this.firstJumpDone = false;
        this.started = false;
        this.wasTakingVelocity = false;
        this.takingVelocity = false;
        this.motionX = Speed.mc.thePlayer.motionX;
        this.motionY = Speed.mc.thePlayer.motionY;
        this.motionZ = Speed.mc.thePlayer.motionZ;
        this.actualX = Speed.mc.thePlayer.posX;
        this.actualY = Speed.mc.thePlayer.posY;
        this.actualZ = Speed.mc.thePlayer.posZ;
        this.actualGround = Speed.mc.thePlayer.onGround;
        this.lastDirection = MovementUtil.getPlayerDirection();
        this.lastYaw = Speed.mc.thePlayer.rotationYaw;
        this.lastForward = Speed.mc.thePlayer.moveForward;
        this.lastStrafe = Speed.mc.thePlayer.moveStrafing;
        this.oldSlot = Speed.mc.thePlayer.inventory.currentItem;
        this.wasCollided = false;
    }

    @Override
    public void onDisable() {
        Speed.mc.timer.timerSpeed = 1.0f;
        switch (this.mode.getMode()) {
            case "Vulcan": {
                Speed.mc.thePlayer.inventory.currentItem = this.oldSlot;
                break;
            }
            case "Watchdog": {
                if (!this.watchdogMode.is("Strafe")) break;
                Speed.mc.thePlayer.motionX *= 0.2;
                Speed.mc.thePlayer.motionZ *= 0.2;
                break;
            }
            case "Fake strafe": {
                Speed.mc.thePlayer.setPosition(this.actualX, this.actualY, this.actualZ);
                Speed.mc.thePlayer.motionX = this.motionX;
                Speed.mc.thePlayer.motionY = this.motionY;
                Speed.mc.thePlayer.motionZ = this.motionZ;
                Speed.mc.thePlayer.onGround = this.actualGround;
            }
        }
        if (!this.barriers.isEmpty()) {
            for (BlockPos pos : this.barriers) {
                Speed.mc.theWorld.setBlockToAir(pos);
            }
            this.barriers.clear();
        }
    }

    @Listener
    public void onStrafe(StrafeEvent event) {
        switch (this.mode.getMode()) {
            case "Watchdog": {
                if (!this.watchdogMode.is("Test")) break;
                if (!Speed.mc.thePlayer.isSprinting()) {
                    event.setAttributeSpeed(event.getAttributeSpeed() * 1.3f);
                }
                if (!Speed.mc.thePlayer.onGround || Speed.mc.gameSettings.keyBindJump.isKeyDown()) break;
                Speed.mc.thePlayer.jump();
                break;
            }
            case "Strafe": {
                if (this.allDirSprint.isEnabled() && !Speed.mc.thePlayer.isSprinting()) {
                    event.setAttributeSpeed(event.getAttributeSpeed() * 1.3f);
                }
                if (!this.autoJump.isEnabled() || !Speed.mc.thePlayer.onGround || Speed.mc.gameSettings.keyBindJump.isKeyDown()) break;
                Speed.mc.thePlayer.jump();
            }
        }
    }

    @Listener
    public void onJump(JumpEvent event) {
        switch (this.mode.getMode()) {
            case "Strafe": {
                if (!this.allDirSprint.isEnabled()) break;
                event.setBoosting(MovementUtil.isMoving());
                event.setYaw(MovementUtil.getPlayerDirection());
                break;
            }
            case "Watchdog": {
                if (!this.watchdogMode.is("Test")) break;
                event.setBoosting(MovementUtil.isMoving());
                event.setYaw(MovementUtil.getPlayerDirection());
                break;
            }
            case "Test": 
            case "Test2": {
                event.setBoosting(MovementUtil.isMoving());
                event.setYaw(MovementUtil.getPlayerDirection());
            }
        }
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        switch (this.mode.getMode()) {
            case "Vulcan": {
                for (int i = 8; i >= 0; --i) {
                    ItemStack stack = Speed.mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack == null || !(stack.getItem() instanceof ItemBlock) || PlayerUtil.isBlockBlacklisted(stack.getItem())) continue;
                    Speed.mc.thePlayer.inventory.currentItem = i;
                    break;
                }
                if (Speed.mc.thePlayer.onGround) {
                    if (!MovementUtil.isMoving()) break;
                    Speed.mc.thePlayer.jump();
                    this.ticks = 0;
                    break;
                }
                if (this.ticks == 4) {
                    if (this.started) {
                        Speed.mc.thePlayer.motionY = -1.0;
                    }
                    double x = Speed.mc.thePlayer.motionX > 0.0 ? 1.5 : -1.5;
                    double z = Speed.mc.thePlayer.motionZ > 0.0 ? 1.5 : -1.5;
                    Speed.mc.playerController.syncCurrentPlayItem();
                    PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(Speed.mc.thePlayer.posX + x, Speed.mc.thePlayer.posY - 2.0, Speed.mc.thePlayer.posZ + z), EnumFacing.UP.getIndex(), Speed.mc.thePlayer.inventory.getStackInSlot(Speed.mc.thePlayer.inventory.currentItem), 0.5f, 1.0f, 0.5f));
                    Speed.mc.thePlayer.swingItem();
                    this.started = true;
                }
                ++this.ticks;
                break;
            }
            case "Test": {
                if (!Speed.mc.thePlayer.onGround) break;
                Speed.mc.thePlayer.jump();
                Speed.mc.thePlayer.motionY = 0.0;
            }
        }
    }

    @Listener
    public void onMove(MoveEvent event) {
        if (!this.takingVelocity && Speed.mc.thePlayer.onGround) {
            this.wasTakingVelocity = false;
        }
        double velocityExtra = 0.28 + (double)MovementUtil.getSpeedAmplifier() * 0.07;
        float direction = MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection());
        float forward = Speed.mc.thePlayer.moveForward;
        float strafe = Speed.mc.thePlayer.moveStrafing;
        switch (this.mode.getMode()) {
            case "Vanilla": {
                if (Speed.mc.thePlayer.onGround && MovementUtil.isMoving() && this.autoJump.isEnabled()) {
                    Speed.mc.thePlayer.motionY = Speed.mc.thePlayer.getJumpUpwardsMotion();
                    event.setY(Speed.mc.thePlayer.motionY);
                }
                MovementUtil.strafe(event, this.vanillaSpeed.getValue());
                break;
            }
            case "NCP": {
                switch (this.ncpMode.getMode()) {
                    case "Hop": {
                        if (Speed.mc.thePlayer.onGround) {
                            this.prevOnGround = true;
                            if (MovementUtil.isMoving()) {
                                Speed.mc.thePlayer.motionY = Speed.mc.thePlayer.getJumpUpwardsMotion();
                                event.setY(Speed.mc.thePlayer.motionY);
                                this.speed *= 0.91;
                                this.speed += (this.ticks >= 8 ? 0.2 : 0.15) + (double)Speed.mc.thePlayer.getAIMoveSpeed();
                                this.ticks = 0;
                            }
                        } else if (this.prevOnGround) {
                            this.speed *= 0.58;
                            this.speed += 0.026;
                            this.prevOnGround = false;
                        } else {
                            this.speed *= 0.91;
                            this.speed += 0.026;
                            ++this.ticks;
                        }
                        if (!(this.speed > 0.2)) break;
                        this.speed -= 1.0E-6;
                        break;
                    }
                    case "Updated Hop": {
                        if (Speed.mc.thePlayer.onGround) {
                            this.prevOnGround = true;
                            if (!MovementUtil.isMoving()) break;
                            MovementUtil.jump(event);
                            this.speed *= 0.91;
                            if (this.takingVelocity && this.damageBoost.isEnabled()) {
                                this.speed = this.velocityDist + velocityExtra;
                            }
                            this.speed += 0.2 + (double)Speed.mc.thePlayer.getAIMoveSpeed();
                            break;
                        }
                        if (this.prevOnGround) {
                            this.speed *= 0.53;
                            if (this.takingVelocity && this.damageBoost.isEnabled()) {
                                this.speed = this.velocityDist + velocityExtra;
                            }
                            this.speed += (double)0.026f;
                            this.prevOnGround = false;
                            break;
                        }
                        this.speed *= 0.91;
                        if (this.takingVelocity && this.damageBoost.isEnabled()) {
                            this.speed = this.velocityDist + velocityExtra;
                        }
                        this.speed += (double)0.026f;
                    }
                }
                switch (this.timerMode.getMode()) {
                    case "None": {
                        Speed.mc.timer.timerSpeed = 1.0f;
                        break;
                    }
                    case "Bypass": {
                        Speed.mc.timer.timerSpeed = 1.08f;
                        break;
                    }
                    case "Custom": {
                        Speed.mc.timer.timerSpeed = (float)this.customTimer.getValue();
                    }
                }
                MovementUtil.strafe(event, this.speed);
                break;
            }
            case "Watchdog": {
                switch (this.watchdogMode.getMode()) {
                    case "Strafe": {
                        if (Speed.mc.thePlayer.onGround) {
                            if (MovementUtil.isMoving()) {
                                this.prevOnGround = true;
                                MovementUtil.jump(event);
                                this.speed = 0.585 + (double)MovementUtil.getSpeedAmplifier() * 0.065;
                            }
                        } else if (this.prevOnGround) {
                            this.speed = this.ticks++ % 5 > 0 && this.fast.isEnabled() ? (this.speed *= (double)0.65f) : (this.speed *= (double)0.53f);
                            this.prevOnGround = false;
                        } else {
                            this.speed = Math.min(this.speed, 0.35 + (double)MovementUtil.getSpeedAmplifier() * 0.02);
                            this.speed *= (double)0.91f;
                            this.speed += (double)((float)this.attributeSpeedOffground.getValue() * 0.98f);
                        }
                        MovementUtil.strafe(event, this.speed);
                        break;
                    }
                    case "Semi-Strafe": {
                        if (Speed.mc.thePlayer.onGround) {
                            this.prevOnGround = true;
                            if (MovementUtil.isMoving()) {
                                MovementUtil.jump(event);
                                this.speed = 0.6 + (double)MovementUtil.getSpeedAmplifier() * 0.075;
                            }
                        } else if (this.prevOnGround) {
                            this.speed *= (double)0.54f;
                            this.prevOnGround = false;
                        } else {
                            this.speed *= (double)0.6f;
                            this.speed += (double)((Speed.mc.thePlayer.isSprinting() ? 0.026f : 0.02f) * 0.6f);
                        }
                        direction = MovementUtil.getPlayerDirection();
                        if (!Speed.mc.thePlayer.onGround) {
                            float dirChange = Math.abs(direction - this.lastDirection);
                            if (dirChange > 180.0f) {
                                dirChange = 360.0f - dirChange;
                            }
                            double reduceMult = 1.0 - (double)dirChange * 0.01;
                            this.speed *= reduceMult;
                            this.speed = Math.max(this.speed, 0.09);
                        }
                        if (Speed.mc.thePlayer.isCollidedHorizontally) {
                            this.speed = 0.09;
                        }
                        MovementUtil.strafe(event, this.speed);
                        this.lastDirection = direction;
                        break;
                    }
                    case "Strafeless": {
                        if (MovementUtil.isMoving()) {
                            if (Speed.mc.thePlayer.onGround) {
                                this.prevOnGround = true;
                                MovementUtil.jump(event);
                                if (Speed.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                    MovementUtil.strafeNoTargetStrafe(event, 0.59 - Math.random() * 0.001 + (double)MovementUtil.getSpeedAmplifier() * 0.08);
                                } else {
                                    MovementUtil.strafeNoTargetStrafe(event, 0.6 - Math.random() * 0.001);
                                }
                            } else if (this.prevOnGround) {
                                if (Speed.mc.thePlayer.isSprinting() && ++this.counter > 1 && this.fast.isEnabled()) {
                                    if (Speed.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                                        event.setX(event.getX() * this.speedPotMult.getValue());
                                        event.setZ(event.getZ() * this.speedPotMult.getValue());
                                    } else {
                                        event.setX(event.getX() * this.mult.getValue());
                                        event.setZ(event.getZ() * this.mult.getValue());
                                    }
                                }
                                this.prevOnGround = false;
                            }
                        }
                        this.lastForward = forward;
                        this.lastStrafe = strafe;
                        break;
                    }
                    case "Ground": {
                        if (Speed.mc.thePlayer.onGround) {
                            this.ticks = 0;
                            if (!this.started) {
                                MovementUtil.jump(event);
                                MovementUtil.strafe(event, 0.55 + (double)MovementUtil.getSpeedAmplifier() * 0.07);
                                this.started = true;
                            } else {
                                Speed.mc.thePlayer.motionY = 5.0E-4;
                                event.setY(5.0E-4);
                                this.firstJumpDone = true;
                                this.speed = 0.335 + (double)((float)MovementUtil.getSpeedAmplifier() * 0.045f);
                            }
                        } else {
                            ++this.ticks;
                            if (this.speed > 0.28) {
                                this.speed *= 0.995;
                            }
                        }
                        if (!this.firstJumpDone || this.ticks > 2) break;
                        MovementUtil.strafe(event, this.speed);
                    }
                }
                Speed.mc.timer.timerSpeed = (float)this.customTimer.getValue();
                break;
            }
            case "Blocksmc": {
                if (Speed.mc.thePlayer.onGround) {
                    this.prevOnGround = true;
                    if (MovementUtil.isMoving()) {
                        MovementUtil.jump(event);
                        this.speed = 0.57 + (double)MovementUtil.getSpeedAmplifier() * 0.065;
                        if (this.takingVelocity && this.damageBoost.isEnabled()) {
                            this.speed = this.velocityDist + velocityExtra;
                        }
                        this.ticks = 1;
                    }
                } else if (this.prevOnGround) {
                    this.speed *= 0.53;
                    if (this.takingVelocity && this.damageBoost.isEnabled()) {
                        this.speed = this.velocityDist + velocityExtra;
                    }
                    this.speed += (double)0.026f;
                    this.prevOnGround = false;
                } else {
                    this.speed *= 0.91;
                    if (this.takingVelocity && this.damageBoost.isEnabled()) {
                        this.speed = this.velocityDist + velocityExtra;
                    }
                    this.speed += (double)0.026f;
                }
                if (this.takingVelocity) {
                    this.ticks = -7;
                }
                if (++this.ticks == 0 && !Speed.mc.thePlayer.onGround) {
                    this.speed = 0.28 + (double)MovementUtil.getSpeedAmplifier() * 0.065;
                }
                MovementUtil.strafe(event, this.speed);
                break;
            }
            case "Strafe": {
                if (Speed.mc.thePlayer.hurtTime > this.minHurtTime.getValue()) break;
                MovementUtil.strafe(event);
                break;
            }
            case "Fake strafe": {
                double distance = Math.hypot(Speed.mc.thePlayer.posX - this.actualX, Speed.mc.thePlayer.posZ - this.actualZ);
                if (this.fakeFly.isEnabled()) {
                    if (Speed.mc.gameSettings.keyBindJump.isKeyDown()) {
                        event.setY(0.35);
                    } else if (Speed.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        event.setY(-0.35);
                    } else {
                        event.setY(0.0);
                    }
                    Speed.mc.thePlayer.motionY = 0.0;
                } else if (Speed.mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    MovementUtil.jump(event);
                }
                if (!this.started) {
                    this.speed = 0.65;
                    this.started = true;
                } else if (this.clientSpeed.is("Normal")) {
                    double baseSpeed = 0.33 + (double)MovementUtil.getSpeedAmplifier() * 0.02;
                    this.speed = Speed.mc.thePlayer.onGround ? 0.33 + baseSpeed : Math.min(this.speed - baseSpeed * distance * 0.15, baseSpeed);
                    this.speed = Math.max(this.speed, 0.2);
                } else if (this.clientSpeed.is("Custom")) {
                    this.speed = this.customClientSpeed.getValue();
                }
                MovementUtil.strafe(event, this.speed);
                this.lastDirection = direction;
            }
        }
    }

    @Listener
    public void onEntityAction(EntityActionEvent event) {
        switch (this.mode.getMode()) {
            case "Fake strafe": {
                this.lastActualX = this.actualX;
                this.lastActualY = this.actualY;
                this.lastActualZ = this.actualZ;
                float direction = RotationsUtil.getRotationsToPosition(this.lastActualX, this.lastActualY, this.lastActualZ, Speed.mc.thePlayer.posX, Speed.mc.thePlayer.posY, Speed.mc.thePlayer.posZ)[0];
                float gcd = RotationsUtil.getGCD();
                float yawDiff = direction - this.lastYaw;
                float fixedYawDiff = yawDiff - yawDiff % gcd;
                direction = this.lastYaw + fixedYawDiff;
                float dir = direction * ((float)Math.PI / 180);
                float friction = this.getFriction(this.actualX, this.actualY, this.actualZ) * 0.91f;
                if (this.actualGround) {
                    this.motionY = Speed.mc.thePlayer.getJumpUpwardsMotion();
                    if (Speed.mc.thePlayer.isPotionActive(Potion.jump)) {
                        this.motionY += (double)((float)(Speed.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1f);
                    }
                    if (!this.wasCollided) {
                        this.motionX -= (double)(MathHelper.sin(dir) * 0.2f);
                        this.motionZ += (double)(MathHelper.cos(dir) * 0.2f);
                    }
                }
                float aa = 0.16277136f / (friction * friction * friction);
                Speed.mc.thePlayer.setSprinting(!this.wasCollided);
                float attributeSpeed = this.actualGround ? Speed.mc.thePlayer.getAIMoveSpeed() * aa : (this.wasCollided ? 0.02f : 0.026f);
                boolean oldActualGround = this.actualGround;
                float forward = 0.98f;
                float strafe = 0.0f;
                float thing = strafe * strafe + forward * forward;
                if (thing >= 1.0E-4f) {
                    if ((thing = MathHelper.sqrt_float(thing)) < 1.0f) {
                        thing = 1.0f;
                    }
                    thing = attributeSpeed / thing;
                    float f1 = MathHelper.sin(direction * (float)Math.PI / 180.0f);
                    float f2 = MathHelper.cos(direction * (float)Math.PI / 180.0f);
                    this.motionX += (double)((strafe *= thing) * f2 - (forward *= thing) * f1);
                    this.motionZ += (double)(forward * f2 + strafe * f1);
                }
                if (this.groundStrafe.isEnabled() && this.actualGround) {
                    double speed = Math.hypot(this.motionX, this.motionZ);
                    this.motionX = -Math.sin(Math.toRadians(direction)) * speed;
                    this.motionZ = Math.cos(Math.toRadians(direction)) * speed;
                }
                double clientX = Speed.mc.thePlayer.posX;
                double clientY = Speed.mc.thePlayer.posY;
                double clientZ = Speed.mc.thePlayer.posZ;
                double clientMotionX = Speed.mc.thePlayer.motionX;
                double clientMotionY = Speed.mc.thePlayer.motionY;
                double clientMotionZ = Speed.mc.thePlayer.motionZ;
                boolean clientGround = Speed.mc.thePlayer.onGround;
                Speed.mc.thePlayer.setPosition(this.actualX, this.actualY, this.actualZ);
                Speed.mc.thePlayer.onGround = this.actualGround;
                Speed.mc.thePlayer.moveEntityNoEvent(this.motionX, this.motionY, this.motionZ);
                boolean collided = Speed.mc.thePlayer.isCollidedHorizontally;
                this.motionX = Speed.mc.thePlayer.posX - this.lastActualX;
                this.motionY = Speed.mc.thePlayer.posY - this.lastActualY;
                this.motionZ = Speed.mc.thePlayer.posZ - this.lastActualZ;
                this.actualX = Speed.mc.thePlayer.posX;
                this.actualY = Speed.mc.thePlayer.posY;
                this.actualZ = Speed.mc.thePlayer.posZ;
                this.actualGround = Speed.mc.thePlayer.onGround;
                Speed.mc.thePlayer.setPosition(clientX, clientY, clientZ);
                Speed.mc.thePlayer.onGround = clientGround;
                Speed.mc.thePlayer.motionX = clientMotionX;
                Speed.mc.thePlayer.motionY = clientMotionY;
                Speed.mc.thePlayer.motionZ = clientMotionZ;
                if (oldActualGround) {
                    this.motionX *= (double)(friction * 0.91f);
                    this.motionZ *= (double)(friction * 0.91f);
                } else {
                    this.motionX *= (double)0.91f;
                    this.motionZ *= (double)0.91f;
                }
                this.motionY -= 0.08;
                this.motionY *= (double)0.98f;
                if (Math.abs(this.motionX) < 0.005) {
                    this.motionX = 0.0;
                }
                if (Math.abs(this.motionY) < 0.005) {
                    this.motionY = 0.0;
                }
                if (Math.abs(this.motionZ) < 0.005) {
                    this.motionZ = 0.0;
                }
                if (this.sprint.isEnabled()) {
                    event.setSprinting(!this.wasCollided);
                } else {
                    event.setSprinting(false);
                }
                Speed.mc.thePlayer.setSprinting(true);
                event.setSneaking(false);
                this.wasCollided = collided;
                break;
            }
            case "Test": 
            case "Test2": {
                event.setSprinting(MovementUtil.isMoving());
            }
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        switch (this.mode.getMode()) {
            case "Fake strafe": {
                event.setX(this.actualX);
                event.setY(this.actualY);
                event.setZ(this.actualZ);
                event.setOnGround(this.actualGround);
                float direction = RotationsUtil.getRotationsToPosition(this.lastActualX, this.lastActualY, this.lastActualZ, Speed.mc.thePlayer.posX, Speed.mc.thePlayer.posY, Speed.mc.thePlayer.posZ)[0];
                if (!this.rotate.isEnabled()) break;
                float f = Speed.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
                float gcd = f * f * f * 1.2f;
                float deltaYaw = direction - this.lastYaw;
                float fixedDeltaYaw = deltaYaw - deltaYaw % gcd;
                this.lastYaw = direction = this.lastYaw + fixedDeltaYaw;
                event.setYaw(direction);
            }
        }
        this.takingVelocity = false;
        ++this.ticksSinceVelocity;
    }

    @Listener
    public void onRender3D(Render3DEvent event) {
        switch (this.mode.getMode()) {
            case "Fake strafe": {
                if (!this.renderRealPosBox.isEnabled() || Speed.mc.gameSettings.thirdPersonView <= 0) break;
                RenderUtil.prepareBoxRender(3.25f, 1.0, 1.0, 1.0, 0.8f);
                RenderUtil.renderCustomPlayerBox(mc.getRenderManager(), event.getPartialTicks(), this.actualX, this.actualY, this.actualZ, this.lastActualX, this.lastActualY, this.lastActualZ);
                RenderUtil.stopBoxRender();
            }
        }
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity)event.getPacket();
            if (Speed.mc.thePlayer.getEntityId() == packet.getEntityID()) {
                this.wasTakingVelocity = true;
                this.takingVelocity = true;
                this.velocityX = (double)packet.getMotionX() / 8000.0;
                this.velocityY = (double)packet.getMotionY() / 8000.0;
                this.velocityZ = (double)packet.getMotionZ() / 8000.0;
                this.velocityDist = Math.hypot(this.velocityX, this.velocityZ);
                this.ticksSinceVelocity = 0;
                if (this.mode.is("Fake strafe")) {
                    event.setCancelled(true);
                    switch (this.velocityMode.getMode()) {
                        case "Vertical": {
                            this.motionY = this.velocityY;
                            break;
                        }
                        case "Legit": {
                            this.motionX = this.velocityX;
                            this.motionY = this.velocityY;
                            this.motionZ = this.velocityZ;
                        }
                    }
                }
            }
        } else if (event.getPacket() instanceof S08PacketPlayerPosLook && this.mode.is("Fake strafe")) {
            this.setEnabled(false);
        }
    }

    private float getFriction(double x, double y, double z) {
        Block block = Speed.mc.theWorld.getBlockState(new BlockPos(x, Math.floor(y) - 1.0, z)).getBlock();
        if (block != null) {
            if (block instanceof BlockIce || block instanceof BlockPackedIce) {
                return 0.98f;
            }
            if (block instanceof BlockSlime) {
                return 0.8f;
            }
        }
        return 0.6f;
    }

    @Override
    public String getSuffix() {
        return this.mode.getMode();
    }

    public double getActualX() {
        return this.actualX;
    }

    public double getActualY() {
        return this.actualY;
    }

    public double getActualZ() {
        return this.actualZ;
    }

    public double getLastActualX() {
        return this.lastActualX;
    }

    public double getLastActualY() {
        return this.lastActualY;
    }

    public double getLastActualZ() {
        return this.lastActualZ;
    }
}

