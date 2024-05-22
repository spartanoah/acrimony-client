/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.combat;

import Acrimony.event.Listener;
import Acrimony.event.impl.MotionEvent;
import Acrimony.event.impl.MoveEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.event.impl.VelocityEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.misc.KeyboardUtil;
import Acrimony.util.network.PacketUtil;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.RotationsUtil;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;

public class Velocity
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Cancel", "Cancel", "Packet cancel", "Edit", "Ignore horizontal", "Delayed", "Strafe", "Legit", "Hypixel", "MMC", "Block collide", "Ground collision", "Spoof offground");
    private final IntegerSetting horizontal = new IntegerSetting("Horizontal", () -> this.mode.is("Edit") || this.mode.is("Delayed"), 0, 0, 100, 2);
    private final IntegerSetting vertical = new IntegerSetting("Vertical", () -> this.mode.is("Edit") || this.mode.is("Delayed"), 0, 0, 100, 2);
    private final IntegerSetting delay = new IntegerSetting("Delay", () -> this.mode.is("Delayed") || this.mode.is("Strafe") || this.mode.is("Ground collision"), 1, 0, 5, 1);
    private final BooleanSetting always = new BooleanSetting("Always", () -> this.mode.is("Legit"), false);
    private final BooleanSetting dmgBoost = new BooleanSetting("Dmg Boost", () -> this.mode.is("Hypixel"), true);
    private final IntegerSetting distance = new IntegerSetting("Distance", () -> this.mode.is("Block collide"), 2, 1, 1, 3);
    private final BooleanSetting onlyServerSide = new BooleanSetting("Only server-side", () -> this.mode.is("Spoof offground"), true);
    private final DoubleSetting ySpoof = new DoubleSetting("Y spoof", () -> this.mode.is("Spoof offground"), 0.07, 0.0, 0.42, 0.01);
    private final BooleanSetting spoofForTwoTicks = new BooleanSetting("Spoof for two ticks", () -> this.mode.is("Spoof offground"), false);
    private final BooleanSetting roundPosition = new BooleanSetting("Round position", () -> this.mode.is("Ground collision"), true);
    private boolean velocityTick;
    private int counter;
    private int ticks;
    private double direction;
    private final CopyOnWriteArrayList<Packet> packetsQueue = new CopyOnWriteArrayList();
    private BlockPos barrier;

    public Velocity() {
        super("Velocity", Category.COMBAT);
        this.addSettings(this.mode, this.horizontal, this.vertical, this.delay, this.always, this.distance, this.onlyServerSide, this.ySpoof, this.spoofForTwoTicks, this.roundPosition, this.dmgBoost);
    }

    @Override
    public void onEnable() {
        this.ticks = 0;
        this.counter = 0;
        this.direction = 0.0;
        this.packetsQueue.clear();
    }

    @Override
    public void onDisable() {
        if (this.barrier != null) {
            Velocity.mc.theWorld.setBlockToAir(this.barrier);
            this.barrier = null;
        }
    }

    @Listener
    public void onVelocity(VelocityEvent event) {
        this.direction = Math.hypot(event.getX(), event.getZ()) > 0.0 ? (double)RotationsUtil.getRotationsToPosition(Velocity.mc.thePlayer.posX + event.getX(), Velocity.mc.thePlayer.posY, Velocity.mc.thePlayer.posZ + event.getZ())[0] : (double)MovementUtil.getPlayerDirection();
        switch (this.mode.getMode()) {
            case "Packet cancel": {
                this.velocityTick = true;
            }
            case "Cancel": {
                event.setX(Velocity.mc.thePlayer.motionX);
                event.setY(Velocity.mc.thePlayer.motionY);
                event.setZ(Velocity.mc.thePlayer.motionZ);
                break;
            }
            case "Edit": {
                event.setX(event.getX() * (double)this.horizontal.getValue() / 100.0);
                event.setZ(event.getZ() * (double)this.horizontal.getValue() / 100.0);
                event.setY(event.getY() * (double)this.vertical.getValue() / 100.0);
                break;
            }
            case "Ignore horizontal": {
                event.setX(Velocity.mc.thePlayer.motionX);
                event.setZ(Velocity.mc.thePlayer.motionZ);
                break;
            }
            case "Delayed": 
            case "Strafe": 
            case "Ground collision": {
                this.ticks = this.delay.getValue() + 1;
                break;
            }
            case "Hypixel": {
                if (MovementUtil.isMoving() && !Velocity.mc.thePlayer.onGround) {
                    this.ticks = 3;
                    break;
                }
                event.setX(Velocity.mc.thePlayer.motionX);
                event.setZ(Velocity.mc.thePlayer.motionZ);
                break;
            }
            case "MMC": {
                ++this.counter;
                if (this.counter % 3 != 0) break;
                event.setX(Velocity.mc.thePlayer.motionX);
                event.setY(Velocity.mc.thePlayer.motionY);
                event.setZ(Velocity.mc.thePlayer.motionZ);
                break;
            }
            case "Block collide": {
                this.ticks = 5;
                break;
            }
            case "Spoof offground": {
                event.setX(Velocity.mc.thePlayer.motionX);
                event.setY(Velocity.mc.thePlayer.motionY);
                event.setZ(Velocity.mc.thePlayer.motionZ);
                this.ticks = this.spoofForTwoTicks.isEnabled() ? 2 : 1;
                break;
            }
            case "Legit": {
                if (!Velocity.mc.thePlayer.onGround || !(event.getY() > 0.0) || Velocity.mc.currentScreen != null) break;
                if (!this.always.isEnabled()) {
                    double velocityDist = Math.hypot(event.getX(), event.getZ());
                    if (this.counter >= 4 && (velocityDist < 0.6 || this.counter >= 7)) {
                        this.counter = 0;
                        break;
                    }
                    this.velocityTick = true;
                    ++this.counter;
                    break;
                }
                this.velocityTick = true;
            }
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        switch (this.mode.getMode()) {
            case "Legit": {
                if (!this.velocityTick) break;
                Velocity.mc.gameSettings.keyBindJump.pressed = true;
                Velocity.mc.gameSettings.keyBindForward.pressed = true;
                Velocity.mc.gameSettings.keyBindSprint.pressed = true;
                break;
            }
            case "Packet cancel": {
                if (this.velocityTick || this.packetsQueue.isEmpty()) break;
                for (Packet p : this.packetsQueue) {
                    PacketUtil.sendPacket(p);
                }
                this.packetsQueue.clear();
                break;
            }
            case "MMC": {
                if (this.ticks <= 0) break;
                --this.ticks;
                if (this.ticks != 1) break;
                Velocity.mc.thePlayer.onGround = true;
                break;
            }
            case "Block collide": {
                if (this.ticks <= 0) break;
                if (this.ticks == 5) {
                    float dir = (float)Math.toRadians(this.direction);
                    double posX = Velocity.mc.thePlayer.posX - Math.sin(dir) * 2.0;
                    double posZ = Velocity.mc.thePlayer.posZ + Math.cos(dir) * 2.0;
                    this.barrier = new BlockPos(posX, Velocity.mc.thePlayer.posY + 1.0, posZ);
                    Velocity.mc.theWorld.setBlockState(this.barrier, Blocks.barrier.getDefaultState());
                } else if (this.ticks == 1 && this.barrier != null) {
                    Velocity.mc.theWorld.setBlockToAir(this.barrier);
                    this.barrier = null;
                }
                --this.ticks;
                break;
            }
            case "Spoof offground": {
                if (this.ticks <= 0 || this.onlyServerSide.isEnabled()) break;
                Velocity.mc.thePlayer.onGround = false;
                break;
            }
            case "Ground collision": {
                if (this.ticks != 1) break;
                Velocity.mc.thePlayer.onGround = true;
                if (!this.roundPosition.isEnabled()) break;
                Velocity.mc.thePlayer.setPosition(Velocity.mc.thePlayer.posX, (double)Math.round(Velocity.mc.thePlayer.posY * 64.0) / 64.0, Velocity.mc.thePlayer.posZ);
            }
        }
    }

    @Listener
    public void onMove(MoveEvent event) {
        switch (this.mode.getMode()) {
            case "Delayed": {
                if (this.ticks <= 0) break;
                --this.ticks;
                if (this.ticks != 0) break;
                Velocity.mc.thePlayer.motionX = event.getX() * (double)this.horizontal.getValue() / 100.0;
                event.setX(Velocity.mc.thePlayer.motionX);
                Velocity.mc.thePlayer.motionZ = event.getZ() * (double)this.horizontal.getValue() / 100.0;
                event.setZ(Velocity.mc.thePlayer.motionZ);
                Velocity.mc.thePlayer.motionY = event.getY() * (double)this.vertical.getValue() / 100.0;
                event.setY(Velocity.mc.thePlayer.motionY);
                break;
            }
            case "Strafe": 
            case "Hypixel": {
                if (this.ticks <= 1) break;
                --this.ticks;
                if (this.ticks != 1 || !this.dmgBoost.isEnabled()) break;
                MovementUtil.strafe(event);
                break;
            }
            case "MMC": {
                if (this.ticks != 1) break;
                Velocity.mc.thePlayer.onGround = true;
                Velocity.mc.thePlayer.motionY = 0.0;
                event.setY(0.0);
                break;
            }
            case "Ground collision": {
                if (this.ticks != 1) break;
                Velocity.mc.thePlayer.onGround = true;
                Velocity.mc.thePlayer.motionY = 0.0;
                event.setY(0.0);
            }
        }
    }

    @Listener
    public void onMotion(MotionEvent event) {
        switch (this.mode.getMode()) {
            case "Legit": {
                if (!this.velocityTick) break;
                KeyboardUtil.resetKeybindings(Velocity.mc.gameSettings.keyBindJump, Velocity.mc.gameSettings.keyBindForward, Velocity.mc.gameSettings.keyBindSprint);
                this.velocityTick = false;
                break;
            }
            case "Packet cancel": {
                if (!this.velocityTick) break;
                this.packetsQueue.clear();
                this.velocityTick = false;
                break;
            }
            case "Ground collision": {
                if (this.ticks <= 0) break;
                if (this.ticks == 1) {
                    event.setOnGround(true);
                }
                --this.ticks;
                break;
            }
            case "Spoof offground": {
                if (this.ticks <= 0) break;
                event.setOnGround(false);
                event.setY(event.getY() + (double)((float)this.ySpoof.getValue()));
                --this.ticks;
            }
        }
    }
}

