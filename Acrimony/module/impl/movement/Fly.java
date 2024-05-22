/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.MoveEvent;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.PacketSendEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.event.impl.VelocityEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.misc.LogUtil;
import Acrimony.util.network.PacketUtil;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.PlayerUtil;
import Acrimony.util.world.WorldUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class Fly
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Collision", "NCP", "Blocksmc", "Velocity");
    private final ModeSetting vanillaMode = new ModeSetting("Vanilla Mode", () -> this.mode.is("Vanilla"), "Motion", "Motion", "Creative");
    private final DoubleSetting vanillaSpeed = new DoubleSetting("Vanilla speed", () -> this.mode.is("Vanilla") && this.vanillaMode.is("Motion"), 2.0, 0.2, 9.0, 0.2);
    private final DoubleSetting vanillaVerticalSpeed = new DoubleSetting("Vanilla vertical speed", () -> this.mode.is("Vanilla") && this.vanillaMode.is("Motion"), 2.0, 0.2, 9.0, 0.2);
    private final ModeSetting collisionMode = new ModeSetting("Collision mode", () -> this.mode.is("Collision"), "Airwalk", "Airwalk", "Airjump");
    private final ModeSetting ncpMode = new ModeSetting("NCP Mode", () -> this.mode.is("NCP"), "Old", "Old");
    private final DoubleSetting ncpSpeed = new DoubleSetting("NCP speed", () -> this.mode.is("NCP") && this.ncpMode.is("Old"), 1.0, 0.3, 1.7, 0.05);
    private final BooleanSetting damage = new BooleanSetting("Damage", () -> this.mode.is("NCP") && this.ncpMode.is("Old"), false);
    private final ModeSetting velocityMode = new ModeSetting("Velocity Mode", () -> this.mode.is("Velocity"), "Bow", "Bow", "Bow2", "Wait for hit");
    private final BooleanSetting legit = new BooleanSetting("Legit", () -> this.mode.is("Bow") || this.mode.is("Bow2"), false);
    private final BooleanSetting automated = new BooleanSetting("Automated", () -> this.mode.is("Blocksmc"), false);
    private double speed;
    private boolean takingVelocity;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double velocityDist;
    private int ticksSinceVelocity;
    private int counter;
    private int ticks;
    private int veloTicks;
    private boolean started;
    private boolean done;
    private double lastMotionX;
    private double lastMotionY;
    private double lastMotionZ;
    private boolean hasBow;
    private int oldSlot;
    private boolean notMoving;
    private float lastYaw;
    private float lastPitch;
    private BlockPos lastBarrier;
    private double lastY;

    public Fly() {
        super("Fly", Category.MOVEMENT);
        this.addSettings(this.mode, this.vanillaMode, this.vanillaSpeed, this.vanillaVerticalSpeed, this.ncpMode, this.ncpSpeed, this.damage, this.velocityMode, this.legit, this.automated);
    }

    @Override
    public void onEnable() {
        this.ticksSinceVelocity = Integer.MAX_VALUE;
        this.veloTicks = 0;
        this.ticks = 0;
        this.counter = 0;
        this.done = false;
        this.started = false;
        this.hasBow = false;
        this.notMoving = false;
        this.lastMotionX = Fly.mc.thePlayer.motionX;
        this.lastMotionY = Fly.mc.thePlayer.motionY;
        this.lastMotionZ = Fly.mc.thePlayer.motionZ;
        this.lastYaw = Fly.mc.thePlayer.rotationYaw;
        this.lastPitch = Fly.mc.thePlayer.rotationPitch;
        this.lastY = Fly.mc.thePlayer.posY;
        this.lastBarrier = null;
        switch (this.mode.getMode()) {
            case "NCP": {
                if (!this.ncpMode.is("Old")) break;
                if (Fly.mc.thePlayer.onGround) {
                    this.speed = this.ncpSpeed.getValue();
                    if (!this.damage.isEnabled()) break;
                    PlayerUtil.ncpDamage();
                    break;
                }
                this.speed = 0.28;
                break;
            }
            case "Velocity": {
                if (!Fly.mc.thePlayer.onGround) break;
                Fly.mc.thePlayer.jump();
            }
        }
    }

    @Override
    public void onDisable() {
        Fly.mc.thePlayer.capabilities.isFlying = false;
        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
        switch (this.mode.getMode()) {
            case "Vanilla": {
                if (!this.vanillaMode.is("Motion")) break;
                MovementUtil.strafe(0.0);
                break;
            }
            case "NCP": {
                if (!this.ncpMode.is("Old")) break;
                MovementUtil.strafe(0.0);
                break;
            }
            case "Velocity": {
                switch (this.velocityMode.getMode()) {
                    case "Wait for hit": {
                        Fly.mc.thePlayer.motionX = this.lastMotionX * 0.91;
                        Fly.mc.thePlayer.motionY = this.lastMotionY;
                        Fly.mc.thePlayer.motionZ = this.lastMotionZ * 0.91;
                        break;
                    }
                    case "Bow": {
                        Fly.mc.thePlayer.rotationYaw = this.lastYaw;
                        Fly.mc.thePlayer.rotationPitch = -90.0f;
                        Fly.mc.gameSettings.keyBindUseItem.pressed = false;
                        break;
                    }
                    case "Bow2": {
                        Fly.mc.thePlayer.motionX = this.lastMotionX * 0.91;
                        Fly.mc.thePlayer.motionY = this.lastMotionY;
                        Fly.mc.thePlayer.motionZ = this.lastMotionZ * 0.91;
                        Fly.mc.thePlayer.rotationPitch = -90.0f;
                        Fly.mc.gameSettings.keyBindUseItem.pressed = false;
                    }
                }
                break;
            }
            case "Blocksmc": {
                MovementUtil.strafe(0.0);
            }
        }
        if (this.lastBarrier != null) {
            Fly.mc.theWorld.setBlockToAir(this.lastBarrier);
        }
        Fly.mc.timer.timerSpeed = 1.0f;
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        switch (this.mode.getMode()) {
            case "Velocity": {
                switch (this.velocityMode.getMode()) {
                    case "Bow": {
                        boolean sameZDir;
                        if (!this.takingVelocity) break;
                        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
                        Fly.mc.thePlayer.motionY = this.velocityY;
                        boolean sameXDir = this.lastMotionX > 0.01 && this.velocityX > 0.0 || this.lastMotionX < -0.01 && this.velocityX < 0.0;
                        boolean bl = sameZDir = this.lastMotionZ > 0.01 && this.velocityZ > 0.0 || this.lastMotionZ < -0.01 && this.velocityZ < 0.0;
                        if (!sameXDir || !sameZDir) break;
                        Fly.mc.thePlayer.motionX = this.velocityX;
                        Fly.mc.thePlayer.motionZ = this.velocityZ;
                    }
                }
                break;
            }
            case "Collision": {
                switch (this.collisionMode.getMode()) {
                    case "Airwalk": {
                        Fly.mc.thePlayer.onGround = true;
                        break;
                    }
                    case "Airjump": {
                        if (Fly.mc.thePlayer.onGround && !Fly.mc.gameSettings.keyBindJump.isKeyDown()) {
                            Fly.mc.thePlayer.jump();
                        }
                        double d = Fly.mc.thePlayer.fallDistance;
                        double d2 = Fly.mc.gameSettings.keyBindJump.isKeyDown() ? 0.0 : 0.7;
                        if (!(d > d2)) break;
                        if (this.lastBarrier != null) {
                            Fly.mc.theWorld.setBlockToAir(this.lastBarrier);
                        }
                        this.lastBarrier = new BlockPos(Fly.mc.thePlayer.posX, Fly.mc.thePlayer.posY - 1.0, Fly.mc.thePlayer.posZ);
                        Fly.mc.theWorld.setBlockState(this.lastBarrier, Blocks.barrier.getDefaultState());
                    }
                }
                break;
            }
            case "Test": {
                if (Fly.mc.thePlayer.onGround) {
                    if (Fly.mc.gameSettings.keyBindJump.isKeyDown()) break;
                    Fly.mc.thePlayer.jump();
                    break;
                }
                if (this.ticks >= 2 && this.ticks <= 8) {
                    Fly.mc.thePlayer.motionY += 0.07;
                }
                ++this.ticks;
            }
        }
    }

    @Listener
    public void onMove(MoveEvent event) {
        block86: {
            switch (this.mode.getMode()) {
                case "Vanilla": {
                    switch (this.vanillaMode.getMode()) {
                        case "Motion": {
                            MovementUtil.strafe(event, this.vanillaSpeed.getValue());
                            if (Fly.mc.gameSettings.keyBindJump.isKeyDown()) {
                                event.setY(this.vanillaVerticalSpeed.getValue());
                            } else if (Fly.mc.gameSettings.keyBindSneak.isKeyDown()) {
                                event.setY(-this.vanillaVerticalSpeed.getValue());
                            } else {
                                event.setY(0.0);
                            }
                            Fly.mc.thePlayer.motionY = 0.0;
                            break;
                        }
                        case "Creative": {
                            Fly.mc.thePlayer.capabilities.isFlying = true;
                        }
                    }
                    break;
                }
                case "Collision": {
                    if (!this.collisionMode.is("Airwalk")) break;
                    Fly.mc.thePlayer.motionY = 0.0;
                    event.setY(0.0);
                    break;
                }
                case "NCP": {
                    switch (this.ncpMode.getMode()) {
                        case "Old": {
                            if (Fly.mc.thePlayer.onGround) {
                                MovementUtil.jump(event);
                                MovementUtil.strafe(event, 0.58);
                                break;
                            }
                            Fly.mc.thePlayer.motionY = 1.0E-10;
                            event.setY(1.0E-10);
                            if (!MovementUtil.isMoving() || Fly.mc.thePlayer.isCollidedHorizontally || this.speed < 0.28) {
                                this.speed = 0.28;
                            }
                            MovementUtil.strafe(event, this.speed);
                            this.speed -= this.speed / 159.0;
                        }
                    }
                    break;
                }
                case "Velocity": {
                    switch (this.velocityMode.getMode()) {
                        case "Wait for hit": {
                            if (this.takingVelocity) {
                                Fly.mc.thePlayer.motionY = this.velocityY;
                                event.setY(Fly.mc.thePlayer.motionY);
                                Fly.mc.thePlayer.motionX = this.lastMotionX;
                                event.setX(Fly.mc.thePlayer.motionX);
                                Fly.mc.thePlayer.motionZ = this.lastMotionZ;
                                event.setZ(Fly.mc.thePlayer.motionZ);
                                this.notMoving = false;
                                this.ticks = 0;
                                break;
                            }
                            if (event.getY() < -0.3 && !this.notMoving) {
                                this.lastMotionX = event.getX();
                                this.lastMotionY = event.getY();
                                this.lastMotionZ = event.getZ();
                                this.notMoving = true;
                            }
                            if (this.notMoving) {
                                Fly.mc.thePlayer.motionY = 0.0;
                                event.setY(0.0);
                                MovementUtil.strafe(event, 0.0);
                            }
                            ++this.ticks;
                            break;
                        }
                        case "Bow": {
                            for (int i = 8; i >= 0; --i) {
                                ItemStack stack = Fly.mc.thePlayer.inventory.getStackInSlot(i);
                                if (stack == null || !(stack.getItem() instanceof ItemBow)) continue;
                                Fly.mc.thePlayer.inventory.currentItem = i;
                                break;
                            }
                            if (this.takingVelocity) {
                                Fly.mc.timer.timerSpeed = 1.0f;
                                this.notMoving = false;
                                this.ticks = 0;
                                this.counter = 0;
                                this.started = true;
                            } else {
                                if (this.ticks <= 3) {
                                    if (this.started) {
                                        Fly.mc.timer.timerSpeed = 1.5f;
                                    }
                                    Fly.mc.gameSettings.keyBindUseItem.pressed = true;
                                } else {
                                    Fly.mc.gameSettings.keyBindUseItem.pressed = false;
                                }
                                ++this.ticks;
                            }
                            if (this.ticks >= 6) {
                                Fly.mc.timer.timerSpeed = 0.03f;
                            } else if (this.ticks == 5) {
                                Fly.mc.timer.timerSpeed = 0.1f;
                            }
                            if (this.started && !this.notMoving && !this.takingVelocity && MovementUtil.getHorizontalMotion() > 0.07) {
                                break;
                            }
                            break block86;
                        }
                        case "Bow2": {
                            for (int i = 8; i >= 0; --i) {
                                ItemStack stack = Fly.mc.thePlayer.inventory.getStackInSlot(i);
                                if (stack == null || !(stack.getItem() instanceof ItemBow)) continue;
                                Fly.mc.thePlayer.inventory.currentItem = i;
                                break;
                            }
                            if (this.takingVelocity) {
                                boolean sameZDir;
                                Fly.mc.thePlayer.motionY = this.velocityY;
                                event.setY(Fly.mc.thePlayer.motionY);
                                boolean sameXDir = this.lastMotionX > 0.0 && this.velocityX > 0.0 || this.lastMotionX < 0.0 && this.velocityX < 0.0;
                                boolean bl = sameZDir = this.lastMotionZ > 0.0 && this.velocityZ > 0.0 || this.lastMotionZ < 0.0 && this.velocityZ < 0.0;
                                if (sameXDir && sameZDir) {
                                    Fly.mc.thePlayer.motionX = this.velocityX;
                                    event.setX(Fly.mc.thePlayer.motionX);
                                    Fly.mc.thePlayer.motionZ = this.velocityZ;
                                    event.setZ(Fly.mc.thePlayer.motionZ);
                                } else {
                                    Fly.mc.thePlayer.motionX = this.lastMotionX;
                                    event.setX(Fly.mc.thePlayer.motionX);
                                    Fly.mc.thePlayer.motionZ = this.lastMotionZ;
                                    event.setZ(Fly.mc.thePlayer.motionZ);
                                }
                                this.notMoving = false;
                                this.ticks = 0;
                                break;
                            }
                            if (this.ticks >= 6 && !this.notMoving) {
                                this.lastMotionX = event.getX();
                                this.lastMotionY = event.getY();
                                this.lastMotionZ = event.getZ();
                                this.notMoving = true;
                            }
                            Fly.mc.gameSettings.keyBindUseItem.pressed = this.ticks >= 1 && this.ticks <= 6;
                            if (this.notMoving) {
                                Fly.mc.thePlayer.motionY = 0.0;
                                event.setY(0.0);
                                MovementUtil.strafe(event, 0.0);
                            }
                            ++this.ticks;
                        }
                    }
                    break;
                }
                case "Blocksmc": {
                    if (this.automated.isEnabled() && ++this.counter < 6) {
                        float yaw = MathHelper.wrapAngleTo180_float(Fly.mc.thePlayer.rotationYaw);
                        double x = 0.0;
                        double z = 0.0;
                        EnumFacing facing = EnumFacing.UP;
                        if (yaw > 135.0f || yaw < -135.0f) {
                            z = 1.0;
                            facing = EnumFacing.NORTH;
                        } else if (yaw > -135.0f && yaw < -45.0f) {
                            x = -1.0;
                            facing = EnumFacing.EAST;
                        } else if (yaw > -45.0f && yaw < 45.0f) {
                            z = -1.0;
                            facing = EnumFacing.SOUTH;
                        } else if (yaw > 45.0f && yaw < 135.0f) {
                            x = 1.0;
                            facing = EnumFacing.WEST;
                        }
                        switch (this.counter) {
                            case 1: {
                                BlockPos pos = new BlockPos(Fly.mc.thePlayer.posX + x, Fly.mc.thePlayer.posY - 1.0, Fly.mc.thePlayer.posZ + z);
                                Fly.mc.playerController.onPlayerRightClick(Fly.mc.thePlayer, Fly.mc.theWorld, Fly.mc.thePlayer.getHeldItem(), pos, EnumFacing.UP, WorldUtil.getVec3(pos, EnumFacing.DOWN, true));
                                break;
                            }
                            case 2: {
                                BlockPos pos = new BlockPos(Fly.mc.thePlayer.posX + x, Fly.mc.thePlayer.posY, Fly.mc.thePlayer.posZ + z);
                                Fly.mc.playerController.onPlayerRightClick(Fly.mc.thePlayer, Fly.mc.theWorld, Fly.mc.thePlayer.getHeldItem(), pos, EnumFacing.UP, WorldUtil.getVec3(pos, EnumFacing.DOWN, true));
                                break;
                            }
                            case 3: {
                                BlockPos pos = new BlockPos(Fly.mc.thePlayer.posX + x, Fly.mc.thePlayer.posY + 1.0, Fly.mc.thePlayer.posZ + z);
                                Fly.mc.playerController.onPlayerRightClick(Fly.mc.thePlayer, Fly.mc.theWorld, Fly.mc.thePlayer.getHeldItem(), pos, EnumFacing.UP, WorldUtil.getVec3(pos, EnumFacing.DOWN, true));
                                break;
                            }
                            case 5: {
                                BlockPos pos = new BlockPos(Fly.mc.thePlayer.posX + x, Fly.mc.thePlayer.posY + 2.0, Fly.mc.thePlayer.posZ + z);
                                Fly.mc.playerController.onPlayerRightClick(Fly.mc.thePlayer, Fly.mc.theWorld, Fly.mc.thePlayer.getHeldItem(), pos, facing, WorldUtil.getVec3(pos, facing, true));
                            }
                        }
                        PacketUtil.sendPacket(new C0APacketAnimation());
                        MovementUtil.strafe(event, 0.04);
                        return;
                    }
                    BlockPos pos = new BlockPos(Fly.mc.thePlayer.posX, Fly.mc.thePlayer.posY + 2.0, Fly.mc.thePlayer.posZ);
                    if (Fly.mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir) {
                        this.started = true;
                    }
                    Acrimony.instance.getPacketBlinkHandler().startBlinking();
                    if (this.started) {
                        Fly.mc.timer.timerSpeed = 0.3f;
                        if (Fly.mc.thePlayer.onGround) {
                            if (this.ticks > 0) {
                                this.setEnabled(false);
                                return;
                            }
                            if (MovementUtil.isMoving()) {
                                MovementUtil.jump(event);
                                MovementUtil.strafe(event, 0.58);
                            }
                        } else if (this.ticks == 1) {
                            MovementUtil.strafe(event, 9.5);
                        }
                        ++this.ticks;
                        break;
                    }
                    MovementUtil.strafe(event, 0.1);
                    break;
                }
                case "Hypixel": {
                    if (this.veloTicks <= 0) break;
                    --this.veloTicks;
                    if (this.veloTicks != 0) break;
                    MovementUtil.strafe(event);
                }
            }
        }
        this.takingVelocity = false;
        ++this.ticksSinceVelocity;
    }

    @Listener
    public void onEntityAction(EntityActionEvent event) {
        switch (this.mode.getMode()) {
            case "Velocity": {
                if (this.velocityMode.is("Wait for hit")) {
                    event.setSprinting(true);
                    break;
                }
                if (!this.velocityMode.is("Airjump") || this.started) break;
                event.setSprinting(false);
                break;
            }
            case "Blocksmc": {
                if (!this.automated.isEnabled() || this.counter >= 6) break;
                event.setSprinting(false);
            }
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        switch (this.mode.getMode()) {
            case "Velocity": {
                if (!this.velocityMode.is("Bow") && !this.velocityMode.is("Bow2")) break;
                event.setPitch(-90.0f);
                break;
            }
            case "Collision": {
                if (!this.collisionMode.is("Airwalk")) break;
                event.setOnGround(true);
            }
        }
    }

    @Listener
    public void onVelocity(VelocityEvent event) {
        if (this.mode.is("Hypixel")) {
            if (MovementUtil.isMoving() && !Fly.mc.thePlayer.onGround) {
                this.veloTicks = 2;
                LogUtil.addChatMessage(String.valueOf(this.veloTicks));
            } else {
                event.setX(Fly.mc.thePlayer.motionX);
                event.setZ(Fly.mc.thePlayer.motionZ);
            }
        }
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity)event.getPacket();
            if (Fly.mc.thePlayer.getEntityId() == packet.getEntityID()) {
                this.takingVelocity = true;
                this.velocityX = (double)packet.getMotionX() / 8000.0;
                this.velocityY = (double)packet.getMotionY() / 8000.0;
                this.velocityZ = (double)packet.getMotionZ() / 8000.0;
                this.velocityDist = Math.hypot(this.velocityX, this.velocityZ);
                this.ticksSinceVelocity = 0;
                if (this.mode.is("Velocity")) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getPacket() instanceof S08PacketPlayerPosLook && this.mode.is("Velocity")) {
            this.setEnabled(false);
            return;
        }
    }

    @Listener
    public void onSend(PacketSendEvent event) {
        switch (this.mode.getMode()) {
            case "Velocity": {
                if (!this.velocityMode.is("Wait for hit") && !this.velocityMode.is("Bow2") || !(event.getPacket() instanceof C03PacketPlayer) || !this.notMoving) break;
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getSuffix() {
        return this.mode.getMode();
    }
}

