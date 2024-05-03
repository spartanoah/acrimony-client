/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.ghost;

import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.ModeSetting;

public class DelayRemover
extends Module {
    public final ModeSetting mouseDelayMode = new ModeSetting("Mouse delay", "Normal", "Normal", "Reach gain", "None");

    public DelayRemover() {
        super("Delay remover", Category.GHOST);
        this.addSettings(this.mouseDelayMode);
    }
}

