/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.event.Listener;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.DoubleSetting;
import Acrimony.setting.impl.ModeSetting;

public class Animations
extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "1.7", "1.7", "Old", "Slide", "Plain", "Sigma", "Remix", "Felix", "Swank", "Swang", "Astolfo", "Exhibition", "Exhibobo");
    public static DoubleSetting sSpeed = new DoubleSetting("Swing Speed", 1.0, 0.1, 16.0, 0.1);
    public static BooleanSetting cameramod = new BooleanSetting("Camera Setting", false);
    public static DoubleSetting x = new DoubleSetting("X", () -> cameramod.isEnabled(), 1.125, -2.0, 2.0, 0.025);
    public static DoubleSetting y = new DoubleSetting("Y", () -> cameramod.isEnabled(), -0.075, -0.9, 1.55, 0.025);
    public static DoubleSetting z = new DoubleSetting("Z", () -> cameramod.isEnabled(), -1.2, -3.0, -1.2, 0.025);
    public static DoubleSetting scale = new DoubleSetting("Scale", () -> cameramod.isEnabled(), 1.0, -2.0, 1.0, 0.05);

    @Listener
    public void onUpdate(UpdateEvent event) {
    }

    public Animations() {
        super("Animations", Category.VISUAL);
        this.addSettings(mode, sSpeed, cameramod, x, y, z, scale);
    }
}

