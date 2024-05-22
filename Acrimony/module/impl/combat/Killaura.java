/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.combat;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.ItemRenderEvent;
import Acrimony.event.impl.JumpEvent;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.PostMotionEvent;
import Acrimony.event.impl.Render3DEvent;
import Acrimony.event.impl.RenderEvent;
import Acrimony.event.impl.SlowdownEvent;
import Acrimony.event.impl.StrafeEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Antibot;
import Acrimony.module.impl.combat.Teams;
import Acrimony.module.impl.combat.Velocity;
import Acrimony.module.impl.movement.Speed;
import Acrimony.module.impl.player.Antivoid;
import Acrimony.module.impl.player.Breaker;
import Acrimony.module.impl.player.Scaffold;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.misc.DeltaTime;
import Acrimony.util.misc.LogUtil;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.player.FixedRotations;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.RotationsUtil;
import Acrimony.util.render.RenderUtil;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.fixes.AttackOrder;
import org.lwjgl.opengl.GL11;

public class Killaura
extends Module {
    private double time;
    public boolean down;
    private EntityLivingBase target;
    public final ModeSetting mode = new ModeSetting("Mode", "Single", "Single", "Switch", "Fast Switch");
    private final ModeSetting filter = new ModeSetting("Filter", "Range", "Range", "Health");
    private final ModeSetting rotations = new ModeSetting("Rotations", "Normal", "Normal", "Randomised", "Smooth", "None");
    private final DoubleSetting randomAmount = new DoubleSetting("Random amount", () -> this.mode.is("Randomised"), 4.0, 0.25, 10.0, 0.25);
    public final DoubleSetting startingRange = new DoubleSetting("Starting range", 4.0, 3.0, 6.0, 0.05);
    public final DoubleSetting range = new DoubleSetting("Range", 4.0, 3.0, 6.0, 0.05);
    public final DoubleSetting rotationRange = new DoubleSetting("Rotation range", 4.0, 3.0, 6.0, 0.05);
    private final ModeSetting raycast = new ModeSetting("Raycast", "Disabled", "Disabled", "Normal", "Legit");
    private final ModeSetting attackDelayMode = new ModeSetting("Attack delay mode", "APS", "APS", "Delay in ticks");
    private final IntegerSetting minAPS = new IntegerSetting("Min APS", () -> this.attackDelayMode.is("APS"), 10, 1, 20, 1);
    private final IntegerSetting maxAPS = new IntegerSetting("Max APS", () -> this.attackDelayMode.is("APS"), 10, 1, 20, 1);
    private final IntegerSetting attackDelay = new IntegerSetting("Attack delay", () -> this.attackDelayMode.is("Delay in ticks"), 2, 1, 20, 1);
    private final IntegerSetting failRate = new IntegerSetting("Fail rate", 0, 0, 30, 1);
    private final IntegerSetting hurtTime = new IntegerSetting("Hurt time", 10, 0, 10, 1);
    public final ModeSetting autoblock = new ModeSetting("Autoblock", "Fake", "Vanilla", "NCP", "AAC5", "Spoof", "Spoof2", "Blink", "Not moving", "Fake", "Watchdog", "None");
    private final BooleanSetting noHitOnFirstTick = new BooleanSetting("No hit on first tick", () -> this.autoblock.is("Vanilla"), false);
    private final ModeSetting blockTiming = new ModeSetting("Block timing", () -> this.autoblock.is("Spoof") || this.autoblock.is("Spoof2"), "Post", "Pre", "Post");
    private final IntegerSetting blockHurtTime = new IntegerSetting("Block hurt time", () -> this.autoblock.is("Spoof") || this.autoblock.is("Spoof2") || this.autoblock.is("Blink"), 5, 0, 10, 1);
    private final BooleanSetting whileTargetNotLooking = new BooleanSetting("While target not looking", () -> this.autoblock.is("Blink"), true);
    private final ModeSetting slowdown = new ModeSetting("Slowdown", () -> this.autoblock.is("Blink"), "Enabled", "Enabled", "Onground", "Offground", "Disabled");
    private final IntegerSetting blinkTicks = new IntegerSetting("Blink ticks", () -> this.autoblock.is("Blink"), 5, 3, 10, 1);
    private final BooleanSetting whileHitting = new BooleanSetting("While hitting", () -> this.autoblock.is("Not moving"), false);
    private final BooleanSetting whileSpeedEnabled = new BooleanSetting("While speed enabled", () -> !this.autoblock.is("None") && !this.autoblock.is("Fake"), true);
    private final ModeSetting moveFix = new ModeSetting("Move fix", "Disabled", "Disabled", "Normal", "Silent");
    private final BooleanSetting delayTransactions = new BooleanSetting("Delay transactions", false);
    private final BooleanSetting whileInventoryOpened = new BooleanSetting("While inventory", false);
    private final BooleanSetting whileScaffoldEnabled = new BooleanSetting("While scaffold", false);
    private final BooleanSetting whileUsingBreaker = new BooleanSetting("While using BedNuker", false);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", false);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", false);
    private final BooleanSetting attackDead = new BooleanSetting("Attack dead", false);
    private final ModeSetting visualiseTarget = new ModeSetting("TargetESP", "BOX", "BOX", "Jello", "NONE");
    private boolean hadTarget;
    private ClientTheme theme;
    private FixedRotations fixedRotations;
    private double random;
    private boolean attackNextTick;
    private double rotSpeed;
    private boolean done;
    public static boolean blocking;
    public static boolean fakeBlocking;
    private int autoblockTicks;
    private int attackCounter;
    private Antibot antibotModule;
    private Teams teamsModule;
    private Speed speedModule;
    private Scaffold scaffoldModule;
    private Breaker breakerModule;
    private Antivoid antivoidModule;
    private Velocity velocityModule;
    private boolean couldBlock;
    private boolean blinking;
    private int lastSlot;
    private final TimerUtil attackTimer = new TimerUtil();

    public Killaura() {
        super("Killaura", Category.COMBAT);
        this.addSettings(this.mode, this.filter, this.rotations, this.randomAmount, this.startingRange, this.range, this.rotationRange, this.raycast, this.attackDelayMode, this.minAPS, this.maxAPS, this.attackDelay, this.failRate, this.hurtTime, this.autoblock, this.noHitOnFirstTick, this.blockTiming, this.blinkTicks, this.blockHurtTime, this.whileTargetNotLooking, this.slowdown, this.whileHitting, this.whileSpeedEnabled, this.moveFix, this.delayTransactions, this.whileInventoryOpened, this.whileScaffoldEnabled, this.whileUsingBreaker, this.players, this.animals, this.monsters, this.invisibles, this.attackDead, this.visualiseTarget);
    }

    @Override
    public void onEnable() {
        this.fixedRotations = new FixedRotations(Killaura.mc.thePlayer.rotationYaw, Killaura.mc.thePlayer.rotationPitch);
        this.rotSpeed = 15.0;
        this.done = false;
        this.random = 0.5;
        this.attackNextTick = false;
        this.couldBlock = false;
    }

    @Override
    public void onDisable() {
        fakeBlocking = false;
        if (Killaura.mc.thePlayer != null) {
            if (this.hadTarget && this.rotations.is("Smooth")) {
                Killaura.mc.thePlayer.rotationYaw = this.fixedRotations.getYaw();
            }
            this.stopTargeting();
        }
        if (!Scaffold.isSpoofing) {
            Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
        }
        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
    }

    private void stopTargeting() {
        this.target = null;
        this.releaseBlocking();
        this.hadTarget = false;
        this.attackCounter = this.attackDelay.getValue();
        this.attackNextTick = false;
        if (this.delayTransactions.isEnabled()) {
            Acrimony.instance.getPacketDelayHandler().stopAll();
        }
    }

    @Listener
    public void onRender(RenderEvent event) {
        if (Killaura.mc.thePlayer == null || Killaura.mc.thePlayer.ticksExisted < 10) {
            this.setEnabled(false);
            return;
        }
        if (this.target != null && this.attackDelayMode.is("APS")) {
            long delay1 = (long)(1000.0 / (double)this.minAPS.getValue());
            long delay2 = (long)(1000.0 / (double)this.maxAPS.getValue());
            delay1 = Math.max(delay1, delay2);
            long delay = (long)((double)delay2 + (double)(delay1 - delay2) * this.random);
            if (this.attackTimer.getTimeElapsed() >= delay) {
                this.attackNextTick = true;
                this.attackTimer.reset();
            }
        }
    }

    @Override
    public void onClientStarted() {
        this.antibotModule = Acrimony.instance.getModuleManager().getModule(Antibot.class);
        this.speedModule = Acrimony.instance.getModuleManager().getModule(Speed.class);
        this.teamsModule = Acrimony.instance.getModuleManager().getModule(Teams.class);
        this.scaffoldModule = Acrimony.instance.getModuleManager().getModule(Scaffold.class);
        this.breakerModule = Acrimony.instance.getModuleManager().getModule(Breaker.class);
        this.antivoidModule = Acrimony.instance.getModuleManager().getModule(Antivoid.class);
        this.velocityModule = Acrimony.instance.getModuleManager().getModule(Velocity.class);
    }

    @Listener
    public void onTick(TickEvent event) {
        boolean shouldBlock;
        boolean usingBreaker;
        if (Killaura.mc.thePlayer.ticksExisted < 10) {
            this.setEnabled(false);
            return;
        }
        this.random = Math.random();
        switch (this.mode.getMode()) {
            case "Single": {
                if (this.target != null && this.canAttack(this.target)) break;
                this.target = this.findTarget(true);
                break;
            }
            case "Switch": {
                this.target = this.findTarget(true);
                break;
            }
            case "Fast Switch": {
                this.target = this.findTarget(false);
            }
        }
        this.getRotations();
        boolean inventoryOpened = Killaura.mc.currentScreen instanceof GuiContainer && !this.whileInventoryOpened.isEnabled();
        boolean scaffoldEnabled = this.scaffoldModule.isEnabled() && !this.whileScaffoldEnabled.isEnabled();
        boolean bl = usingBreaker = this.breakerModule.isEnabled() && this.breakerModule.isBreakingBed() && !this.whileUsingBreaker.isEnabled();
        if (this.target == null || inventoryOpened || scaffoldEnabled || usingBreaker) {
            this.stopTargeting();
            this.couldBlock = false;
            fakeBlocking = false;
            return;
        }
        boolean attackTick = false;
        double d = this.getDistanceToEntity(this.target);
        double d2 = this.hadTarget ? this.range.getValue() : this.startingRange.getValue();
        if (d <= d2) {
            if (this.target.hurtTime <= this.hurtTime.getValue()) {
                switch (this.attackDelayMode.getMode()) {
                    case "APS": {
                        if (!this.hadTarget) {
                            attackTick = true;
                            this.attackTimer.reset();
                            break;
                        }
                        if (!this.attackNextTick) break;
                        attackTick = true;
                        this.attackNextTick = false;
                        break;
                    }
                    case "Delay in ticks": {
                        if (++this.attackCounter < this.attackDelay.getValue()) break;
                        attackTick = true;
                    }
                }
            }
            if (this.delayTransactions.isEnabled()) {
                Acrimony.instance.getPacketDelayHandler().startDelayingPing(2000L);
            }
            this.hadTarget = true;
        } else {
            this.hadTarget = false;
        }
        this.couldBlock = shouldBlock = this.canBlock();
        if (shouldBlock) {
            fakeBlocking = true;
            if (!this.autoblockAllowAttack()) {
                attackTick = false;
            }
            this.beforeAttackAutoblock(attackTick);
        } else {
            if (blocking) {
                attackTick = false;
            }
            this.releaseBlocking();
        }
        if (attackTick) {
            boolean canAttack = true;
            if (!this.raycast.is("Disabled")) {
                canAttack = this.raycast.is("Legit") ? RotationsUtil.raycastEntity(this.target, this.fixedRotations.getYaw(), this.fixedRotations.getPitch(), this.fixedRotations.getLastYaw(), this.fixedRotations.getLastPitch(), this.range.getValue() + 0.3) : RotationsUtil.raycastEntity(this.target, this.fixedRotations.getYaw(), this.fixedRotations.getPitch(), this.fixedRotations.getYaw(), this.fixedRotations.getPitch(), this.range.getValue() + 0.3);
            }
            double aaa = (double)this.failRate.getValue() / 100.0;
            if (Math.random() > 1.0 - aaa) {
                canAttack = false;
            }
            if (canAttack) {
                AttackOrder.sendFixedAttack(Killaura.mc.thePlayer, this.target);
            }
            this.attackCounter = 0;
        }
        if (shouldBlock) {
            this.afterAttackAutoblock(attackTick);
        } else {
            this.releaseBlocking();
            this.autoblockTicks = 0;
        }
        Killaura.mc.gameSettings.keyBindAttack.pressed = false;
        if (!this.autoblock.is("None") && !this.autoblock.is("Blink")) {
            Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
        }
        if (!this.rotations.is("None") && this.isRotating() && this.moveFix.is("Silent")) {
            float value;
            float diff = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(this.fixedRotations.getYaw()) - MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection())) + 22.5f;
            if (diff < 0.0f) {
                diff = 360.0f + diff;
            }
            int a = (int)((double)diff / 45.0);
            float forward = value = Killaura.mc.thePlayer.moveForward != 0.0f ? Math.abs(Killaura.mc.thePlayer.moveForward) : Math.abs(Killaura.mc.thePlayer.moveStrafing);
            float strafe = 0.0f;
            for (int i = 0; i < 8 - a; ++i) {
                float[] dirs = MovementUtil.incrementMoveDirection(forward, strafe);
                forward = dirs[0];
                strafe = dirs[1];
            }
            if (forward < 0.8f) {
                Killaura.mc.gameSettings.keyBindSprint.pressed = false;
                Killaura.mc.thePlayer.setSprinting(false);
            }
        }
    }

    @Listener
    public void onSlowdown(SlowdownEvent event) {
        block18: {
            if (!this.canBlock()) break block18;
            block3 : switch (this.autoblock.getMode()) {
                case "Blink": {
                    switch (this.slowdown.getMode()) {
                        case "Onground": {
                            if (!Killaura.mc.thePlayer.onGround) {
                                event.setAllowedSprinting(true);
                                event.setForward(1.0f);
                                event.setStrafe(1.0f);
                                break block3;
                            }
                            break block18;
                        }
                        case "Offground": {
                            if (Killaura.mc.thePlayer.onGround) {
                                event.setAllowedSprinting(true);
                                event.setForward(1.0f);
                                event.setStrafe(1.0f);
                                break block3;
                            }
                            break block18;
                        }
                        case "Disabled": {
                            event.setAllowedSprinting(true);
                            event.setForward(1.0f);
                            event.setStrafe(1.0f);
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onJump(JumpEvent event) {
        if (this.target != null && !this.rotations.is("None") && !this.moveFix.is("Disabled")) {
            event.setYaw(this.fixedRotations.getYaw());
        }
    }

    @Listener
    public void onStrafe(StrafeEvent event) {
        if (!this.rotations.is("None") && this.isRotating()) {
            switch (this.moveFix.getMode()) {
                case "Normal": {
                    event.setYaw(this.fixedRotations.getYaw());
                    break;
                }
                case "Silent": {
                    float value;
                    event.setYaw(this.fixedRotations.getYaw());
                    float diff = MathHelper.wrapAngleTo180_float(MathHelper.wrapAngleTo180_float(this.fixedRotations.getYaw()) - MathHelper.wrapAngleTo180_float(MovementUtil.getPlayerDirection())) + 22.5f;
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
                }
            }
        }
    }

    private boolean canRenderBlocking() {
        return this.canBlock() || this.autoblock.is("Fake");
    }

    private boolean canBlock() {
        fakeBlocking = true;
        ItemStack stack = Killaura.mc.thePlayer.getHeldItem();
        if (this.autoblock.is("Blink")) {
            if (this.antivoidModule.isBlinking()) {
                return false;
            }
            if (Killaura.mc.thePlayer.hurtTime > this.blockHurtTime.getValue()) {
                return false;
            }
            if (this.target != null && !this.whileTargetNotLooking.isEnabled()) {
                boolean targetLooking;
                float targetYaw = MathHelper.wrapAngleTo180_float(this.target.rotationYaw);
                float diff = Math.abs(MathHelper.wrapAngleTo180_float(Killaura.mc.thePlayer.rotationYaw) - targetYaw);
                boolean bl = targetLooking = diff > 90.0f && diff < 270.0f || (double)Killaura.mc.thePlayer.getDistanceToEntity(this.target) < 1.3;
                if (!targetLooking) {
                    return false;
                }
            }
        }
        if (this.autoblock.is("Spoof") || this.autoblock.is("Spoof2")) {
            if (Killaura.mc.thePlayer.hurtTime > this.blockHurtTime.getValue()) {
                return false;
            }
            if (this.autoblock.is("Spoof2") && this.target != null) {
                return true;
            }
        }
        return this.target != null && stack != null && stack.getItem() instanceof ItemSword && (this.whileSpeedEnabled.isEnabled() || !Acrimony.instance.getModuleManager().getModule(Speed.class).isEnabled());
    }

    private void beforeAttackAutoblock(boolean attackTick) {
        int slot = Killaura.mc.thePlayer.inventory.currentItem;
        switch (this.autoblock.getMode()) {
            case "Vanilla": {
                if (!blocking) {
                    Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                    blocking = true;
                }
                ++this.autoblockTicks;
                break;
            }
            case "NCP": {
                if (!blocking) break;
                Acrimony.util.network.PacketUtil.releaseUseItem(true);
                blocking = false;
                break;
            }
            case "Spoof": {
                Acrimony.util.network.PacketUtil.sendPacket(new C09PacketHeldItemChange(slot < 8 ? slot + 1 : 0));
                Acrimony.util.network.PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                if (!this.blockTiming.is("Pre")) break;
                Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                blocking = true;
                break;
            }
            case "Spoof2": {
                if (this.autoblockTicks >= 2) {
                    Killaura.mc.thePlayer.inventory.currentItem = this.lastSlot;
                    Killaura.mc.playerController.syncCurrentPlayItem();
                    Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
                    if (this.blinking) {
                        Acrimony.instance.getPacketBlinkHandler().releasePackets();
                    }
                    this.autoblockTicks = 0;
                }
                if (this.autoblockTicks == 0) {
                    if (!this.blockTiming.is("Pre")) break;
                    Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                    blocking = true;
                    break;
                }
                if (this.autoblockTicks != 1) break;
                if (!(this.velocityModule.isEnabled() && this.mode.is("Hypixel") && this.speedModule.isEnabled())) {
                    Acrimony.instance.getPacketBlinkHandler().startBlinking();
                    this.blinking = true;
                }
                this.lastSlot = slot;
                Acrimony.instance.getSlotSpoofHandler().startSpoofing(slot);
                Killaura.mc.thePlayer.inventory.currentItem = slot < 8 ? slot + 1 : 0;
                break;
            }
            case "Blink": {
                if (this.autoblockTicks > 0 && this.autoblockTicks < this.blinkTicks.getValue()) {
                    Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
                }
                if (this.autoblockTicks == this.blinkTicks.getValue() || this.autoblockTicks == 0) {
                    Killaura.mc.gameSettings.keyBindUseItem.pressed = true;
                    this.autoblockTicks = 0;
                }
                fakeBlocking = true;
                blocking = true;
                break;
            }
            case "Not moving": {
                if (!MovementUtil.isMoving() && (this.target.hurtTime >= this.hurtTime.getValue() + 1 || this.whileHitting.isEnabled())) break;
                Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
                blocking = false;
            }
        }
    }

    private void afterAttackAutoblock(boolean attackTick) {
        switch (this.autoblock.getMode()) {
            case "AAC5": {
                Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                blocking = true;
                break;
            }
            case "Watchdog": {
                if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_12_2)) {
                    Acrimony.util.network.PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, Killaura.mc.thePlayer.inventory.getCurrentItem(), 0.0f, 0.0f, 0.0f));
                    PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                    useItem.write(Type.VAR_INT, 1);
                    PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                }
                blocking = true;
            }
        }
    }

    private void postAutoblock() {
        switch (this.autoblock.getMode()) {
            case "NCP": {
                if (blocking) break;
                Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                blocking = true;
                break;
            }
            case "Spoof": {
                if (!this.blockTiming.is("Post")) break;
                Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                blocking = true;
                break;
            }
            case "Spoof2": {
                if (this.blockTiming.is("Post")) {
                    Acrimony.util.network.PacketUtil.sendBlocking(true, false);
                    blocking = true;
                }
                ++this.autoblockTicks;
                break;
            }
            case "Not moving": {
                if (MovementUtil.isMoving() || this.target.hurtTime < this.hurtTime.getValue() + 1 && !this.whileHitting.isEnabled()) break;
                Killaura.mc.gameSettings.keyBindUseItem.pressed = true;
                blocking = true;
                break;
            }
            case "Blink": {
                if (this.target == null) {
                    LogUtil.addChatMessage("Autoblock test 2");
                }
                if (this.autoblockTicks == 0) {
                    Acrimony.instance.getPacketBlinkHandler().releasePackets();
                    Acrimony.instance.getPacketBlinkHandler().startBlinking();
                }
                ++this.autoblockTicks;
                this.blinking = true;
            }
        }
    }

    private boolean autoblockAllowAttack() {
        switch (this.autoblock.getMode()) {
            case "Vanilla": {
                return this.noHitOnFirstTick.isEnabled() ? this.autoblockTicks > 1 : true;
            }
            case "Spoof2": {
                return this.autoblockTicks == 2;
            }
            case "Blink": {
                return this.autoblockTicks >= 2 && this.autoblockTicks < this.blinkTicks.getValue();
            }
        }
        return true;
    }

    private void releaseBlocking() {
        fakeBlocking = false;
        ItemStack stack = Killaura.mc.thePlayer.getHeldItem();
        if (this.hadTarget && this.autoblock.is("Blink") && !blocking && this.target == null) {
            LogUtil.addChatMessage("Autoblock test : " + Acrimony.instance.getPacketBlinkHandler().isBlinking());
        }
        int slot = Killaura.mc.thePlayer.inventory.currentItem;
        if (blocking) {
            switch (this.autoblock.getMode()) {
                case "Vanilla": 
                case "NCP": 
                case "AAC5": {
                    if (stack == null || !(stack.getItem() instanceof ItemSword)) break;
                    Acrimony.util.network.PacketUtil.releaseUseItem(true);
                    break;
                }
                case "Spoof": {
                    Acrimony.util.network.PacketUtil.sendPacket(new C09PacketHeldItemChange(slot < 8 ? slot + 1 : 0));
                    Acrimony.util.network.PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                    break;
                }
                case "Watchdog": {
                    if (!ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_12_2)) break;
                    Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
                    Acrimony.util.network.PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    break;
                }
                case "Spoof2": {
                    if (this.autoblockTicks == 1) {
                        Killaura.mc.thePlayer.inventory.currentItem = this.lastSlot < 8 ? this.lastSlot + 1 : 0;
                        new Thread(() -> {
                            try {
                                Thread.sleep(40L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Killaura.mc.thePlayer.inventory.currentItem = this.lastSlot;
                            Killaura.mc.playerController.syncCurrentPlayItem();
                        }).start();
                    } else {
                        Killaura.mc.thePlayer.inventory.currentItem = this.lastSlot;
                    }
                    Killaura.mc.playerController.syncCurrentPlayItem();
                    Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
                    if (!this.blinking) break;
                    Acrimony.instance.getPacketBlinkHandler().stopBlinking();
                    this.blinking = false;
                    break;
                }
                case "Not moving": {
                    Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
                }
            }
            blocking = false;
        }
        if (this.autoblock.is("Blink") && (this.blinking || blocking)) {
            Acrimony.instance.getPacketBlinkHandler().stopBlinking();
            this.blinking = false;
            blocking = false;
            Killaura.mc.gameSettings.keyBindUseItem.pressed = false;
        }
        this.autoblockTicks = 0;
        fakeBlocking = false;
    }

    @Listener
    public void onItemRender(ItemRenderEvent event) {
        if (this.canRenderBlocking() && (blocking || !this.autoblock.is("Not moving")) && !this.autoblock.is("None")) {
            event.setRenderBlocking(true);
        }
    }

    private void getRotations() {
        float yaw = this.fixedRotations.getYaw();
        float pitch = this.fixedRotations.getPitch();
        if (this.target != null) {
            float[] rots = RotationsUtil.getRotationsToEntity(this.target, false);
            if (this.speedModule.isEnabled() && this.speedModule.mode.is("Pathfind")) {
                rots = RotationsUtil.getRotationsToEntity(this.speedModule.getActualX(), this.speedModule.getActualY(), this.speedModule.getActualZ(), this.target, false);
            }
            switch (this.rotations.getMode()) {
                case "Normal": {
                    yaw = rots[0];
                    pitch = rots[1];
                    break;
                }
                case "Randomised": {
                    double amount = this.randomAmount.getValue();
                    yaw = (float)((double)rots[0] + Math.random() * amount - amount / 2.0);
                    pitch = (float)((double)rots[1] + Math.random() * amount - amount / 2.0);
                    break;
                }
                case "Smooth": {
                    float yaw1 = rots[0];
                    float currentYaw = MathHelper.wrapAngleTo180_float(yaw);
                    float diff = Math.abs(currentYaw - yaw1);
                    if (diff >= 8.0f) {
                        if (diff > 35.0f) {
                            this.rotSpeed += 4.0 - Math.random();
                            this.rotSpeed = Math.max(this.rotSpeed, (double)((float)(31.0 - Math.random())));
                        } else {
                            this.rotSpeed -= 6.5 - Math.random();
                            this.rotSpeed = Math.max(this.rotSpeed, (double)((float)(14.0 - Math.random())));
                        }
                        yaw = diff <= 180.0f ? (currentYaw > yaw1 ? (float)((double)yaw - this.rotSpeed) : (float)((double)yaw + this.rotSpeed)) : (currentYaw > yaw1 ? (float)((double)yaw + this.rotSpeed) : (float)((double)yaw - this.rotSpeed));
                    } else {
                        yaw = currentYaw > yaw1 ? (float)((double)yaw - (double)diff * 0.8) : (float)((double)yaw + (double)diff * 0.8);
                    }
                    yaw = (float)((double)yaw + (Math.random() * 0.7 - 0.35));
                    pitch = (float)((double)Killaura.mc.thePlayer.rotationPitch + (double)(rots[1] - Killaura.mc.thePlayer.rotationPitch) * 0.6);
                    pitch = (float)((double)pitch + (Math.random() * 0.5 - 0.25));
                    this.done = false;
                }
            }
        } else {
            switch (this.rotations.getMode()) {
                case "Smooth": {
                    this.rotSpeed = 15.0;
                    if (this.hadTarget) break;
                    this.done = true;
                }
            }
        }
        this.fixedRotations.updateRotations(yaw, pitch);
    }

    private boolean isRotating() {
        switch (this.rotations.getMode()) {
            case "Normal": 
            case "Randomised": {
                return this.target != null;
            }
            case "Smooth": {
                return this.target != null || !this.done;
            }
            case "None": {
                return false;
            }
        }
        return false;
    }

    @Listener
    public void onMotion(MotionEvent event) {
        if (this.isRotating()) {
            event.setYaw(this.fixedRotations.getYaw());
            event.setPitch(this.fixedRotations.getPitch());
        }
    }

    @Listener
    public void onPostMotion(PostMotionEvent event) {
        if (this.couldBlock) {
            this.postAutoblock();
        }
    }

    public EntityLivingBase findTarget(boolean allowSame) {
        return this.findTarget(allowSame, this.rotationRange.getValue());
    }

    public EntityLivingBase findTarget(boolean allowSame, double range) {
        ArrayList<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
        for (Entity entity2 : Killaura.mc.theWorld.loadedEntityList) {
            if (!(entity2 instanceof EntityLivingBase) || entity2 == Killaura.mc.thePlayer || !this.canAttack((EntityLivingBase)entity2, range)) continue;
            entities.add((EntityLivingBase)entity2);
        }
        if (entities != null && entities.size() > 0) {
            switch (this.filter.getMode()) {
                case "Range": {
                    entities.sort(Comparator.comparingDouble(entity -> entity.getDistanceToEntity(Killaura.mc.thePlayer)));
                    break;
                }
                case "Health": {
                    entities.sort(Comparator.comparingDouble(entity -> entity.getHealth()));
                }
            }
            if (!allowSame && entities.size() > 1 && entities.get(0) == this.target) {
                return (EntityLivingBase)entities.get(1);
            }
            return (EntityLivingBase)entities.get(0);
        }
        return null;
    }

    public boolean canAttack(EntityLivingBase entity) {
        return this.canAttack(entity, this.rotationRange.getValue());
    }

    public boolean canAttack(EntityLivingBase entity, double range) {
        if (this.getDistanceToEntity(entity) > range) {
            return false;
        }
        if ((entity.isInvisible() || entity.isInvisibleToPlayer(Killaura.mc.thePlayer)) && !this.invisibles.isEnabled()) {
            return false;
        }
        if (!(!(entity instanceof EntityPlayer) || this.players.isEnabled() && this.teamsModule.canAttack((EntityPlayer)entity))) {
            return false;
        }
        if (entity instanceof EntityAnimal && !this.animals.isEnabled()) {
            return false;
        }
        if (entity instanceof EntityMob && !this.monsters.isEnabled()) {
            return false;
        }
        if (!(entity instanceof EntityPlayer || entity instanceof EntityAnimal || entity instanceof EntityMob)) {
            return false;
        }
        if (entity.isDead && !this.attackDead.isEnabled()) {
            return false;
        }
        return this.antibotModule.canAttack(entity, this);
    }

    public double getDistanceToEntity(EntityLivingBase entity) {
        double yDiff;
        Vec3 playerVec = new Vec3(Killaura.mc.thePlayer.posX, Killaura.mc.thePlayer.posY + (double)Killaura.mc.thePlayer.getEyeHeight(), Killaura.mc.thePlayer.posZ);
        if (this.speedModule.isEnabled() && this.speedModule.mode.is("Pathfind")) {
            playerVec = new Vec3(this.speedModule.getActualX(), this.speedModule.getActualY() + (double)Killaura.mc.thePlayer.getEyeHeight(), this.speedModule.getActualZ());
        }
        double targetY = (yDiff = Killaura.mc.thePlayer.posY - entity.posY) > 0.0 ? entity.posY + (double)entity.getEyeHeight() : (-yDiff < (double)Killaura.mc.thePlayer.getEyeHeight() ? Killaura.mc.thePlayer.posY + (double)Killaura.mc.thePlayer.getEyeHeight() : entity.posY);
        Vec3 targetVec = new Vec3(entity.posX, targetY, entity.posZ);
        return playerVec.distanceTo(targetVec) - (double)0.3f;
    }

    public double getDistanceCustomPosition(double x, double y, double z, double eyeHeight) {
        Vec3 playerVec = new Vec3(Killaura.mc.thePlayer.posX, Killaura.mc.thePlayer.posY + (double)Killaura.mc.thePlayer.getEyeHeight(), Killaura.mc.thePlayer.posZ);
        double yDiff = Killaura.mc.thePlayer.posY - y;
        double targetY = yDiff > 0.0 ? y + eyeHeight : (-yDiff < (double)Killaura.mc.thePlayer.getEyeHeight() ? Killaura.mc.thePlayer.posY + (double)Killaura.mc.thePlayer.getEyeHeight() : y);
        Vec3 targetVec = new Vec3(x, targetY, z);
        return playerVec.distanceTo(targetVec) - (double)0.3f;
    }

    @Listener
    public void onRender3D(Render3DEvent event) {
        switch (this.visualiseTarget.getMode()) {
            case "BOX": {
                if (this.target == null) break;
                double posX = this.target.lastTickPosX + (this.target.posX - this.target.lastTickPosX) * (double)event.getPartialTicks() - Killaura.mc.getRenderManager().renderPosX;
                double posY = this.target.lastTickPosY + (this.target.posY - this.target.lastTickPosY) * (double)event.getPartialTicks() - Killaura.mc.getRenderManager().renderPosY;
                double posZ = this.target.lastTickPosZ + (this.target.posZ - this.target.lastTickPosZ) * (double)event.getPartialTicks() - Killaura.mc.getRenderManager().renderPosZ;
                RenderUtil.drawSoiledEntityESP(posX, posY, posZ, this.target.width / 1.2f, (double)this.target.height + 0.2, this.target.hurtTime > 0 ? new Color(255, 0, 0, 80).getRGB() : new Color(0, 197, 3, 30).getRGB());
                break;
            }
            case "Jello": {
                if (this.target == null || !this.hadTarget) break;
                this.renderJello(this.target);
            }
        }
    }

    public void renderJello(EntityLivingBase e) {
        int j;
        this.time += 0.015 * ((double)DeltaTime.getDeltaTime() * 0.1);
        double height = 0.5 * (1.0 + Math.sin(Math.PI * 2 * (this.time * 0.6)));
        if (height > 0.995) {
            this.down = true;
        } else if (height < 0.01) {
            this.down = false;
        }
        double x = e.posX + (e.posX - e.lastTickPosX) * (double)Killaura.mc.timer.renderPartialTicks - Killaura.mc.getRenderManager().renderPosX;
        double y = e.posY + (e.posY - e.lastTickPosY) * (double)Killaura.mc.timer.renderPartialTicks - Killaura.mc.getRenderManager().renderPosY;
        double z = e.posZ + (e.posZ - e.lastTickPosZ) * (double)Killaura.mc.timer.renderPartialTicks - Killaura.mc.getRenderManager().renderPosZ;
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(2.0f);
        GL11.glShadeModel(7425);
        GL11.glDisable(2884);
        double size = e.width;
        double yOffset = ((double)e.height + 0.2) * height;
        GL11.glBegin(5);
        for (j = 0; j < 361; ++j) {
            Killaura.glColor(new Color(255, 255, 255, 100), (int)(!this.down ? 255.0 * height : 255.0 * (1.0 - height)));
            GL11.glVertex3d(x + Math.cos(Math.toRadians(j)) * size, y + yOffset, z - Math.sin(Math.toRadians(j)) * size);
            Killaura.glColor(new Color(255, 255, 255, 10), (int)(!this.down ? 255.0 * height : 255.0 * (1.0 - height)));
            GL11.glVertex3d(x + Math.cos(Math.toRadians(j)) * size, y + yOffset + (!this.down ? -0.5 * (1.0 - height) : 0.5 * height), z - Math.sin(Math.toRadians(j)) * size);
        }
        GL11.glEnd();
        GL11.glBegin(2);
        for (j = 0; j < 361; ++j) {
            Killaura.glColor(new Color(255, 255, 255, 180), (int)(!this.down ? 255.0 * height : 255.0 * (1.0 - height)));
            GL11.glVertex3d(x + Math.cos(Math.toRadians(j)) * size, y + yOffset, z - Math.sin(Math.toRadians(j)) * size);
        }
        GL11.glEnd();
        GlStateManager.enableAlpha();
        GL11.glShadeModel(7424);
        GL11.glDisable(2848);
        GL11.glEnable(2884);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void glColor(Color color, int i) {
        GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    @Override
    public String getSuffix() {
        return this.mode.getMode() + "," + this.autoblock.getMode();
    }

    public EntityLivingBase getTarget() {
        return this.target;
    }
}

