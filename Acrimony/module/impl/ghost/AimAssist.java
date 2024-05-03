/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.ghost;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Antibot;
import Acrimony.module.impl.combat.Teams;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.player.FixedRotations;
import Acrimony.util.player.RotationsUtil;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public class AimAssist
extends Module {
    private Antibot antibotModule;
    private Teams teamsModule;
    private final ModeSetting filter = new ModeSetting("Filter", "Range", "Range", "Health");
    private final DoubleSetting range = new DoubleSetting("Range", 4.5, 3.0, 8.0, 0.1);
    private final IntegerSetting speed = new IntegerSetting("Speed", 10, 1, 40, 1);
    private final TimerUtil timer = new TimerUtil();
    private EntityPlayer target;
    private FixedRotations rotations;

    public AimAssist() {
        super("AimAssist", Category.GHOST);
        this.addSettings(this.filter, this.range, this.speed);
    }

    @Override
    public void onEnable() {
        this.rotations = new FixedRotations(AimAssist.mc.thePlayer.rotationYaw, AimAssist.mc.thePlayer.rotationPitch);
    }

    @Override
    public void onClientStarted() {
        this.antibotModule = Acrimony.instance.getModuleManager().getModule(Antibot.class);
        this.teamsModule = Acrimony.instance.getModuleManager().getModule(Teams.class);
    }

    @Listener
    public void onRender(RenderEvent event) {
        this.target = this.findTarget();
        if (this.target != null && Mouse.isButtonDown(0) && AimAssist.mc.currentScreen == null) {
            float[] rots = RotationsUtil.getRotationsToEntity(this.target, false);
            float yaw = rots[0];
            float currentYaw = MathHelper.wrapAngleTo180_float(AimAssist.mc.thePlayer.rotationYaw);
            float diff = Math.abs(currentYaw - yaw);
            if (diff >= 4.0f && diff <= 356.0f) {
                float aa = diff <= (float)this.speed.getValue() ? diff * 0.9f : (float)((double)this.speed.getValue() - Math.random() * 0.5);
                float finalSpeed = aa * (float)Math.max(this.timer.getTimeElapsed(), 1L) * 0.01f;
                AimAssist.mc.thePlayer.rotationYaw = diff <= 180.0f ? (currentYaw > yaw ? (AimAssist.mc.thePlayer.rotationYaw -= finalSpeed) : (AimAssist.mc.thePlayer.rotationYaw += finalSpeed)) : (currentYaw > yaw ? (AimAssist.mc.thePlayer.rotationYaw += finalSpeed) : (AimAssist.mc.thePlayer.rotationYaw -= finalSpeed));
            }
        }
        this.rotations.updateRotations(AimAssist.mc.thePlayer.rotationYaw, AimAssist.mc.thePlayer.rotationPitch);
        AimAssist.mc.thePlayer.rotationYaw = this.rotations.getYaw();
        AimAssist.mc.thePlayer.rotationPitch = this.rotations.getPitch();
        this.timer.reset();
    }

    public EntityPlayer findTarget() {
        ArrayList<EntityPlayer> entities = new ArrayList<EntityPlayer>();
        for (Entity entity2 : AimAssist.mc.theWorld.loadedEntityList) {
            EntityPlayer player;
            if (!(entity2 instanceof EntityPlayer) || entity2 == AimAssist.mc.thePlayer || !this.canAttackEntity(player = (EntityPlayer)entity2)) continue;
            entities.add(player);
        }
        if (entities != null && entities.size() > 0) {
            switch (this.filter.getMode()) {
                case "Range": {
                    entities.sort(Comparator.comparingDouble(entity -> entity.getDistanceToEntity(AimAssist.mc.thePlayer)));
                    break;
                }
                case "Health": {
                    entities.sort(Comparator.comparingDouble(entity -> entity.getHealth()));
                }
            }
            return (EntityPlayer)entities.get(0);
        }
        return null;
    }

    private boolean canAttackEntity(EntityPlayer player) {
        if (!player.isDead && (double)AimAssist.mc.thePlayer.getDistanceToEntity(player) < this.range.getValue() && !player.isInvisible() && !player.isInvisibleToPlayer(AimAssist.mc.thePlayer)) {
            if (!this.teamsModule.canAttack(player)) {
                return false;
            }
            return this.antibotModule.canAttack(player, this);
        }
        return false;
    }
}

