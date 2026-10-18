package baseclient.gg.dhyeybg.baseclient.module.player;

import baseclient.gg.dhyeybg.baseclient.module.Category;
import baseclient.gg.dhyeybg.baseclient.module.Module;
import baseclient.gg.dhyeybg.baseclient.module.SingleplayerWorld;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class AutoSmelt extends Module {
    private long nextActionTime;

    public AutoSmelt() {
        super("AutoSmelt", "Moves smeltable items and fuel into an open furnace", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc == null || mc.player == null || mc.playerController == null
                || !(mc.player.openContainer instanceof ContainerFurnace)
                || !SingleplayerWorld.isSingleplayer()
                || !mc.player.inventory.getItemStack().func_190926_b()
                || System.currentTimeMillis() < nextActionTime) {
            return;
        }

        ContainerFurnace furnace = (ContainerFurnace) mc.player.openContainer;
        int windowId = furnace.windowId;
        if (furnace.getSlot(2).getHasStack()) {
            mc.playerController.windowClick(windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
            nextActionTime = System.currentTimeMillis() + 150L;
            return;
        }

        for (int slot = 3; slot < furnace.inventorySlots.size(); slot++) {
            ItemStack stack = furnace.getSlot(slot).getStack();
            if (stack == null || stack.func_190926_b()) {
                continue;
            }
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
            if (result == null || result.func_190926_b()) {
                continue;
            }
            ItemStack currentInput = furnace.getSlot(0).getStack();
            if (currentInput.func_190926_b()
                    || ItemStack.areItemsEqual(currentInput, stack)
                            && ItemStack.areItemStackTagsEqual(currentInput, stack)
                            && currentInput.func_190916_E() < currentInput.getMaxStackSize()) {
                mc.playerController.windowClick(windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                nextActionTime = System.currentTimeMillis() + 200L;
                return;
            }
        }

        if (!furnace.getSlot(1).getHasStack()) {
            for (int slot = 3; slot < furnace.inventorySlots.size(); slot++) {
                ItemStack stack = furnace.getSlot(slot).getStack();
                if (stack != null && !stack.func_190926_b() && TileEntityFurnace.isItemFuel(stack)) {
                    mc.playerController.windowClick(windowId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                    nextActionTime = System.currentTimeMillis() + 200L;
                    return;
                }
            }
        }
    }
}
