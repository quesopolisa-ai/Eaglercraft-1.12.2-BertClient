package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class AutoArmor extends Module {
    private Setting delay;
    private Setting minimumDurability;
    private long nextActionTime;

    public AutoArmor() {
        super("Auto Armor", "Equips better armor from your inventory", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        delay = new Setting("Delay MS", this, 250.0D, 0.0D, 2000.0D, true);
        minimumDurability = new Setting("Min Durability %", this, 5.0D, 0.0D, 100.0D, true);
        manager.rSetting(delay);
        manager.rSetting(minimumDurability);
    }

    @Override
    public void onEnable() {
        nextActionTime = 0L;
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.playerController == null
                || mc.currentScreen != null || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().func_190926_b()
                || mc.player.isHandActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) {
            return;
        }

        for (EntityEquipmentSlot armorSlot : armorSlots()) {
            int bestInventoryIndex = findBestArmor(armorSlot);
            if (bestInventoryIndex < 0) {
                continue;
            }

            int sourceContainerSlot = toContainerSlot(bestInventoryIndex);
            int armorContainerSlot = 8 - armorSlot.getIndex();
            Slot wornSlot = mc.player.inventoryContainer.getSlot(armorContainerSlot);
            if (!wornSlot.canTakeStack(mc.player)) {
                continue;
            }

            int windowId = mc.player.inventoryContainer.windowId;
            mc.playerController.windowClick(windowId, sourceContainerSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(windowId, armorContainerSlot, 0, ClickType.PICKUP, mc.player);
            if (!mc.player.inventory.getItemStack().func_190926_b()) {
                mc.playerController.windowClick(windowId, sourceContainerSlot, 0, ClickType.PICKUP, mc.player);
            }

            long delayMillis = delay == null ? 250L : (long) delay.getValDouble();
            nextActionTime = now + Math.max(0L, delayMillis);
            return;
        }
    }

    private int findBestArmor(EntityEquipmentSlot armorSlot) {
        ItemStack equipped = mc.player.getItemStackFromSlot(armorSlot);
        double equippedScore = armorScore(equipped);
        int bestIndex = -1;

        NonNullList<ItemStack> inventory = mc.player.inventory.mainInventory;
        for (int inventoryIndex = 0; inventoryIndex < inventory.size(); inventoryIndex++) {
            ItemStack stack = inventory.get(inventoryIndex);
            if (stack == null || stack.func_190926_b() || !(stack.getItem() instanceof ItemArmor)) {
                continue;
            }

            ItemArmor armor = (ItemArmor) stack.getItem();
            if (armor.getEquipmentSlot() != armorSlot
                    || getDurabilityPercent(stack) < getMinimumDurability()) {
                continue;
            }

            double score = armorScore(stack);
            if (score > equippedScore) {
                equippedScore = score;
                bestIndex = inventoryIndex;
            }
        }
        return bestIndex;
    }

    private double armorScore(ItemStack stack) {
        if (stack == null || stack.func_190926_b() || !(stack.getItem() instanceof ItemArmor)) {
            return 0.0D;
        }

        ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount * 1000.0D + armor.toughness * 100.0D
                + getDurabilityPercent(stack) * 0.1D;
    }

    private double getDurabilityPercent(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return 100.0D;
        }
        return 100.0D * (maxDamage - stack.getItemDamage()) / maxDamage;
    }

    private int getMinimumDurability() {
        return minimumDurability == null ? 5 : (int) minimumDurability.getValDouble();
    }

    private int toContainerSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
    }

    private EntityEquipmentSlot[] armorSlots() {
        return new EntityEquipmentSlot[] {
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET
        };
    }
}
