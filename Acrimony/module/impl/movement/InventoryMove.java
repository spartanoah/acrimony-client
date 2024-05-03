/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.movement;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.EntityActionEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.ModeSetting;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class InventoryMove
extends Module {
    private final ModeSetting noSprint = new ModeSetting("No sprint", "Disabled", "Disabled", "Enabled", "Spoof");
    private final BooleanSetting blink = new BooleanSetting("Blink", false);
    private boolean hadInventoryOpened;
    private boolean blinking;

    public InventoryMove() {
        super("Inventory Move", Category.MOVEMENT);
        this.addSettings(this.noSprint, this.blink);
    }

    @Override
    public void onDisable() {
        if (this.blinking) {
            Acrimony.instance.getPacketBlinkHandler().stopBlinking();
            this.blinking = false;
        }
    }

    @Listener(value=3)
    public void onTick(TickEvent event) {
        if (this.isInventoryOpened()) {
            this.allowMove();
            if (this.noSprint.is("Enabled")) {
                InventoryMove.mc.gameSettings.keyBindSprint.pressed = false;
                InventoryMove.mc.thePlayer.setSprinting(false);
            }
            if (this.blink.isEnabled()) {
                Acrimony.instance.getPacketBlinkHandler().startBlinking();
                this.blinking = true;
            }
        } else {
            if (this.blinking) {
                Acrimony.instance.getPacketBlinkHandler().stopBlinking();
                this.blinking = false;
            }
            if (this.hadInventoryOpened) {
                this.allowMove();
                this.hadInventoryOpened = false;
            }
        }
    }

    @Listener(value=3)
    public void onUpdate(UpdateEvent event) {
        if (this.isInventoryOpened()) {
            this.allowMove();
            if (this.noSprint.is("Enabled")) {
                InventoryMove.mc.gameSettings.keyBindSprint.pressed = false;
                InventoryMove.mc.thePlayer.setSprinting(false);
            }
        }
    }

    @Listener(value=3)
    public void onEntityAction(EntityActionEvent event) {
        if (this.isInventoryOpened()) {
            this.allowMove();
            if (this.noSprint.is("Spoof")) {
                event.setSprinting(false);
            }
        }
    }

    private boolean isInventoryOpened() {
        return InventoryMove.mc.currentScreen instanceof GuiInventory || InventoryMove.mc.currentScreen instanceof GuiChest;
    }

    private void allowMove() {
        KeyBinding[] keys;
        GameSettings settings = InventoryMove.mc.gameSettings;
        for (KeyBinding key : keys = new KeyBinding[]{settings.keyBindForward, settings.keyBindBack, settings.keyBindLeft, settings.keyBindRight, settings.keyBindJump}) {
            key.pressed = Keyboard.isKeyDown(key.getKeyCode());
        }
        this.hadInventoryOpened = true;
    }
}

