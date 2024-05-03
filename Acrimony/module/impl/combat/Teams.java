/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.combat;

import Acrimony.module.Category;
import Acrimony.module.Module;
import net.minecraft.entity.player.EntityPlayer;

public class Teams
extends Module {
    public Teams() {
        super("Teams", Category.COMBAT);
    }

    public boolean canAttack(EntityPlayer entity) {
        if (!this.isEnabled()) {
            return true;
        }
        if (Teams.mc.thePlayer.getTeam() != null && entity.getTeam() != null) {
            Character targetColor = Character.valueOf(entity.getDisplayName().getFormattedText().charAt(1));
            Character playerColor = Character.valueOf(Teams.mc.thePlayer.getDisplayName().getFormattedText().charAt(1));
            return !playerColor.equals(targetColor);
        }
        return false;
    }
}

