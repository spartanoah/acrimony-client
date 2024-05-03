/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module;

import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.module.AlignType;
import Acrimony.module.Category;
import Acrimony.module.EventListenType;
import Acrimony.module.Module;
import Acrimony.setting.impl.DoubleSetting;
import net.minecraft.client.gui.GuiChat;

public abstract class HUDModule
extends Module {
    public final DoubleSetting posX;
    public final DoubleSetting posY;
    protected int width;
    protected int height;
    private boolean holdingMouse;
    protected AlignType alignType;

    public HUDModule(String name, Category category, double defaultX, double defaultY, int width, int height, AlignType alignType) {
        super(name, category);
        this.posX = new DoubleSetting("Pos X", () -> false, defaultX, 0.0, 1000.0, 0.5);
        this.posY = new DoubleSetting("Pos Y", () -> false, defaultY, 0.0, 1000.0, 0.5);
        this.width = width;
        this.height = height;
        this.alignType = alignType;
        this.listenType = EventListenType.MANUAL;
        this.startListening();
        this.addSettings(this.posX, this.posY);
    }

    @Listener
    public final void onRender(RenderEvent event) {
        boolean inChat = HUDModule.mc.currentScreen instanceof GuiChat;
        if (this.isEnabled() || inChat) {
            this.renderModule(inChat);
        }
    }

    protected abstract void renderModule(boolean var1);

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isHoldingMouse() {
        return this.holdingMouse;
    }

    public void setHoldingMouse(boolean holdingMouse) {
        this.holdingMouse = holdingMouse;
    }

    public AlignType getAlignType() {
        return this.alignType;
    }
}

