package baseclient.gg.dhyeybg.baseclient.module.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.settings.Setting;
import baseclient.gg.dhyeybg.baseclient.settings.SettingsManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class InventoryManager extends Module {
    private Setting sortInventory;
    private Setting equipArmor;
    private List<ItemStack> sortedOrder = Collections.emptyList();
    private int nextSlot;
    private boolean enabledArmor;

    public InventoryManager() {
        this("Inventory Manager");
    }

    protected InventoryManager(String moduleName) {
        super(moduleName, "Organizes inventory and coordinates armor management", Category.PLAYER);
    }

    public void registerSettings(SettingsManager manager) {
        sortInventory = new Setting("Sort Inventory", this, true);
        equipArmor = new Setting("Equip Better Armor", this, true);
        manager.rSetting(sortInventory);
        manager.rSetting(equipArmor);
    }

    @Override
    public void onEnable() {
        if (equipArmor != null && equipArmor.getValBoolean()
                && baseclient.gg.dhyeybg.baseclient.BaseClient.instance != null) {
            Module armor = baseclient.gg.dhyeybg.baseclient.BaseClient.instance.moduleManager
                    .getModule("Auto Armor");
            if (armor != null && !armor.isToggled()) {
                armor.setToggled(true);
                enabledArmor = armor.isToggled();
            }
        }
        beginSorting();
    }

    @Override
    public void onDisable() {
        if (baseclient.gg.dhyeybg.baseclient.BaseClient.instance != null) {
            Module armor = baseclient.gg.dhyeybg.baseclient.BaseClient.instance.moduleManager
                    .getModule("Auto Armor");
            if (armor != null && enabledArmor && armor.isToggled()) {
                armor.setToggled(false);
            }
        }
        enabledArmor = false;
        sortedOrder = Collections.emptyList();
    }

    private void beginSorting() {
        if (sortInventory == null || !sortInventory.getValBoolean()
                || mc == null || mc.player == null) {
            sortedOrder = Collections.emptyList();
            return;
        }
        sortedOrder = new ArrayList<ItemStack>();
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack != null && !stack.func_190926_b()) {
                sortedOrder.add(stack.copy());
            }
        }
        Collections.sort(sortedOrder, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack left, ItemStack right) {
                String leftName = registryName(left);
                String rightName = registryName(right);
                int nameOrder = leftName.compareTo(rightName);
                if (nameOrder != 0) {
                    return nameOrder;
                }
                int damageOrder = Integer.compare(left.getItemDamage(), right.getItemDamage());
                return damageOrder != 0 ? damageOrder
                        : Integer.compare(left.func_190916_E(), right.func_190916_E());
            }
        });
        nextSlot = 9;
    }

    @Override
    public void onUpdate() {
        if (sortedOrder.isEmpty() || mc == null || mc.player == null || mc.playerController == null
                || mc.currentScreen != null || mc.player.openContainer != mc.player.inventoryContainer
                || !mc.player.inventory.getItemStack().func_190926_b()) {
            return;
        }

        while (nextSlot < 36 && nextSlot - 9 < sortedOrder.size()) {
            ItemStack wanted = sortedOrder.get(nextSlot - 9);
            ItemStack current = mc.player.inventory.getStackInSlot(nextSlot);
            if (matches(current, wanted)) {
                nextSlot++;
                continue;
            }

            int sourceSlot = findMatchingSlot(wanted, nextSlot + 1);
            if (sourceSlot < 0) {
                nextSlot++;
                continue;
            }

            int windowId = mc.player.inventoryContainer.windowId;
            mc.playerController.windowClick(windowId, nextSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(windowId, sourceSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(windowId, nextSlot, 0, ClickType.PICKUP, mc.player);
            nextSlot++;
            return;
        }
        sortedOrder = Collections.emptyList();
    }

    private int findMatchingSlot(ItemStack wanted, int start) {
        for (int slot = start; slot < 36; slot++) {
            if (matches(mc.player.inventory.getStackInSlot(slot), wanted)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean matches(ItemStack stack, ItemStack wanted) {
        return stack != null && !stack.func_190926_b()
                && ItemStack.areItemsEqual(stack, wanted)
                && ItemStack.areItemStackTagsEqual(stack, wanted)
                && stack.func_190916_E() == wanted.func_190916_E();
    }

    private static String registryName(ItemStack stack) {
        ResourceLocation name = Item.REGISTRY.getNameForObject(stack.getItem());
        return name == null ? stack.getItem().getUnlocalizedName() : name.toString();
    }
}
