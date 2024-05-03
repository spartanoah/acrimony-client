/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.combat;

import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.util.ModuleUtil;
import Acrimony.util.player.RotationsUtil;
import Acrimony.util.world.WorldUtil;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Keyboard;

public class TargetStrafe
extends Module {
    private final DoubleSetting maxRange = new DoubleSetting("Max range", 3.0, 1.0, 6.0, 0.1);
    public final BooleanSetting whilePressingSpace = new BooleanSetting("While pressing space", false);
    private boolean goingRight;
    private Killaura killaura;

    public TargetStrafe() {
        super("TargetStrafe", Category.COMBAT);
        this.addSettings(this.maxRange, this.whilePressingSpace);
    }

    @Override
    public void onClientStarted() {
        this.killaura = ModuleUtil.getKillaura();
    }

    public boolean shouldTargetStrafe() {
        return this.killaura.isEnabled() && this.killaura.getTarget() != null && this.killaura.getDistanceToEntity(this.killaura.getTarget()) <= this.killaura.rotationRange.getValue() && this.isEnabled() && (Keyboard.isKeyDown(TargetStrafe.mc.gameSettings.keyBindJump.getKeyCode()) || !this.whilePressingSpace.isEnabled());
    }

    public float getDirection() {
        float direction;
        EntityLivingBase target;
        double distance;
        if (TargetStrafe.mc.thePlayer.isCollidedHorizontally || !WorldUtil.isBlockUnder(3)) {
            boolean bl = this.goingRight = !this.goingRight;
        }
        if ((distance = this.killaura.getDistanceToEntity(target = this.killaura.getTarget())) > this.maxRange.getValue()) {
            direction = RotationsUtil.getRotationsToEntity(target, false)[0];
        } else {
            double offset = 90.0 - this.killaura.getDistanceToEntity(target) * 5.0;
            if (!this.goingRight) {
                offset = -offset;
            }
            direction = (float)((double)RotationsUtil.getRotationsToEntity(target, false)[0] + offset);
        }
        return (float)Math.toRadians(direction);
    }
}

