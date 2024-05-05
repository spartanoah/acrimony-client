/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module;

import Acrimony.module.HUDModule;
import Acrimony.module.Module;
import Acrimony.module.impl.combat.Antibot;
import Acrimony.module.impl.combat.Killaura;
import Acrimony.module.impl.combat.TargetStrafe;
import Acrimony.module.impl.combat.Teams;
import Acrimony.module.impl.combat.Tickbase;
import Acrimony.module.impl.combat.Velocity;
import Acrimony.module.impl.ghost.AimAssist;
import Acrimony.module.impl.ghost.Autoclicker;
import Acrimony.module.impl.ghost.Backtrack;
import Acrimony.module.impl.ghost.DelayRemover;
import Acrimony.module.impl.ghost.FastPlace;
import Acrimony.module.impl.ghost.KeepSprint;
import Acrimony.module.impl.ghost.Reach;
import Acrimony.module.impl.ghost.Safewalk;
import Acrimony.module.impl.ghost.WTap;
import Acrimony.module.impl.misc.Autoplay;
import Acrimony.module.impl.misc.SelfDestruct;
import Acrimony.module.impl.movement.Blink;
import Acrimony.module.impl.movement.Fly;
import Acrimony.module.impl.movement.InventoryMove;
import Acrimony.module.impl.movement.Noslow;
import Acrimony.module.impl.movement.Speed;
import Acrimony.module.impl.movement.Sprint;
import Acrimony.module.impl.movement.Turnback;
import Acrimony.module.impl.player.Antivoid;
import Acrimony.module.impl.player.AutoTool;
import Acrimony.module.impl.player.Breaker;
import Acrimony.module.impl.player.ChestStealer;
import Acrimony.module.impl.player.InventoryManager;
import Acrimony.module.impl.player.Nofall;
import Acrimony.module.impl.player.Scaffold;
import Acrimony.module.impl.player.Timer;
import Acrimony.module.impl.visual.Animations;
import Acrimony.module.impl.visual.Cape;
import Acrimony.module.impl.visual.Chams;
import Acrimony.module.impl.visual.ClickGuiModule;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.module.impl.visual.CustomGui;
import Acrimony.module.impl.visual.ESP;
import Acrimony.module.impl.visual.Fullbright;
import Acrimony.module.impl.visual.GlowESP;
import Acrimony.module.impl.visual.HUD;
import Acrimony.module.impl.visual.Keystrokes;
import Acrimony.module.impl.visual.NameProtect;
import Acrimony.module.impl.visual.Rotations;
import Acrimony.module.impl.visual.SessionInfo;
import Acrimony.module.impl.visual.TargetHUD;
import Acrimony.module.impl.visual.TimeChanger;
import Acrimony.module.impl.visual.Xray;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleManager {
    public List<Module> modules = new ArrayList<Module>();
    public List<HUDModule> hudModules;

    public ModuleManager() {
        this.modules.add(new Killaura());
        this.modules.add(new Velocity());
        this.modules.add(new TargetStrafe());
        this.modules.add(new Tickbase());
        this.modules.add(new Teams());
        this.modules.add(new Backtrack());
        this.modules.add(new Reach());
        this.modules.add(new Autoclicker());
        this.modules.add(new AimAssist());
        this.modules.add(new DelayRemover());
        this.modules.add(new WTap());
        this.modules.add(new Antibot());
        this.modules.add(new KeepSprint());
        this.modules.add(new Sprint());
        this.modules.add(new Turnback());
        this.modules.add(new Fly());
        this.modules.add(new Speed());
        this.modules.add(new InventoryMove());
        this.modules.add(new Noslow());
        this.modules.add(new Blink());
        this.modules.add(new Safewalk());
        this.modules.add(new ChestStealer());
        this.modules.add(new InventoryManager());
        this.modules.add(new Nofall());
        this.modules.add(new Antivoid());
        this.modules.add(new Timer());
        this.modules.add(new FastPlace());
        this.modules.add(new AutoTool());
        this.modules.add(new Scaffold());
        this.modules.add(new Breaker());
        this.modules.add(new HUD());
        this.modules.add(new ClientTheme());
        this.modules.add(new ClickGuiModule());
        this.modules.add(new ESP());
        this.modules.add(new Chams());
        this.modules.add(new Animations());
        this.modules.add(new Rotations());
        this.modules.add(new TargetHUD());
        this.modules.add(new Keystrokes());
        this.modules.add(new TimeChanger());
        this.modules.add(new Fullbright());
        this.modules.add(new NameProtect());
        this.modules.add(new Xray());
        this.modules.add(new GlowESP());
        this.modules.add(new CustomGui());
        this.modules.add(new SessionInfo());
        this.modules.add(new Cape());
        this.modules.add(new Autoplay());
        this.modules.add(new SelfDestruct());
        this.hudModules = this.modules.stream().filter(HUDModule.class::isInstance).map(HUDModule.class::cast).collect(Collectors.toList());
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        Optional<Module> module = this.modules.stream().filter(m -> m.getClass().equals(clazz)).findFirst();
        if (module.isPresent()) {
            return (T)module.get();
        }
        return null;
    }

    public void addModules(List<Module> modules) {
        this.modules = modules;
    }

    public <T extends Module> T getModuleByName(String name) {
        Optional<Module> module = this.modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst();
        if (module.isPresent()) {
            return (T)module.get();
        }
        return null;
    }

    public <T extends Module> T getModuleByNameNoSpace(String name) {
        Optional<Module> module = this.modules.stream().filter(m -> m.getName().replace(" ", "").equalsIgnoreCase(name)).findFirst();
        if (module.isPresent()) {
            return (T)module.get();
        }
        return null;
    }
}

