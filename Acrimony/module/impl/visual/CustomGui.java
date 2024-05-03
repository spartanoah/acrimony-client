/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ModeSetting;

public class CustomGui
extends Module {
    public static BooleanSetting clearChat = new BooleanSetting("Clear Chat", true);
    public static ModeSetting guiContainerAnimation = new ModeSetting("Inventory", "Scale", "Scale", "Slide", "Default");

    public CustomGui() {
        super("Gui", Category.VISUAL);
        this.addSettings(guiContainerAnimation, clearChat);
    }
}

