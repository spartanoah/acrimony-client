/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.ghost;

import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;

public class Safewalk
extends Module {
    public final BooleanSetting offGround = new BooleanSetting("Offground", false);

    public Safewalk() {
        super("Safewalk", Category.GHOST);
        this.addSettings(this.offGround);
    }
}

