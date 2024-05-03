/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.ghost;

import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;

public class Reach
extends Module {
    public final DoubleSetting startingReach = new DoubleSetting("Starting reach", 3.5, 3.0, 6.0, 0.05);
    public final DoubleSetting reach = new DoubleSetting("Reach", 3.5, 3.0, 6.0, 0.05);

    public Reach() {
        super("Reach", Category.GHOST);
        this.addSettings(this.startingReach, this.reach);
    }
}

