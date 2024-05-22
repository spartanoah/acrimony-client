/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util;

import Acrimony.Acrimony;
import Acrimony.module.Module;
import Acrimony.module.ModuleManager;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.module.impl.combat.TargetStrafe;
import Acrimony.module.impl.combat.Teams;
import Acrimony.module.impl.ghost.DelayRemover;
import Acrimony.module.impl.ghost.Reach;
import Acrimony.module.impl.movement.Blink;
import Acrimony.module.impl.movement.Fly;
import Acrimony.module.impl.movement.Longjump;
import Acrimony.module.impl.movement.Speed;
import Acrimony.module.impl.visual.Animations;
import Acrimony.module.impl.visual.HUD;

public class ModuleUtil {
    private static ModuleManager moduleManager;

    private static ModuleManager getModuleManager() {
        if (moduleManager == null) {
            moduleManager = Acrimony.instance.getModuleManager();
        }
        return moduleManager;
    }

    public static Killaura getKillaura() {
        return ModuleUtil.getModuleManager().getModule(Killaura.class);
    }

    public static Reach getReach() {
        return ModuleUtil.getModuleManager().getModule(Reach.class);
    }

    public static TargetStrafe getTargetStrafe() {
        return ModuleUtil.getModuleManager().getModule(TargetStrafe.class);
    }

    public static Teams getTeams() {
        return ModuleUtil.getModuleManager().getModule(Teams.class);
    }

    public static Fly getFly() {
        return ModuleUtil.getModuleManager().getModule(Fly.class);
    }

    public static DelayRemover getDelayRemover() {
        return ModuleUtil.getModuleManager().getModule(DelayRemover.class);
    }

    public static Speed getSpeed() {
        return ModuleUtil.getModuleManager().getModule(Speed.class);
    }

    public static HUD getHUD() {
        return ModuleUtil.getModuleManager().getModule(HUD.class);
    }

    public static Longjump getLongjump() {
        return ModuleUtil.getModuleManager().getModule(Longjump.class);
    }

    public static Animations getAnimations() {
        return ModuleUtil.getModuleManager().getModule(Animations.class);
    }

    public static Blink getBlink() {
        return ModuleUtil.getModuleManager().getModule(Blink.class);
    }

    public static void startBlinking(Module m, boolean transactionsAtLast) {
        ModuleUtil.getBlink().startBlinking(m, transactionsAtLast);
    }

    public static void stopBlinking() {
        ModuleUtil.getBlink().stopBlinking();
    }
}

