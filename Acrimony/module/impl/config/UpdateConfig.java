/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.config;

import Acrimony.autoconfig.DownloadConfig;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.Module;

public class UpdateConfig
extends Module {
    public UpdateConfig() {
        super("Update Config", Category.CONFIG);
        this.listenType = EventListenType.MANUAL;
        this.setStateHidden(true);
        this.startListening();
        this.setEnabledSilently(true);
    }

    @Override
    public void onEnable() {
        DownloadConfig.init();
        this.toggle();
    }
}

