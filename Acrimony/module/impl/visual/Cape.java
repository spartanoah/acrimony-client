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
    private final ModeSetting cmode = new ModeSetting("Cape Mode", "Acrimony", "Acrimony", "Rise", "Dortware", "Hanabi", "Diablo", "Astolfo", "Expensive", "Hantai", "Hantai1", "Hantai2", "Hantai3", "Hantai4", "Hantai5", "Hantai6", "Hantai7");

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
                break;
            }
            case "Astolfo": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/astolfo.png"));
                break;
            }
            case "Expensive": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/expensive.png"));
                break;
            }
            case "Hantai": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai.png"));
                break;
            }
            case "Hantai1": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai1.png"));
                break;
            }
            case "Hantai2": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai2.png"));
                break;
            }
            case "Hantai3": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai3.png"));
                break;
            }
            case "Hantai4": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai4.png"));
                break;
            }
            case "Hantai5": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai5.png"));
                break;
            }
            case "Hantai6": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai6.png"));
                break;
            }
            case "Hantai7": {
                Cape.mc.thePlayer.setLocationOfCape(new ResourceLocation("acrimony/misc/hantai7.png"));
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

