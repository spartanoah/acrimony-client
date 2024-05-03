/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.event.Listener;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.ModeSetting;
import net.minecraft.util.ResourceLocation;

public class Cape
extends Module {
    private final ModeSetting cmode = new ModeSetting("Cape Mode", "Acrimony", "Acrimony", "Rise", "Dortware", "Hanabi", "Diablo");

    public Cape() {
        super("Client Cape", Category.VISUAL);
        this.addSettings(this.cmode);
        this.setEnabledSilently(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Listener
    public void onUpdate(UpdateEvent e) {
        switch (this.cmode.getMode()) {
            case "Acrimony": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/main.png"));
                break;
            }
            case "Rise": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/rise.png"));
                break;
            }
            case "Dortware": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/dort.png"));
                break;
            }
            case "Hanabi": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hanabi.png"));
                break;
            }
            case "Diablo": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/diablo.png"));
            }
        }
    }

    @Override
    public void onDisable() {
        Cape.mc.thePlayer.setLocationOfCape(null);
        super.onDisable();
    }

    public ModeSetting getCmode() {
        return this.cmode;
    }
}

