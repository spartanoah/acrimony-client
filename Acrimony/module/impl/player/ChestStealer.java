/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.module.impl.player.InventoryManager;
import Acrimony.setting.impl.BooleanSetting;
import Acrimony.setting.impl.IntegerSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class ChestStealer
extends Module {
    private final IntegerSetting delay = new IntegerSetting("Delay", 1, 0, 10, 1);
    private final BooleanSetting filter = new BooleanSetting("Filter", true);
    private final BooleanSetting autoClose = new BooleanSetting("Autoclose", true);
    private final BooleanSetting guiDetect = new BooleanSetting("Gui detect", true);
    private int counter;
    private InventoryManager invManager;

    public ChestStealer() {
        super("Chest Stealer", Category.PLAYER);
        this.addSettings(this.delay, this.filter, this.autoClose, this.guiDetect);
    }

    @Override
    public void onClientStarted() {
        this.invManager = Acrimony.instance.getModuleManager().getModule(InventoryManager.class);
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        if (!(ChestStealer.mc.thePlayer.openContainer == null || !(ChestStealer.mc.thePlayer.openContainer instanceof ContainerChest) || this.isGUI() && this.guiDetect.isEnabled())) {
            ContainerChest container = (ContainerChest)ChestStealer.mc.thePlayer.openContainer;
            for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);
                if (stack == null || this.isUseless(stack) || ++this.counter <= this.delay.getValue()) continue;
                ChestStealer.mc.playerController.windowClick(container.windowId, i, 1, 1, ChestStealer.mc.thePlayer);
                this.counter = 0;
                return;
            }
            if (this.autoClose.isEnabled() && this.isChestEmpty(container)) {
                ChestStealer.mc.thePlayer.closeScreen();
            }
        }
    }

    private boolean isChestEmpty(ContainerChest container) {
        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
            ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);
            if (stack == null || this.isUseless(stack)) continue;
            return false;
        }
        return true;
    }

    private boolean isUseless(ItemStack stack) {
        if (!this.filter.isEnabled()) {
            return false;
        }
        return this.invManager.isUseless(stack);
    }

    private boolean isGUI() {
        for (double x = ChestStealer.mc.thePlayer.posX - 5.0; x <= ChestStealer.mc.thePlayer.posX + 5.0; x += 1.0) {
            for (double y = ChestStealer.mc.thePlayer.posY - 5.0; y <= ChestStealer.mc.thePlayer.posY + 5.0; y += 1.0) {
                for (double z = ChestStealer.mc.thePlayer.posZ - 5.0; z <= ChestStealer.mc.thePlayer.posZ + 5.0; z += 1.0) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = ChestStealer.mc.theWorld.getBlockState(pos).getBlock();
                    if (!(block instanceof BlockChest) && !(block instanceof BlockEnderChest)) continue;
                    return false;
                }
            }
        }
        return true;
    }
}

