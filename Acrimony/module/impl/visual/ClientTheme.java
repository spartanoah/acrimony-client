/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.Module;
import Acrimony.setting.impl.ColorSetting;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.util.render.ColorUtil;
import java.awt.Color;

public class ClientTheme
extends Module {
    public final ColorSetting color = new ColorSetting("Color", "Acrimony", "Acrimony", "Acrimony2", "Blue", "Red", "Pink", "Coral", "Chocolate", "Berry", "Wood", "Water", "Lava", "Purple");
    private boolean colorsSet;
    public static Color color1;
    public static Color color2;
    private final IntegerSetting fadespeed = new IntegerSetting("ColorSpeed", 2500, 200, 5000, 10);
    public static IntegerSetting blurradius;

    public ClientTheme() {
        super("Color theme", Category.VISUAL);
        this.addSettings(this.color, this.fadespeed, blurradius);
        this.listenType = EventListenType.MANUAL;
        this.startListening();
    }

    @Override
    public void onEnable() {
        this.setEnabled(false);
    }

    @Listener(value=0)
    public void onRender(RenderEvent event) {
        this.colorsSet = true;
    }

    public int getColor(int offset) {
        if (!this.colorsSet) {
            this.colorsSet = true;
        }
        switch (this.color.getMode()) {
            case "White": {
                color1 = new Color(1, 1, 1);
                color2 = new Color(141, 141, 141);
                return -1;
            }
            case "Acrimony": {
                color1 = new Color(0, 231, 252);
                color2 = new Color(102, 94, 208);
                return ColorUtil.getColor(new Color(0, 231, 252), new Color(102, 94, 208), this.fadespeed.getValue(), offset);
            }
            case "Acrimony2": {
                color1 = new Color(145, 33, 33);
                color2 = new Color(255, 72, 72);
                return ColorUtil.getColor(new Color(145, 33, 33), new Color(255, 72, 72), this.fadespeed.getValue(), offset);
            }
            case "Blue": {
                color1 = new Color(0, 35, 206);
                color2 = new Color(25, 25, 25);
                return ColorUtil.getColor(new Color(25, 25, 25), new Color(0, 35, 206), this.fadespeed.getValue(), offset);
            }
            case "Red": {
                color1 = new Color(255, 35, 35);
                color2 = new Color(255, 226, 226);
                return ColorUtil.getColor(new Color(255, 35, 35), new Color(255, 226, 226), new Color(145, 33, 33), this.fadespeed.getValue(), offset);
            }
            case "Pink": {
                color1 = new Color(255, 112, 236);
                color2 = new Color(25, 25, 25);
                return ColorUtil.getColor(new Color(255, 112, 236), new Color(25, 25, 25), this.fadespeed.getValue(), offset);
            }
            case "Coral": {
                color1 = new Color(211, 45, 30);
                color2 = new Color(25, 25, 25);
                return ColorUtil.getColor(new Color(25, 25, 25), new Color(211, 45, 30), this.fadespeed.getValue(), offset);
            }
            case "Chocolate": {
                color1 = new Color(123, 63, 0);
                color2 = new Color(197, 138, 75);
                return ColorUtil.getColor(new Color(123, 63, 0), new Color(197, 138, 75), this.fadespeed.getValue(), offset);
            }
            case "Berry": {
                color1 = new Color(31, 169, 255);
                color2 = new Color(120, 89, 255);
                return ColorUtil.getColor(new Color(31, 169, 255), new Color(120, 89, 255), this.fadespeed.getValue(), offset);
            }
            case "Wood": {
                color1 = new Color(196, 147, 124);
                color2 = new Color(54, 126, 52);
                return ColorUtil.getColor(new Color(196, 147, 124), new Color(54, 126, 52), this.fadespeed.getValue(), offset);
            }
            case "Water": {
                color1 = new Color(25, 25, 25);
                color2 = new Color(31, 169, 255);
                return ColorUtil.getColor(new Color(25, 25, 25), new Color(31, 169, 255), this.fadespeed.getValue(), offset);
            }
            case "Lava": {
                color1 = new Color(255, 140, 68);
                color2 = new Color(255, 78, 78);
                return ColorUtil.getColor(new Color(255, 140, 68), new Color(255, 78, 78), this.fadespeed.getValue(), offset);
            }
            case "Purple": {
                color1 = new Color(202, 68, 255);
                color2 = new Color(25, 25, 25);
                return ColorUtil.getColor(new Color(202, 68, 255), new Color(25, 25, 25), this.fadespeed.getValue(), offset);
            }
        }
        return -1;
    }

    static {
        blurradius = new IntegerSetting("Blooms Radius", 16, 5, 30, 1);
    }
}

