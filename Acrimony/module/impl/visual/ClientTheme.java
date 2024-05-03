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
import Acrimony.util.render.ColorUtil;
import java.awt.Color;

public class ClientTheme
extends Module {
    public final ColorSetting color = new ColorSetting("Color", "Acrimony", "Acrimony", "Acrimony2", "White");
    private boolean colorsSet;
    public static Color color1;
    public static Color color2;

    public ClientTheme() {
        super("Color theme", Category.VISUAL);
        this.addSettings(this.color);
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
                return ColorUtil.getColor(new Color(0, 231, 252), new Color(102, 94, 208), 2500L, offset);
            }
            case "Acrimony2": {
                color1 = new Color(145, 33, 33);
                color2 = new Color(255, 72, 72);
                return ColorUtil.getColor(new Color(145, 33, 33), new Color(255, 72, 72), 2500L, offset);
            }
        }
        return -1;
    }
}

