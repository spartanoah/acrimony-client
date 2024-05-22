/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.visual;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.ui.click.dropdown.DropdownClickGUI;
import Acrimony.ui.click.window.WindowClickGUI;

public class ClickGuiModule
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Dropdown", "Window", "Dropdown");
    private DropdownClickGUI dropdownClickGUI;
    private WindowClickGUI window;
    public final BooleanSetting boxOnHover = new BooleanSetting("Box on hover", false);
    public final BooleanSetting boxOnSettings = new BooleanSetting("Box on settings", () -> this.boxOnHover.isEnabled(), false);
    private ClientTheme theme;

    public ClickGuiModule() {
        super("ClickGUI", Category.VISUAL);
        this.setKey(54);
        this.addSettings(this.mode, this.boxOnHover, this.boxOnSettings);
    }

    @Override
    public void onEnable() {
        switch (this.mode.getMode()) {
            case "Dropdown": {
                if (this.dropdownClickGUI == null) {
                    this.dropdownClickGUI = new DropdownClickGUI(this);
                }
                mc.displayGuiScreen(this.dropdownClickGUI);
                break;
            }
            case "Window": {
                if (this.window == null) {
                    this.window = new WindowClickGUI(this);
                }
                mc.displayGuiScreen(this.window);
            }
        }
    }

    @Override
    public void onClientStarted() {
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
    }

    @Listener
    public void onRender(RenderEvent event) {
    }
}

