/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.player;

import Acrimony.event.Listener;
import Acrimony.event.impl.UpdateEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.util.player.ArmorType;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class InventoryManager
extends Module {
    private final IntegerSetting delaySetting = new IntegerSetting("Delay", 1, 0, 10, 1);
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private ItemStack pickaxe;
    private ItemStack axe;
    private ItemStack shovel;
    private ItemStack block_stack;
    private ItemStack golden_apples;
    private int delay;
    private final int helmet_slot = 5;
    private final int chestplate_slot = 6;
    private final int leggings_slot = 7;
    private final int boots_slot = 8;
    private final IntegerSetting weapon_slot = new IntegerSetting("Sword slot", 0, 0, 8, 1);
    private final IntegerSetting block_stack_slot = new IntegerSetting("Block stack slot", 1, 0, 8, 1);
    private final IntegerSetting axe_slot = new IntegerSetting("Axe slot", 2, 0, 8, 1);
    private final IntegerSetting pickaxe_slot = new IntegerSetting("Pickaxe slot", 3, 0, 8, 1);
    private final IntegerSetting shovel_slot = new IntegerSetting("Shovel slot", 4, 0, 8, 1);
    private final IntegerSetting golden_apples_slot = new IntegerSetting("Golden apples slot", 5, 0, 8, 1);
    private final boolean consider_silk_touch = false;
    private final boolean priorise_golden_tool = false;

    public InventoryManager() {
        super("Inventory Manager", Category.PLAYER);
        this.addSettings(this.delaySetting, this.weapon_slot, this.axe_slot, this.pickaxe_slot, this.shovel_slot, this.block_stack_slot, this.golden_apples_slot);
    }

    @Override
    public void onEnable() {
        this.delay = 0;
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        this.helmet = container.getSlot(5).getStack();
        this.chestplate = container.getSlot(6).getStack();
        this.leggings = container.getSlot(7).getStack();
        this.boots = container.getSlot(8).getStack();
        this.weapon = container.getSlot(this.weapon_slot.getValue() + 36).getStack();
        this.axe = container.getSlot(this.axe_slot.getValue() + 36).getStack();
        this.pickaxe = container.getSlot(this.pickaxe_slot.getValue() + 36).getStack();
        this.shovel = container.getSlot(this.shovel_slot.getValue() + 36).getStack();
        this.block_stack = container.getSlot(this.block_stack_slot.getValue() + 36).getStack();
        this.golden_apples = container.getSlot(this.golden_apples_slot.getValue() + 36).getStack();
        if (InventoryManager.mc.currentScreen instanceof GuiInventory) {
            if (++this.delay > this.delaySetting.getValue()) {
                for (ArmorType type : ArmorType.values()) {
                    this.getBestArmor(type);
                }
                this.getBestWeapon();
                this.getBestAxe();
                this.getBestPickaxe();
                this.getBestShovel();
                this.getBlockStack();
                this.getGoldenApples();
                this.dropUselessItems();
            }
        } else {
            this.delay = 0;
        }
    }

    private void hotbarExchange(int hotbarNumber, int slotId) {
        InventoryManager.mc.playerController.windowClick(InventoryManager.mc.thePlayer.inventoryContainer.windowId, slotId, hotbarNumber, 2, InventoryManager.mc.thePlayer);
        this.delay = 0;
    }

    private void shiftClick(int slotId) {
        InventoryManager.mc.playerController.windowClick(InventoryManager.mc.thePlayer.inventoryContainer.windowId, slotId, 1, 1, InventoryManager.mc.thePlayer);
        this.delay = 0;
    }

    private void drop(int slotId) {
        InventoryManager.mc.playerController.windowClick(InventoryManager.mc.thePlayer.inventoryContainer.windowId, slotId, 1, 4, InventoryManager.mc.thePlayer);
        this.delay = 0;
    }

    private void dropUselessItems() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !this.isGarbage(stack)) continue;
            this.drop(i);
            break;
        }
    }

    public boolean isGarbage(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.snowball || item == Items.egg || item == Items.fishing_rod || item == Items.experience_bottle || item == Items.skull || item == Items.flint || item == Items.lava_bucket || item == Items.flint_and_steel || item == Items.string) {
            return true;
        }
        if (item instanceof ItemHoe) {
            return true;
        }
        if (item instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion)item;
            for (PotionEffect effect : potion.getEffects(stack)) {
                int id = effect.getPotionID();
                if (id != Potion.moveSlowdown.getId() && id != Potion.blindness.getId() && id != Potion.poison.getId() && id != Potion.digSlowdown.getId() && id != Potion.weakness.getId() && id != Potion.harm.getId()) continue;
                return true;
            }
        } else {
            String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
            if (itemName.contains("anvil") || itemName.contains("tnt") || itemName.contains("seed") || itemName.contains("table") || itemName.contains("string") || itemName.contains("eye") || itemName.contains("mushroom") || itemName.contains("chest") && !itemName.contains("plate") || itemName.contains("pressure_plate")) {
                return true;
            }
        }
        return false;
    }

    public boolean isUseless(ItemStack stack) {
        if (!this.isEnabled()) {
            return this.isGarbage(stack);
        }
        if (this.isGarbage(stack)) {
            return true;
        }
        if (this.helmet != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 0 && !this.isBetterArmor(stack, this.helmet, ArmorType.HELMET)) {
            return true;
        }
        if (this.chestplate != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 1 && !this.isBetterArmor(stack, this.chestplate, ArmorType.CHESTPLATE)) {
            return true;
        }
        if (this.leggings != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 2 && !this.isBetterArmor(stack, this.leggings, ArmorType.LEGGINGS)) {
            return true;
        }
        if (this.boots != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 3 && !this.isBetterArmor(stack, this.boots, ArmorType.BOOTS)) {
            return true;
        }
        if (stack.getItem() instanceof ItemSword && this.weapon != null && !this.isBetterWeapon(stack, this.weapon)) {
            return true;
        }
        if (stack.getItem() instanceof ItemAxe && this.axe != null && !this.isBetterTool(stack, this.axe)) {
            return true;
        }
        if (stack.getItem() instanceof ItemPickaxe && this.pickaxe != null && !this.isBetterTool(stack, this.pickaxe)) {
            return true;
        }
        return stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") && this.shovel != null && !this.isBetterTool(stack, this.shovel);
    }

    private void getBlockStack() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack blockStack = null;
        int slot = -1;
        if (this.block_stack == null || !this.shouldChooseBlock(this.block_stack)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !this.shouldChooseBlock(stack) || blockStack != null && stack.stackSize < blockStack.stackSize) continue;
                blockStack = stack;
                slot = i;
            }
        }
        if (blockStack != null) {
            this.hotbarExchange(this.block_stack_slot.getValue(), slot);
        }
    }

    private void getGoldenApples() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        if (this.golden_apples == null || !(this.golden_apples.getItem() instanceof ItemAppleGold)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !(stack.getItem() instanceof ItemAppleGold)) continue;
                this.hotbarExchange(this.golden_apples_slot.getValue(), i);
                return;
            }
        }
    }

    private boolean shouldChooseBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    private void getBestWeapon() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack oldWeapon = this.weapon;
        int newSwordSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemSword) || i == this.weapon_slot.getValue() + 36) continue;
            boolean better = this.isBetterWeapon(stack, oldWeapon);
            boolean worse = this.isWorseWeapon(stack, oldWeapon);
            if (better) {
                newSwordSlot = i;
                oldWeapon = stack;
                continue;
            }
            if (!(stack.getItem() instanceof ItemSword)) continue;
            dropSlot = i;
        }
        if (newSwordSlot != -1) {
            this.hotbarExchange(this.weapon_slot.getValue(), newSwordSlot);
        } else if (dropSlot != -1) {
            this.drop(dropSlot);
        }
    }

    private void getBestAxe() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack oldAxe = this.axe;
        int newAxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemAxe) || i == this.axe_slot.getValue() + 36) continue;
            if (this.isBetterTool(stack, oldAxe)) {
                newAxeSlot = i;
                oldAxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newAxeSlot != -1) {
            this.hotbarExchange(this.axe_slot.getValue(), newAxeSlot);
        } else if (dropSlot != -1) {
            this.drop(dropSlot);
        }
    }

    private void getBestPickaxe() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack oldPickaxe = this.pickaxe;
        int newPickaxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemPickaxe) || i == this.pickaxe_slot.getValue() + 36) continue;
            if (this.isBetterTool(stack, oldPickaxe)) {
                newPickaxeSlot = i;
                oldPickaxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newPickaxeSlot != -1) {
            this.hotbarExchange(this.pickaxe_slot.getValue(), newPickaxeSlot);
        } else if (dropSlot != -1) {
            this.drop(dropSlot);
        }
    }

    private void getBestShovel() {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack oldShovel = this.shovel;
        int newShovelSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemTool) || !stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") || i == this.shovel_slot.getValue() + 36) continue;
            if (this.isBetterTool(stack, oldShovel)) {
                newShovelSlot = i;
                oldShovel = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newShovelSlot != -1) {
            this.hotbarExchange(this.shovel_slot.getValue(), newShovelSlot);
        } else if (dropSlot != -1) {
            this.drop(dropSlot);
        }
    }

    private void getBestArmor(ArmorType type) {
        if (this.delay <= this.delaySetting.getValue()) {
            return;
        }
        Container container = InventoryManager.mc.thePlayer.inventoryContainer;
        ItemStack oldArmor = type == ArmorType.HELMET ? this.helmet : (type == ArmorType.CHESTPLATE ? this.chestplate : (type == ArmorType.LEGGINGS ? this.leggings : this.boots));
        int newArmorSlot = -1;
        int dropSlot = -1;
        int armorSlot = type == ArmorType.HELMET ? 5 : (type == ArmorType.CHESTPLATE ? 6 : (type == ArmorType.LEGGINGS ? 7 : 8));
        for (int i = 5; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemArmor)) continue;
            ItemArmor armor = (ItemArmor)stack.getItem();
            boolean better = this.isBetterArmor(stack, oldArmor, type);
            boolean worse = this.isWorseArmor(stack, oldArmor, type);
            if (armor.armorType != type.ordinal()) continue;
            if (better) {
                if (i == armorSlot) continue;
                if (oldArmor != null) {
                    dropSlot = armorSlot;
                    continue;
                }
                newArmorSlot = i;
                oldArmor = stack;
                armorSlot = i;
                continue;
            }
            if (worse || i != armorSlot) {
                dropSlot = i;
                continue;
            }
            if (i == armorSlot) continue;
            newArmorSlot = i;
            oldArmor = stack;
            armorSlot = i;
        }
        if (dropSlot != -1) {
            this.drop(dropSlot);
        } else if (newArmorSlot != -1) {
            this.shiftClick(newArmorSlot);
        }
    }

    private boolean isBetterWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return this.getAttackDamage(newWeapon) > this.getAttackDamage(oldWeapon);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return this.getAttackDamage(newWeapon) < this.getAttackDamage(oldWeapon);
            }
            return false;
        }
        return true;
    }

    private boolean isBetterTool(ItemStack newTool, ItemStack oldTool) {
        Item item = newTool.getItem();
        if (item instanceof ItemTool) {
            if (oldTool != null) {
                return this.getToolUsefulness(newTool) > this.getToolUsefulness(oldTool);
            }
            return true;
        }
        return false;
    }

    private boolean isBetterArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return true;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return this.getArmorProtection(newArmor) > this.getArmorProtection(oldArmor);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return false;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return this.getArmorProtection(newArmor) < this.getArmorProtection(oldArmor);
            }
            return false;
        }
        return true;
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseDamage = 0.0f;
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword)item;
            baseDamage += sword.getAttackDamage();
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            baseDamage += tool.getAttackDamage();
        }
        float enchantsDamage = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseDamage + enchantsDamage;
    }

    private float getToolUsefulness(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseUsefulness = 0.0f;
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            switch (tool.getToolMaterial()) {
                case WOOD: {
                    baseUsefulness = 1.0f;
                    break;
                }
                case GOLD: {
                    baseUsefulness = 1.0f;
                    break;
                }
                case STONE: {
                    baseUsefulness = 2.0f;
                    break;
                }
                case IRON: {
                    baseUsefulness = 3.0f;
                    break;
                }
                case EMERALD: {
                    baseUsefulness = 4.0f;
                }
            }
        }
        float enchantsUsefulness = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) * 0.5f + 0.0f;
        return baseUsefulness + enchantsUsefulness;
    }

    private float getArmorProtection(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseProtection = 0.0f;
        if (item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)item;
            baseProtection += (float)armor.damageReduceAmount;
        }
        float enchantsProtection = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) * 0.1f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseProtection + enchantsProtection;
    }
}

