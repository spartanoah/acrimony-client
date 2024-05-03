/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.client;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.KeyPressEvent;

public class KeybindHandler {
    public KeybindHandler() {
        Acrimony.instance.getEventManager().register(this);
    }

    @Listener
    public void onKeyPress(KeyPressEvent event) {
        Acrimony.instance.getModuleManager().modules.stream().filter(m -> m.getKey() == event.getKey()).forEach(m -> m.toggle());
    }
}

