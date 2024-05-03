/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;

public class Blink
extends Module {
    private final BooleanSetting releaseTransactionsAtLast = new BooleanSetting("Release last", false);
    private String status;
    private boolean lastTransactionsAtLast;

    public Blink() {
        super("Blink", Category.MOVEMENT);
        this.addSettings(this.releaseTransactionsAtLast);
    }

    @Override
    public void onEnable() {
        Acrimony.instance.getPacketBlinkHandler().startBlinking();
    }

    @Override
    public void onDisable() {
        Acrimony.instance.getPacketBlinkHandler().stopBlinking();
        if (this.status != null) {
            this.releaseTransactionsAtLast.setEnabled(this.lastTransactionsAtLast);
            this.setStateHidden(true);
            this.status = null;
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        if (Blink.mc.thePlayer.ticksExisted < 10) {
            this.setEnabled(false);
        }
    }

    public void startBlinking(Module m, boolean transactionsAtLast) {
        this.lastTransactionsAtLast = this.releaseTransactionsAtLast.isEnabled();
        this.releaseTransactionsAtLast.setEnabled(transactionsAtLast);
        this.status = "Enabled by " + m.getName();
        this.setStateHidden(true);
        this.setEnabled(true);
    }

    public void stopBlinking() {
        this.releaseTransactionsAtLast.setEnabled(this.lastTransactionsAtLast);
        this.status = "";
        this.setStateHidden(false);
        this.setEnabled(false);
    }
}

