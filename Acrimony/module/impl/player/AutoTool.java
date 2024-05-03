/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

public class AutoTool
extends Module {
    private int oldSlot;
    private boolean wasDigging;
    private final BooleanSetting spoof = new BooleanSetting("Item spoof", false);

    public AutoTool() {
        super("AutoTool", Category.PLAYER);
        this.addSettings(this.spoof);
    }

    @Override
    public void onDisable() {
        if (this.wasDigging) {
            AutoTool.mc.thePlayer.inventory.currentItem = this.oldSlot;
            this.wasDigging = false;
        }
        Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
    }

    @Listener(value=3)
    public void onTick(TickEvent event) {
        if ((Mouse.isButtonDown(0) || AutoTool.mc.gameSettings.keyBindAttack.isKeyDown()) && AutoTool.mc.objectMouseOver != null && AutoTool.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = AutoTool.mc.theWorld.getBlockState(AutoTool.mc.objectMouseOver.getBlockPos()).getBlock();
            float strength = 0.0f;
            if (!this.wasDigging) {
                this.oldSlot = AutoTool.mc.thePlayer.inventory.currentItem;
                if (this.spoof.isEnabled()) {
                    Acrimony.instance.getSlotSpoofHandler().startSpoofing(this.oldSlot);
                }
            }
            for (int i = 0; i <= 8; ++i) {
                float slotStrength;
                ItemStack stack = AutoTool.mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null || !((slotStrength = stack.getStrVsBlock(block)) > strength)) continue;
                AutoTool.mc.thePlayer.inventory.currentItem = i;
                strength = slotStrength;
            }
            this.wasDigging = true;
        } else if (this.wasDigging) {
            AutoTool.mc.thePlayer.inventory.currentItem = this.oldSlot;
            Acrimony.instance.getSlotSpoofHandler().stopSpoofing();
            this.wasDigging = false;
        } else {
            this.oldSlot = AutoTool.mc.thePlayer.inventory.currentItem;
        }
    }
}

