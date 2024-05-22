/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.click.window.holder;

import Acrimony.module.Module;
import Acrimony.setting.AbstractSetting;
import Acrimony.ui.click.dropdown.holder.SettingHolder;
import Acrimony.util.misc.TimerUtil;
import java.util.ArrayList;

public class ModuleHolder {
    private Module module;
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil settingsShownTimer = new TimerUtil();
    private boolean lastEnabled;
    private final ArrayList<SettingHolder> settings = new ArrayList();
    private boolean settingsShown;

    public ModuleHolder(Module m) {
        this.module = m;
        for (AbstractSetting s : m.getSettings()) {
            this.settings.add(new SettingHolder(s));
        }
    }

    public void updateState() {
        boolean enabled = this.module.isEnabled();
        if (enabled != this.lastEnabled) {
            this.timer.reset();
        }
        this.lastEnabled = this.module.isEnabled();
    }

    public void setSettingsShown(boolean shown) {
        if (this.settingsShown != shown) {
            this.settingsShown = shown;
            this.settingsShownTimer.reset();
        }
    }

    public Module getModule() {
        return this.module;
    }

    public TimerUtil getTimer() {
        return this.timer;
    }

    public TimerUtil getSettingsShownTimer() {
        return this.settingsShownTimer;
    }

    public boolean isLastEnabled() {
        return this.lastEnabled;
    }

    public ArrayList<SettingHolder> getSettings() {
        return this.settings;
    }

    public boolean isSettingsShown() {
        return this.settingsShown;
    }
}

